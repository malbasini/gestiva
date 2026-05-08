package com.gestiva.billing.invoice.service;

import com.gestiva.accounting.due.service.PaymentDueService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final DeliveryNoteLineRepository deliveryNoteLineRepository;
    private final PaymentDueService paymentDueService;






    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceLineRepository invoiceLineRepository,
                          DeliveryNoteRepository deliveryNoteRepository,
                          DeliveryNoteLineRepository deliveryNoteLineRepository,
                          PaymentDueService paymentDueService) {

        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.deliveryNoteLineRepository = deliveryNoteLineRepository;
        this.paymentDueService = paymentDueService;
    }

    public InvoiceResponse createFromDeliveryNote(Long tenantId, Long deliveryNoteId) {
        DeliveryNote deliveryNote = deliveryNoteRepository.findByTenantIdAndId(tenantId, deliveryNoteId)
                .orElseThrow(() -> new NotFoundException("DDT non trovato"));

        validateDeliveryNoteCanGenerateInvoice(deliveryNote, tenantId, deliveryNoteId);

        List<DeliveryNoteLine> deliveryNoteLines = deliveryNoteLineRepository
                .findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(tenantId, deliveryNoteId);

        if (deliveryNoteLines.isEmpty()) {
            throw new BusinessException("Impossibile creare una fattura da un DDT senza righe.");
        }

        OffsetDateTime now = OffsetDateTime.now();

        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setDeliveryNoteId(deliveryNote.getId());
        invoice.setSalesOrderId(deliveryNote.getSalesOrderId());
        invoice.setCustomerId(deliveryNote.getCustomerId());
        invoice.setInvoiceNumber(generateNextInvoiceNumber(tenantId, now.toLocalDate()));
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setStatus("ISSUED");
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










        return toResponse(savedInvoice);
    }

    private void validateDeliveryNoteCanGenerateInvoice(DeliveryNote deliveryNote,
                                                        Long tenantId,
                                                        Long deliveryNoteId) {
        if (!"ISSUED".equals(deliveryNote.getStatus())) {
            throw new BusinessException("La fattura può essere creata solo da un DDT ISSUED.");
        }

        boolean alreadyExists = invoiceRepository.existsByTenantIdAndDeliveryNoteId(tenantId, deliveryNoteId);
        if (alreadyExists) {
            throw new BusinessException("Esiste già una fattura associata a questo DDT.");
        }
    }

    private String generateNextInvoiceNumber(Long tenantId, LocalDate date) {
        long count = invoiceRepository.countByTenantId(tenantId) + 1;
        return "INV-" + date.getYear() + "-" + String.format("%05d", count);
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
