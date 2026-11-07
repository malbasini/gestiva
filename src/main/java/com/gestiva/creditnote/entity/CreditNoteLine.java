package com.gestiva.creditnote.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
@Table(name = "credit_note_line")
public class CreditNoteLine extends TenantAwareEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "credit_note_id", nullable = false)
    private Long creditNoteId;

    @Column(name = "original_invoice_line_id")
    private Long originalInvoiceLineId;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "quantity", precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "uom_id")
    private Long uomId;

    @Column(name = "uom_code", length = 20)
    private String uomCode;

    @Column(name = "base_uom_code", length = 20)
    private String baseUomCode;

    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_pct", precision = 7, scale = 2)
    private BigDecimal discountPct;

    @Column(name = "tax_pct", nullable = false, precision = 7, scale = 2)
    private BigDecimal taxPct;

    @Column(name = "line_subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineSubtotal;

    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "line_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotal;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getCreditNoteId() {
        return creditNoteId;
    }

    public void setCreditNoteId(Long creditNoteId) {
        this.creditNoteId = creditNoteId;
    }

    public Long getOriginalInvoiceLineId() {
        return originalInvoiceLineId;
    }

    public void setOriginalInvoiceLineId(Long originalInvoiceLineId) {
        this.originalInvoiceLineId = originalInvoiceLineId;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Long getUomId() {
        return uomId;
    }

    public void setUomId(Long uomId) {
        this.uomId = uomId;
    }

    public String getUomCode() {
        return uomCode;
    }

    public void setUomCode(String uomCode) {
        this.uomCode = uomCode;
    }

    public String getBaseUomCode() {
        return baseUomCode;
    }

    public void setBaseUomCode(String baseUomCode) {
        this.baseUomCode = baseUomCode;
    }

    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }

    public void setConversionFactor(BigDecimal conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountPct() {
        return discountPct;
    }

    public void setDiscountPct(BigDecimal discountPct) {
        this.discountPct = discountPct;
    }

    public BigDecimal getTaxPct() {
        return taxPct;
    }

    public void setTaxPct(BigDecimal taxPct) {
        this.taxPct = taxPct;
    }

    public BigDecimal getLineSubtotal() {
        return lineSubtotal;
    }

    public void setLineSubtotal(BigDecimal lineSubtotal) {
        this.lineSubtotal = lineSubtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
