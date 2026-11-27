package com.gestiva.purchasing.receipt.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "goods_receipt_line_lot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_gr_line_lot",
                        columnNames = {"goods_receipt_line_id", "lot_id"}
                ),
                @UniqueConstraint(
                        name = "uk_gr_line_lot_movement",
                        columnNames = {"movement_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_gr_line_lot_trace",
                        columnList = "tenant_id,item_id,lot_id,company_id"
                )
        }
)
public class GoodsReceiptLineLot extends TenantAwareEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "goods_receipt_line_id", nullable = false)
    private Long goodsReceiptLineId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "total_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal totalCost;

    @Column(name = "movement_id")
    private Long movementId;

    public GoodsReceiptLineLot() {
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getGoodsReceiptLineId() {
        return goodsReceiptLineId;
    }

    public void setGoodsReceiptLineId(Long goodsReceiptLineId) {
        this.goodsReceiptLineId = goodsReceiptLineId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
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

    public Long getMovementId() {
        return movementId;
    }

    public void setMovementId(Long movementId) {
        this.movementId = movementId;
    }
}
