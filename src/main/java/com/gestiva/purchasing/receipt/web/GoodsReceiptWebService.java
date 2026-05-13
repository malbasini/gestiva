package com.gestiva.purchasing.receipt.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.invoice.web.SupplierInvoiceListItemView;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
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
public class GoodsReceiptWebService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;



    public GoodsReceiptWebService(GoodsReceiptRepository goodsReceiptRepository,
                                  GoodsReceiptLineRepository goodsReceiptLineRepository,
                                  SupplierRepository supplierRepository,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  SupplierInvoiceRepository supplierInvoiceRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
    }

    public List<GoodsReceiptListItemView> findAll(Long tenantId) {
        var receipts = goodsReceiptRepository.findAll(
                org.springframework.data.jpa.domain.Specification
                        .where((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId)),
                Sort.by(Sort.Direction.DESC, "receiptDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        List<GoodsReceiptListItemView> result = new ArrayList<>();
        for (var receipt : receipts) {
            GoodsReceiptListItemView v = new GoodsReceiptListItemView();
            v.setId(receipt.getId());
            v.setReceiptNumber(receipt.getReceiptNumber());
            v.setFormattedReceiptDate(PdfFormatUtils.formatDate(receipt.getReceiptDate()));
            v.setSupplierName(
                    supplierRepository.findByTenantIdAndId(tenantId, receipt.getSupplierId())
                            .map(s -> s.getName())
                            .orElse("-")
            );
            v.setPurchaseOrderNumber(
                    purchaseOrderRepository.findByTenantIdAndId(tenantId, receipt.getPurchaseOrderId())
                            .map(po -> po.getOrderNumber())
                            .orElse("-")
            );
            result.add(v);
        }
        return result;
    }

    public GoodsReceiptDetailView getDetail(Long tenantId, Long id) {
        var receipt = goodsReceiptRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Ricezione merci non trovata"));

        var lines = goodsReceiptLineRepository.findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(tenantId, id);

        GoodsReceiptDetailView v = new GoodsReceiptDetailView();
        v.setId(receipt.getId());
        v.setReceiptNumber(receipt.getReceiptNumber());
        v.setFormattedReceiptDate(PdfFormatUtils.formatDate(receipt.getReceiptDate()));
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(tenantId, receipt.getSupplierId())
                        .map(s -> s.getName())
                        .orElse("-")
        );
        v.setPurchaseOrderNumber(
                purchaseOrderRepository.findByTenantIdAndId(tenantId, receipt.getPurchaseOrderId())
                        .map(po -> po.getOrderNumber())
                        .orElse("-")
        );
        v.setNotes(receipt.getNotes());
        v.setPurchaseOrderId(receipt.getPurchaseOrderId());

        var invoiceOpt = supplierInvoiceRepository.findFirstByTenantIdAndGoodsReceiptId(tenantId, receipt.getId());
        v.setHasSupplierInvoice(invoiceOpt.isPresent());
        v.setCanCreateSupplierInvoice(invoiceOpt.isEmpty());
        invoiceOpt.ifPresent(invoice -> {
            v.setSupplierInvoiceId(invoice.getId());
            v.setSupplierInvoiceNumber(invoice.getInvoiceNumber());
        });

        for (var line : lines) {
            GoodsReceiptDetailLineView lv = new GoodsReceiptDetailLineView();
            lv.setLineNo(line.getLineNo());
            lv.setDescription(line.getDescription());
            lv.setFormattedQuantityReceived(PdfFormatUtils.formatDecimal(line.getQuantityReceived()));
            v.getLines().add(lv);
        }

        return v;
    }
    public Page<GoodsReceiptListItemView> findPage(Long tenantId,
                                                      int page,
                                                      int size,
                                                      String q,
                                                      LocalDate dateFrom,
                                                      LocalDate dateTo) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("receiptDate"), Sort.Order.desc("id"))
        );
        Specification<GoodsReceipt> spec = Specification.where(byTenant(tenantId))
                .and(bySearch(q))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return goodsReceiptRepository.findAll(spec, pageable).map(this::toListItemView);

    }

    private Specification<GoodsReceipt> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    private Specification<GoodsReceipt> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("receiptNumber")), like);
    }

    private Specification<GoodsReceipt> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("receiptDate"), dateFrom);
    }

    private Specification<GoodsReceipt> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("receiptDate"), dateTo);
    }

    private GoodsReceiptListItemView toListItemView(GoodsReceipt receipt) {

        GoodsReceiptListItemView v = new GoodsReceiptListItemView();
        v.setId(receipt.getId());
        v.setReceiptNumber(receipt.getReceiptNumber());
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(receipt.getTenantId(), receipt.getSupplierId())
                        .map(Supplier::getName)
                        .orElse("-")
        );
        v.setFormattedReceiptDate(PdfFormatUtils.formatDate(receipt.getReceiptDate()));
        v.setPurchaseOrderId(receipt.getPurchaseOrderId());
        v.setPurchaseOrderNumber(
                purchaseOrderRepository.findByTenantIdAndId(receipt.getTenantId(), receipt.getPurchaseOrderId())
                        .map(PurchaseOrder::getOrderNumber)
                        .orElse("-")
        );
        return v;

    }
}