package com.gestiva.logistics.ddt.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DeliveryNoteDocumentWebService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final DeliveryNoteLineRepository deliveryNoteLineRepository;
    private final CustomerRepository customerRepository;

    public DeliveryNoteDocumentWebService(DeliveryNoteRepository deliveryNoteRepository,
                                          DeliveryNoteLineRepository deliveryNoteLineRepository,
                                          CustomerRepository customerRepository) {
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.deliveryNoteLineRepository = deliveryNoteLineRepository;
        this.customerRepository = customerRepository;
    }

    public DeliveryNoteDocumentView getDocument(Long tenantId, Long deliveryNoteId) {
        var note = deliveryNoteRepository.findByTenantIdAndId(tenantId, deliveryNoteId)
                .orElseThrow(() -> new NotFoundException("DDT non trovato"));

        var customer = customerRepository.findByTenantIdAndId(tenantId, note.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        var lines = deliveryNoteLineRepository.findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(tenantId, deliveryNoteId);

        DeliveryNoteDocumentView view = new DeliveryNoteDocumentView();
        view.setId(note.getId());
        view.setCustomerId(note.getCustomerId());
        view.setSalesOrderId(note.getSalesOrderId());
        view.setCustomerName(customer.getName());
        view.setDdtNumber(note.getDdtNumber());
        view.setStatus(note.getStatus());
        view.setCurrencyCode(note.getCurrencyCode());

        view.setFormattedDdtDate(PdfFormatUtils.formatDate(note.getDdtDate()));
        view.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(note.getSubtotalAmount()));
        view.setFormattedTaxAmount(PdfFormatUtils.formatMoney(note.getTaxAmount()));
        view.setFormattedTotalAmount(PdfFormatUtils.formatMoney(note.getTotalAmount()));

        view.setTransportReason(note.getTransportReason());
        view.setCarriageCondition(note.getCarriageCondition());
        view.setCarrierName(note.getCarrierName());
        view.setNotes(note.getNotes());

        view.setLines(lines.stream().map(line -> {
            DeliveryNoteDetailLineView l = new DeliveryNoteDetailLineView();
            l.setLineNo(line.getLineNo());
            l.setDescription(line.getDescription());
            l.setFormattedQuantity(PdfFormatUtils.formatDecimalTrimmed(line.getQuantity(),2));
            l.setUnitOfMeasure(line.getUnitOfMeasure());
            l.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            l.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()));
            l.setFormattedTaxPct(PdfFormatUtils.formatDecimalTrimmed(line.getTaxPct(),2));
            java.math.BigDecimal gross = line.getQuantity().multiply(line.getUnitPrice());
            java.math.BigDecimal discountAmount = gross
                         .multiply(line.getDiscountPct())
                         .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal taxable = gross.subtract(discountAmount);
            l.setFormattedLineTotal(PdfFormatUtils.formatMoney(taxable));
            return l;
        }).toList());

        return view;
    }
}