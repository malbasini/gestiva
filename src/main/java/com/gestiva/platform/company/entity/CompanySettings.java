package com.gestiva.platform.company.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "company_settings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_settings_company",
                        columnNames = {"company_id"}
                )
        }
)
public class CompanySettings extends TenantAwareEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_company_settings_company")
    )
    private Company company;

    @Column(name = "default_vat_pct", precision = 5, scale = 2)
    private BigDecimal defaultVatPct;

    @Column(name = "default_customer_due_days")
    private Integer defaultCustomerDueDays;

    @Column(name = "default_supplier_due_days")
    private Integer defaultSupplierDueDays;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public BigDecimal getDefaultVatPct() {
        return defaultVatPct;
    }

    public void setDefaultVatPct(BigDecimal defaultVatPct) {
        this.defaultVatPct = defaultVatPct;
    }

    public Integer getDefaultCustomerDueDays() {
        return defaultCustomerDueDays;
    }

    public void setDefaultCustomerDueDays(Integer defaultCustomerDueDays) {
        this.defaultCustomerDueDays = defaultCustomerDueDays;
    }

    public Integer getDefaultSupplierDueDays() {
        return defaultSupplierDueDays;
    }

    public void setDefaultSupplierDueDays(Integer defaultSupplierDueDays) {
        this.defaultSupplierDueDays = defaultSupplierDueDays;
    }
}