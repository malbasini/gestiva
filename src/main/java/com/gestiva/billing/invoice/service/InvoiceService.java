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
                .findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(tenantId, deliveryNoteId);

        if (deliveryNoteLines.isEmpty()) {
            throw new BusinessException("Impossibile creare una fattura da un DDT senza righe.");
        }

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
        invoice.setSubtotalAmount(defaultZero(deliveryNote.getSubtotalAmount()));
        invoice.setTaxAmount(defaultZero(deliveryNote.getTaxAmount()));
        invoice.setTotalAmount(defaultZero(deliveryNote.getTotalAmount()));
        invoice.setNotes(deliveryNote.getNotes());
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        int lineNo = 1;
        for (DeliveryNoteLine deliveryNoteLine : deliveryNoteLines) {
            InvoiceLine line = new InvoiceLine();
            line.setTenantId(tenantId);
            line.setInvoiceId(savedInvoice.getId());
            line.setDeliveryNoteLineId(deliveryNoteLine.getId());
            line.setSalesOrderLineId(deliveryNoteLine.getSalesOrderLineId());
            line.setLineNo(lineNo++);
            line.setDescription(deliveryNoteLine.getDescription());
            line.setQuantity(deliveryNoteLine.getQuantity());
            line.setUnitOfMeasure(deliveryNoteLine.getUnitOfMeasure());
            line.setUnitPrice(deliveryNoteLine.getUnitPrice());
            line.setDiscountPct(defaultZero(deliveryNoteLine.getDiscountPct()));
            line.setTaxPct(defaultZero(deliveryNoteLine.getTaxPct()));
            line.setTaxAmount(defaultZero(deliveryNoteLine.getTaxAmount()));
            line.setLineTotal(defaultZero(deliveryNoteLine.getLineTotal()));
            line.setItemId(deliveryNoteLine.getItemId());
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
