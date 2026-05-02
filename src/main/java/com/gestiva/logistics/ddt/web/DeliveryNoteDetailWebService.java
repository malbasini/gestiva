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
public class DeliveryNoteDetailWebService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final DeliveryNoteLineRepository deliveryNoteLineRepository;
    private final CustomerRepository customerRepository;

    public DeliveryNoteDetailWebService(DeliveryNoteRepository deliveryNoteRepository,
                                        DeliveryNoteLineRepository deliveryNoteLineRepository,
                                        CustomerRepository customerRepository) {
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.deliveryNoteLineRepository = deliveryNoteLineRepository;
        this.customerRepository = customerRepository;
    }

    public DeliveryNoteDetailView getDetail(Long tenantId, Long deliveryNoteId) {
        var note = deliveryNoteRepository.findByTenantIdAndId(tenantId, deliveryNoteId)
                .orElseThrow(() -> new NotFoundException("DDT non trovato"));

        var customer = customerRepository.findByTenantIdAndId(tenantId, note.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        var lines = deliveryNoteLineRepository.findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(tenantId, deliveryNoteId);

        DeliveryNoteDetailView view = new DeliveryNoteDetailView();
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
            l.setFormattedQuantity(PdfFormatUtils.formatDecimal(line.getQuantity()));
            l.setUnitOfMeasure(line.getUnitOfMeasure());
            l.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            l.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()));
            l.setFormattedTaxPct(PdfFormatUtils.formatDecimal(line.getTaxPct()));
            l.setFormattedLineTotal(PdfFormatUtils.formatMoney(line.getLineTotal()));
            return l;
        }).toList());

        return view;
    }
}