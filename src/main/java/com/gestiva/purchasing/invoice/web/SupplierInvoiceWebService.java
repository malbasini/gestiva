package com.gestiva.purchasing.invoice.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.supplier.entity.Supplier;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SupplierInvoiceWebService {

    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final SupplierInvoiceLineRepository supplierInvoiceLineRepository;
    private final SupplierRepository supplierRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public SupplierInvoiceWebService(SupplierInvoiceRepository supplierInvoiceRepository,
                                     SupplierInvoiceLineRepository supplierInvoiceLineRepository,
                                     SupplierRepository supplierRepository,
                                     GoodsReceiptRepository goodsReceiptRepository,
                                     PurchaseOrderRepository purchaseOrderRepository) {
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.supplierInvoiceLineRepository = supplierInvoiceLineRepository;
        this.supplierRepository = supplierRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public List<SupplierInvoiceListItemView> findAll(Long tenantId) {
        var invoices = supplierInvoiceRepository.findAll(
                org.springframework.data.jpa.domain.Specification
                        .where((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId)),
                Sort.by(Sort.Direction.DESC, "invoiceDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        List<SupplierInvoiceListItemView> result = new ArrayList<>();
        for (var invoice : invoices) {
            SupplierInvoiceListItemView v = new SupplierInvoiceListItemView();
            v.setId(invoice.getId());
            v.setInvoiceNumber(invoice.getInvoiceNumber());
            v.setFormattedInvoiceDate(PdfFormatUtils.formatDate(invoice.getInvoiceDate()));
            v.setSupplierName(
                    supplierRepository.findByTenantIdAndId(tenantId, invoice.getSupplierId())
                            .map(s -> s.getName())
                            .orElse("-")
            );
            v.setStatus(invoice.getStatus());
            v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(invoice.getTotalAmount()));
            result.add(v);
        }

        return result;
    }

    public SupplierInvoiceDetailView getDetail(Long tenantId, Long id) {
        var invoice = supplierInvoiceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Fattura fornitore non trovata"));

        var lines = supplierInvoiceLineRepository.findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(tenantId, id);

        SupplierInvoiceDetailView v = new SupplierInvoiceDetailView();
        v.setId(invoice.getId());
        v.setInvoiceNumber(invoice.getInvoiceNumber());
        v.setFormattedInvoiceDate(PdfFormatUtils.formatDate(invoice.getInvoiceDate()));
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(tenantId, invoice.getSupplierId())
                        .map(s -> s.getName())
                        .orElse("-")
        );
        v.setStatus(invoice.getStatus());
        v.setCurrencyCode(invoice.getCurrencyCode());
        v.setNotes(invoice.getNotes());
        v.setGoodsReceiptId(invoice.getGoodsReceiptId());
        v.setGoodsReceiptNumber(
                goodsReceiptRepository.findByTenantIdAndId(tenantId, invoice.getGoodsReceiptId())
                        .map(gr -> gr.getReceiptNumber())
                        .orElse("-")
        );
        v.setPurchaseOrderId(invoice.getPurchaseOrderId());
        v.setPurchaseOrderNumber(
                purchaseOrderRepository.findByTenantIdAndId(tenantId, invoice.getPurchaseOrderId())
                        .map(po -> po.getOrderNumber())
                        .orElse("-")
        );
        v.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(invoice.getSubtotalAmount()));
        v.setFormattedTaxAmount(PdfFormatUtils.formatMoney(invoice.getTaxAmount()));
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(invoice.getTotalAmount()));

        for (var line : lines) {
            SupplierInvoiceDetailLineView lv = new SupplierInvoiceDetailLineView();
            lv.setLineNo(line.getLineNo());
            lv.setDescription(line.getDescription());
            lv.setFormattedQuantity(PdfFormatUtils.formatDecimalTrimmed(line.getQuantity(),2));
            lv.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            lv.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()) + "%");
            lv.setFormattedTaxPct(PdfFormatUtils.formatDecimalTrimmed(line.getTaxPct(),2) + "%");
            lv.setFormattedLineTotal(PdfFormatUtils.formatMoney(line.getLineTotal()));
            v.getLines().add(lv);
        }

        return v;
    }

    public Page<SupplierInvoiceListItemView> findPage(Long tenantId,
                                                      int page,
                                                      int size,
                                                      String q,
                                                      String status,
                                                      LocalDate dateFrom,
                                                      LocalDate dateTo) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("invoiceDate"), Sort.Order.desc("id"))
        );
        Specification<SupplierInvoice> spec = Specification.where(byTenant(tenantId))
                .and(bySearch(q))
                .and(byStatus(status))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return supplierInvoiceRepository.findAll(spec, pageable).map(this::toListItemView);

    }

    private Specification<SupplierInvoice> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    private Specification<SupplierInvoice> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("invoiceNumber")), like);
    }

    private Specification<SupplierInvoice> byStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<SupplierInvoice> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("invoiceDate"), dateFrom);
    }

    private Specification<SupplierInvoice> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("invoiceDate"), dateTo);
    }

    private SupplierInvoiceListItemView toListItemView(SupplierInvoice invoice) {

        SupplierInvoiceListItemView v = new SupplierInvoiceListItemView();
        v.setId(invoice.getId());
        v.setInvoiceNumber(invoice.getInvoiceNumber());
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(invoice.getTenantId(), invoice.getSupplierId())
                        .map(Supplier::getName)
                        .orElse("-")
        );
        v.setFormattedInvoiceDate(PdfFormatUtils.formatDate(invoice.getInvoiceDate()));
        v.setStatus(invoice.getStatus());
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(invoice.getTotalAmount()));
        v.setCurrencyCode(invoice.getCurrencyCode());
        return v;

    }

}