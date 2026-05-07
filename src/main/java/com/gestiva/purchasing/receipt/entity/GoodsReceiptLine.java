package com.gestiva.purchasing.receipt.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
}
