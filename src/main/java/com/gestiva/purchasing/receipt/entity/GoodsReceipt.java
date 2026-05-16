package com.gestiva.purchasing.receipt.entity;

import com.gestiva.common.model.TenantAwareEntity;
import com.gestiva.logistics.ddt.entity.DeliveryNoteLine;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "goods_receipt",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_goods_receipt_tenant_number", columnNames = {"tenant_id", "receipt_number"})
        }
)
public class GoodsReceipt extends TenantAwareEntity {

    @Column(name = "receipt_number", nullable = false, length = 50)
    private String receiptNumber;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptLine> lines = new ArrayList<>();

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDate receiptDate) {
        this.receiptDate = receiptDate;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<GoodsReceiptLine> getLines() {
        return lines;
    }

    public void setLines(List<GoodsReceiptLine> lines) {
        this.lines = lines;
    }
}