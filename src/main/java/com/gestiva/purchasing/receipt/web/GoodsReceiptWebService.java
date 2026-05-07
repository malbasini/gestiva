package com.gestiva.purchasing.receipt.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GoodsReceiptWebService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public GoodsReceiptWebService(GoodsReceiptRepository goodsReceiptRepository,
                                  GoodsReceiptLineRepository goodsReceiptLineRepository,
                                  SupplierRepository supplierRepository,
                                  PurchaseOrderRepository purchaseOrderRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
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
        for (var line : lines) {
            GoodsReceiptDetailLineView lv = new GoodsReceiptDetailLineView();
            lv.setLineNo(line.getLineNo());
            lv.setDescription(line.getDescription());
            lv.setFormattedQuantityReceived(PdfFormatUtils.formatDecimal(line.getQuantityReceived()));
            v.getLines().add(lv);
        }

        return v;
    }
}