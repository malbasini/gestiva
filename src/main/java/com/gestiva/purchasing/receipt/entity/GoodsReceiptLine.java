package com.gestiva.purchasing.receipt.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
@Table(name = "goods_receipt_line")
public class GoodsReceiptLine extends TenantAwareEntity {

    @Column(name = "goods_receipt_id", nullable = false)
    private Long goodsReceiptId;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "purchase_order_line_id", nullable = false)
    private Long purchaseOrderLineId;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "quantity_received", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantityReceived;

    @Column(name = "unit_cost", precision = 15, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 15, scale = 4)
    private BigDecimal totalCost;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false, insertable = false, updatable = false)
    private GoodsReceipt goodsReceipt;

    public Long getGoodsReceiptId() {
        return goodsReceiptId;
    }

    public void setGoodsReceiptId(Long goodsReceiptId) {
        this.goodsReceiptId = goodsReceiptId;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public Long getPurchaseOrderLineId() {
        return purchaseOrderLineId;
    }

    public void setPurchaseOrderLineId(Long purchaseOrderLineId) {
        this.purchaseOrderLineId = purchaseOrderLineId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(BigDecimal quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public GoodsReceipt getGoodsReceipt() {
        return goodsReceipt;
    }

    public void setGoodsReceipt(GoodsReceipt goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
    }
}
