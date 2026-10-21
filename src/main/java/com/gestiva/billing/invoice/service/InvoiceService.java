package com.gestiva.billing.invoice.service;

import com.gestiva.accounting.due.service.PaymentDueService;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.billing.invoice.dto.InvoiceResponse;
import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.entity.DeliveryNoteLine;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.security.company.context.CompanyContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.entity.SalesOrderLine;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.order.repository.SalesOrderLineRepository;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.entity.ItemType;
import com.gestiva.warehouse.item.repository.ItemRepository;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final DeliveryNoteLineRepository deliveryNoteLineRepository;
    private final PaymentDueService paymentDueService;
    private final JournalAutoPostingService journalAutoPostingService;
    private final DocumentSequenceService documentSequenceService;
    private final CompanyContext companyContext;
    private final ItemRepository itemRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceLineRepository invoiceLineRepository,
                          SalesOrderRepository salesOrderRepository,
                          SalesOrderLineRepository salesOrderLineRepository,
                          DeliveryNoteRepository deliveryNoteRepository,
                          DeliveryNoteLineRepository deliveryNoteLineRepository,
                          PaymentDueService paymentDueService,
                          JournalAutoPostingService journalAutoPostingService,
                          DocumentSequenceService documentSequenceService,
                          ItemRepository itemRepository,
                          CompanyContext companyContext) {

        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.deliveryNoteLineRepository = deliveryNoteLineRepository;
        this.paymentDueService = paymentDueService;
        this.journalAutoPostingService = journalAutoPostingService;
        this.documentSequenceService = documentSequenceService;
        this.companyContext = companyContext;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.itemRepository = itemRepository;

    }
    public InvoiceResponse createFromDeliveryNote(Long tenantId, Long deliveryNoteId) {
        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException("Nessuna società attiva selezionata.");
        }
        DeliveryNote deliveryNote = deliveryNoteRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        deliveryNoteId
                )
                .orElseThrow(() -> new NotFoundException(
                        "DDT non trovato nella società attiva."
                ));
        validateDeliveryNoteCanGenerateInvoice(
                deliveryNote,
                tenantId,
                companyId,
                deliveryNoteId
        );

        boolean orderAlreadyInvoiced = !invoiceRepository
                .findByTenantIdAndCompanyIdAndSalesOrderIdOrderByInvoiceDateDescIdDesc(
                        tenantId,
                        companyId,
                        deliveryNote.getSalesOrderId()
                )
                .isEmpty();

        if (orderAlreadyInvoiced) {
            throw new BusinessException(
                    "Impossibile generare la fattura: "
                            + "l'ordine cliente risulta già fatturato."
            );
        }

        List<DeliveryNoteLine> deliveryNoteLines = deliveryNoteLineRepository
                .findByTenantIdAndCompanyIdAndDeliveryNoteIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        deliveryNoteId
                );

        if (deliveryNoteLines.isEmpty()) {
            throw new BusinessException("Impossibile creare una fattura da un DDT senza righe.");
        }

        // Recupero dell'ordine originale associato al DDT.
        // Necessario per includere nella fattura anche i servizi.
        if (deliveryNote.getSalesOrderId() == null) {
            throw new BusinessException(
                    "Impossibile generare la fattura: "
                            + "il DDT non è associato a un ordine cliente."
            );
        }

        SalesOrder order = salesOrderRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        deliveryNote.getSalesOrderId()
                )
                .orElseThrow(() -> new BusinessException(
                        "Impossibile generare la fattura: "
                                + "ordine cliente non trovato nella società attiva."
                ));

        List<SalesOrderLine> orderLines = salesOrderLineRepository
                .findByTenantIdAndCompanyIdAndSalesOrderIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        order.getId()
                );

        if (orderLines.isEmpty()) {
            throw new BusinessException(
                    "Impossibile generare la fattura: "
                            + "l'ordine cliente non contiene righe."
            );
        }

        if (!order.getCustomerId().equals(deliveryNote.getCustomerId())) {
            throw new BusinessException(
                    "Impossibile generare la fattura: "
                            + "il cliente del DDT non corrisponde "
                            + "al cliente dell'ordine."
            );
        }

        // La valuta del DDT deve corrispondere a quella
        // dell'ordine originale, da cui derivano gli importi
        // complessivi della fattura.

        if (order.getCurrencyCode() == null
                || deliveryNote.getCurrencyCode() == null
                || !order.getCurrencyCode().equals(
                deliveryNote.getCurrencyCode())) {

            throw new BusinessException(
                    "Impossibile generare la fattura: "
                            + "la valuta del DDT non corrisponde "
                            + "alla valuta dell'ordine cliente."
            );
        }

        validateDeliveryNoteLinesAgainstOrder(
                tenantId,
                orderLines,
                deliveryNoteLines
        );

        validateSalesOrderTotals(order, orderLines);

        validateDeliveryNoteTotals(deliveryNote, deliveryNoteLines);

        OffsetDateTime now = OffsetDateTime.now();

        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setCompanyId(companyId);
        invoice.setDeliveryNoteId(deliveryNote.getId());
        invoice.setSalesOrderId(deliveryNote.getSalesOrderId());
        invoice.setCustomerId(deliveryNote.getCustomerId());
        invoice.setInvoiceNumber(generateNextInvoiceNumber(tenantId, companyId));
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setStatus("DRAFT");
        invoice.setCurrencyCode(deliveryNote.getCurrencyCode());
        // La fattura comprende prodotti e servizi dell'intero ordine.
        invoice.setSubtotalAmount(defaultZero(order.getSubtotalAmount()));
        invoice.setTaxAmount(defaultZero(order.getTaxAmount()));
        invoice.setTotalAmount(defaultZero(order.getTotalAmount()));
        invoice.setNotes(deliveryNote.getNotes());
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        // Associazione tra righe dell'ordine e righe PRODUCT del DDT.
        Map<Long, DeliveryNoteLine> deliveryNoteLinesByOrderLineId =
                new HashMap<>();

        for (DeliveryNoteLine deliveryNoteLine : deliveryNoteLines) {

            DeliveryNoteLine previous = deliveryNoteLinesByOrderLineId.put(
                    deliveryNoteLine.getSalesOrderLineId(),
                    deliveryNoteLine
            );

            if (previous != null) {
                throw new BusinessException(
                        "Il DDT contiene righe prodotto duplicate."
                );
            }
        }
        // Le righe della fattura seguono la sequenza originale dell'ordine.
        // PRODUCT: dati provenienti dal DDT.
        // SERVICE: dati provenienti direttamente dall'ordine.
        int lineNo = 1;
        for (SalesOrderLine orderLine : orderLines) {
            Item item = itemRepository
                    .findByTenantIdAndId(
                            tenantId,
                            orderLine.getItemId()
                    )
                    .orElseThrow(() -> new BusinessException(
                            "Articolo non trovato per la riga "
                                    + orderLine.getLineNo()
                    ));
            InvoiceLine line = new InvoiceLine();
            line.setTenantId(tenantId);
            line.setInvoiceId(savedInvoice.getId());
            line.setSalesOrderLineId(orderLine.getId());
            line.setLineNo(lineNo++);
            if (item.getItemType() == ItemType.PRODUCT) {
                DeliveryNoteLine deliveryNoteLine =
                        deliveryNoteLinesByOrderLineId.get(orderLine.getId());
                if (deliveryNoteLine == null) {
                    throw new BusinessException(
                            "Riga DDT mancante per il prodotto "
                                    + orderLine.getLineNo()
                    );
                }
                line.setDeliveryNoteLineId(deliveryNoteLine.getId());
                line.setDescription(deliveryNoteLine.getDescription());
                line.setQuantity(deliveryNoteLine.getQuantity());
                line.setUnitOfMeasure(deliveryNoteLine.getUnitOfMeasure());
                line.setUnitPrice(deliveryNoteLine.getUnitPrice());
                line.setDiscountPct(defaultZero(deliveryNoteLine.getDiscountPct()));
                line.setTaxPct(defaultZero(deliveryNoteLine.getTaxPct()));
                line.setTaxAmount(defaultZero(deliveryNoteLine.getTaxAmount()));
                line.setLineTotal(defaultZero(deliveryNoteLine.getLineTotal()));
                line.setItemId(deliveryNoteLine.getItemId());
            } else if (item.getItemType() == ItemType.SERVICE) {
                // I servizi non transitano dal DDT.
                line.setDeliveryNoteLineId(null);
                line.setDescription(orderLine.getDescription());
                line.setQuantity(orderLine.getQuantity());
                line.setUnitOfMeasure(orderLine.getUnitOfMeasure());
                line.setUnitPrice(orderLine.getUnitPrice());
                line.setDiscountPct(defaultZero(orderLine.getDiscountPct()));
                line.setTaxPct(defaultZero(orderLine.getTaxPct()));
                line.setTaxAmount(defaultZero(orderLine.getTaxAmount()));
                line.setLineTotal(defaultZero(orderLine.getLineTotal()));
                line.setItemId(orderLine.getItemId());
            } else {
                throw new BusinessException(
                        "Tipo articolo non supportato nella riga "
                                + orderLine.getLineNo()
                );
            }
            invoiceLineRepository.save(line);
        }
        paymentDueService.createReceivableFromCustomerInvoice(
                tenantId,
                invoice.getCustomerId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getCurrencyCode(),
                invoice.getTotalAmount(),
                invoice.getId()
        );
        journalAutoPostingService.postCustomerInvoice(
                tenantId,
                invoice.getInvoiceDate(),
                invoice.getInvoiceNumber(),
                invoice.getSubtotalAmount(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                invoice.getCurrencyCode(),
                invoice.getId(),
                invoice.getNotes()
        );
        return toResponse(savedInvoice);
    }

    public InvoiceResponse createFromSalesOrder(
            Long tenantId,
            Long salesOrderId) {

        Long companyId = companyContext.resolveCurrentCompanyId();

        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata."
            );
        }

        SalesOrder order = salesOrderRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        salesOrderId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Ordine cliente non trovato nella società attiva."
                ));

        List<SalesOrderLine> orderLines = salesOrderLineRepository
                .findByTenantIdAndCompanyIdAndSalesOrderIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        salesOrderId
                );

        validateSalesOrderForDirectInvoice(
                tenantId,
                companyId,
                order,
                orderLines
        );

        validateSalesOrderTotals(order, orderLines);

        // Un ordine già associato a un DDT deve seguire
        // esclusivamente il normale percorso di fatturazione da DDT.
        if (!deliveryNoteRepository
                .findByTenantIdAndCompanyIdAndSalesOrderId(
                        tenantId,
                        companyId,
                        salesOrderId
                )
                .isEmpty()) {

            throw new BusinessException(
                    "L'ordine è già associato a un DDT. "
                            + "Utilizzare la fatturazione da DDT."
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setCompanyId(companyId);
        invoice.setDeliveryNoteId(null);
        invoice.setSalesOrderId(order.getId());
        invoice.setCustomerId(order.getCustomerId());
        invoice.setInvoiceNumber(
                generateNextInvoiceNumber(tenantId, companyId)
        );
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setStatus("DRAFT");
        invoice.setCurrencyCode(order.getCurrencyCode());
        invoice.setSubtotalAmount(defaultZero(order.getSubtotalAmount()));
        invoice.setTaxAmount(defaultZero(order.getTaxAmount()));
        invoice.setTotalAmount(defaultZero(order.getTotalAmount()));
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        int lineNo = 1;

        for (SalesOrderLine orderLine : orderLines) {

            InvoiceLine line = new InvoiceLine();
            line.setTenantId(tenantId);
            line.setInvoiceId(savedInvoice.getId());
            line.setDeliveryNoteLineId(null);
            line.setSalesOrderLineId(orderLine.getId());
            line.setLineNo(lineNo++);
            line.setDescription(orderLine.getDescription());
            line.setQuantity(orderLine.getQuantity());
            line.setUnitOfMeasure(orderLine.getUnitOfMeasure());
            line.setUnitPrice(orderLine.getUnitPrice());
            line.setDiscountPct(defaultZero(orderLine.getDiscountPct()));
            line.setTaxPct(defaultZero(orderLine.getTaxPct()));
            line.setTaxAmount(defaultZero(orderLine.getTaxAmount()));
            line.setLineTotal(defaultZero(orderLine.getLineTotal()));
            line.setItemId(orderLine.getItemId());

            invoiceLineRepository.save(line);
        }

        paymentDueService.createReceivableFromCustomerInvoice(
                tenantId,
                savedInvoice.getCustomerId(),
                savedInvoice.getInvoiceNumber(),
                savedInvoice.getInvoiceDate(),
                savedInvoice.getCurrencyCode(),
                savedInvoice.getTotalAmount(),
                savedInvoice.getId()
        );

        journalAutoPostingService.postCustomerInvoice(
                tenantId,
                savedInvoice.getInvoiceDate(),
                savedInvoice.getInvoiceNumber(),
                savedInvoice.getSubtotalAmount(),
                savedInvoice.getTaxAmount(),
                savedInvoice.getTotalAmount(),
                savedInvoice.getCurrencyCode(),
                savedInvoice.getId(),
                savedInvoice.getNotes()
        );

        return toResponse(savedInvoice);
    }

    private void validateSalesOrderForDirectInvoice(
            Long tenantId,
            Long companyId,
            SalesOrder order,
            List<SalesOrderLine> orderLines) {
        if (!"FULFILLED".equals(order.getStatus())) {
            throw new BusinessException(
                    "La fattura diretta può essere generata solo "
                            + "da un ordine con stato FULFILLED."
            );
        }
        if (orderLines == null || orderLines.isEmpty()) {
            throw new BusinessException(
                    "Impossibile fatturare un ordine senza righe."
            );
        }
        boolean alreadyInvoiced = !invoiceRepository
                .findByTenantIdAndCompanyIdAndSalesOrderIdOrderByInvoiceDateDescIdDesc(
                        tenantId,
                        companyId,
                        order.getId()
                )
                .isEmpty();
        if (alreadyInvoiced) {
            throw new BusinessException(
                    "L'ordine cliente risulta già fatturato."
            );
        }
        for (SalesOrderLine orderLine : orderLines) {
            if (orderLine.getItemId() == null) {
                throw new BusinessException(
                        "Fatturazione diretta non consentita: "
                                + "la riga " + orderLine.getLineNo()
                                + " non ha un articolo associato."
                );
            }
            Item item = itemRepository
                    .findByTenantIdAndId(
                            tenantId,
                            orderLine.getItemId()
                    )
                    .orElseThrow(() -> new BusinessException(
                            "Articolo non trovato per la riga "
                                    + orderLine.getLineNo()
                    ));

            if (item.getItemType() != ItemType.SERVICE) {
                throw new BusinessException(
                        "Fatturazione diretta non consentita: "
                                + "la riga " + orderLine.getLineNo()
                                + " contiene un articolo che non è un servizio."
                );
            }
        }
    }

    private void validateDeliveryNoteCanGenerateInvoice(
            DeliveryNote deliveryNote,
            Long tenantId,
            Long companyId,
            Long deliveryNoteId) {
        if (!"ISSUED".equals(deliveryNote.getStatus())) {
            throw new BusinessException(
                    "La fattura può essere creata solo da un DDT ISSUED."
            );
        }
        boolean alreadyExists = invoiceRepository
                .existsByTenantIdAndCompanyIdAndDeliveryNoteId(
                        tenantId,
                        companyId,
                        deliveryNoteId
                );
        if (alreadyExists) {
            throw new BusinessException(
                    "Esiste già una fattura associata a questo DDT."
            );
        }
    }

    private void validateDeliveryNoteLinesAgainstOrder(
            Long tenantId,
            List<SalesOrderLine> orderLines,
            List<DeliveryNoteLine> deliveryNoteLines) {

        Map<Long, SalesOrderLine> orderLinesById = new HashMap<>();
        Set<Long> expectedProductLineIds = new HashSet<>();

        for (SalesOrderLine orderLine : orderLines) {

            if (orderLine.getItemId() == null) {
                throw new BusinessException(
                        "La riga " + orderLine.getLineNo()
                                + " dell'ordine non ha un articolo associato."
                );
            }

            Item item = itemRepository
                    .findByTenantIdAndId(tenantId, orderLine.getItemId())
                    .orElseThrow(() -> new BusinessException(
                            "Articolo non trovato per la riga "
                                    + orderLine.getLineNo()
                    ));

            if (orderLinesById.put(orderLine.getId(), orderLine) != null) {
                throw new BusinessException(
                        "Riga ordine duplicata: " + orderLine.getId()
                );
            }

            if (item.getItemType() == ItemType.PRODUCT) {
                expectedProductLineIds.add(orderLine.getId());
            } else if (item.getItemType() != ItemType.SERVICE) {
                throw new BusinessException(
                        "Tipo articolo non supportato nella riga "
                                + orderLine.getLineNo()
                );
            }
        }

        Set<Long> actualProductLineIds = new HashSet<>();

        for (DeliveryNoteLine ddtLine : deliveryNoteLines) {

            Long orderLineId = ddtLine.getSalesOrderLineId();

            SalesOrderLine orderLine = orderLinesById.get(orderLineId);

            if (orderLine == null) {
                throw new BusinessException(
                        "Il DDT contiene una riga non appartenente all'ordine."
                );
            }

            if (!expectedProductLineIds.contains(orderLineId)) {
                throw new BusinessException(
                        "Il DDT contiene una riga che non è un prodotto."
                );
            }

            if (!actualProductLineIds.add(orderLineId)) {
                throw new BusinessException(
                        "Il DDT contiene una riga prodotto duplicata."
                );
            }

            if (!Objects.equals(ddtLine.getItemId(), orderLine.getItemId())
                    || !Objects.equals(
                    ddtLine.getDescription(),
                    orderLine.getDescription())
                    || !Objects.equals(
                    ddtLine.getUnitOfMeasure(),
                    orderLine.getUnitOfMeasure())
                    || !Objects.equals(
                    ddtLine.getQuantity(),
                    orderLine.getQuantity())
                    && (ddtLine.getQuantity() == null
                    || orderLine.getQuantity() == null
                    || ddtLine.getQuantity().compareTo(orderLine.getQuantity()) != 0)
                    || ddtLine.getUnitPrice() == null
                    || orderLine.getUnitPrice() == null
                    || ddtLine.getUnitPrice().compareTo(orderLine.getUnitPrice()) != 0
                    || !Objects.equals(
                    defaultZero(ddtLine.getDiscountPct()),
                    defaultZero(orderLine.getDiscountPct()))
                    && defaultZero(ddtLine.getDiscountPct())
                    .compareTo(defaultZero(orderLine.getDiscountPct())) != 0
                    || defaultZero(ddtLine.getTaxPct())
                    .compareTo(defaultZero(orderLine.getTaxPct())) != 0
                    || ddtLine.getLineTotal() == null
                    || orderLine.getLineTotal() == null
                    || ddtLine.getLineTotal().compareTo(orderLine.getLineTotal()) != 0
                    || defaultZero(ddtLine.getTaxAmount())
                    .compareTo(defaultZero(orderLine.getTaxAmount())) != 0) {

                throw new BusinessException(
                        "La riga prodotto " + orderLine.getLineNo()
                                + " del DDT non corrisponde all'ordine."
                );
            }
        }

        if (!actualProductLineIds.equals(expectedProductLineIds)) {
            throw new BusinessException(
                    "Il DDT non contiene tutti i prodotti dell'ordine."
            );
        }
    }

    private void validateSalesOrderTotals(
            SalesOrder order,
            List<SalesOrderLine> orderLines) {

        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        BigDecimal calculatedTax = BigDecimal.ZERO;

        for (SalesOrderLine line : orderLines) {

            if (line.getLineTotal() == null || line.getTaxAmount() == null) {
                throw new BusinessException(
                        "Impossibile generare la fattura: "
                                + "importi mancanti nella riga "
                                + line.getLineNo()
                );
            }

            calculatedSubtotal = calculatedSubtotal.add(
                    line.getLineTotal()
            );

            calculatedTax = calculatedTax.add(
                    line.getTaxAmount()
            );
        }

        BigDecimal calculatedTotal = calculatedSubtotal.add(calculatedTax);

        if (order.getSubtotalAmount() == null
                || order.getTaxAmount() == null
                || order.getTotalAmount() == null) {

            throw new BusinessException(
                    "Impossibile generare la fattura: "
                            + "totali dell'ordine mancanti."
            );
        }

        if (calculatedSubtotal.compareTo(order.getSubtotalAmount()) != 0
                || calculatedTax.compareTo(order.getTaxAmount()) != 0
                || calculatedTotal.compareTo(order.getTotalAmount()) != 0) {

            throw new BusinessException(
                    "Impossibile generare la fattura: "
                            + "i totali dell'ordine non corrispondono "
                            + "alla somma delle righe."
            );
        }
    }

    private void validateDeliveryNoteTotals(
            DeliveryNote deliveryNote,
            List<DeliveryNoteLine> deliveryNoteLines) {

        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        BigDecimal calculatedTax = BigDecimal.ZERO;

        for (DeliveryNoteLine line : deliveryNoteLines) {

            if (line.getLineTotal() == null || line.getTaxAmount() == null) {
                throw new BusinessException(
                        "Impossibile generare la fattura: "
                                + "importi mancanti nelle righe del DDT."
                );
            }

            calculatedSubtotal = calculatedSubtotal.add(line.getLineTotal());
            calculatedTax = calculatedTax.add(line.getTaxAmount());
        }

        BigDecimal calculatedTotal = calculatedSubtotal.add(calculatedTax);

        if (deliveryNote.getSubtotalAmount() == null
                || deliveryNote.getTaxAmount() == null
                || deliveryNote.getTotalAmount() == null) {

            throw new BusinessException(
                    "Impossibile generare la fattura: "
                            + "totali del DDT mancanti."
            );
        }

        if (calculatedSubtotal.compareTo(deliveryNote.getSubtotalAmount()) != 0
                || calculatedTax.compareTo(deliveryNote.getTaxAmount()) != 0
                || calculatedTotal.compareTo(deliveryNote.getTotalAmount()) != 0) {

            throw new BusinessException(
                    "Impossibile generare la fattura: "
                            + "i totali del DDT non corrispondono "
                            + "alla somma delle righe prodotto."
            );
        }
    }

    private String generateNextInvoiceNumber(Long tenantId, Long companyId) {
        return documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.SALES_INVOICE
        );
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setCustomerId(invoice.getCustomerId());
        response.setDeliveryNoteId(invoice.getDeliveryNoteId());
        response.setSalesOrderId(invoice.getSalesOrderId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setStatus(invoice.getStatus());
        response.setCurrencyCode(invoice.getCurrencyCode());
        response.setSubtotalAmount(invoice.getSubtotalAmount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setTotalAmount(invoice.getTotalAmount());
        return response;
    }
}
