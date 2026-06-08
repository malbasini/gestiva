package com.gestiva.security.tenant.entity;

import com.gestiva.common.model.BaseEntity;
import com.gestiva.inventory.valuation.model.InventoryValuationMethod;
import jakarta.persistence.*;

@Entity
@Table(name = "tenant")
public class Tenant extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120, unique = true)
    private String slug;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // ACTIVE, TRIALING, SUSPENDED

    @Column(name = "default_locale", nullable = false, length = 10)
    private String defaultLocale; // it, en

    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency; // EUR

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_valuation_method", nullable = false, length = 20)
    private InventoryValuationMethod inventoryValuationMethod = InventoryValuationMethod.LIFO;

    @Column(name = "subscription_active", nullable = false)
    private boolean subscriptionActive;

    @Column(name = "subscription_status", length = 30)
    private String subscriptionStatus; // PENDING, ACTIVE, SUSPENDED, CANCELLED

    @Column(name = "subscription_plan", length = 50)
    private String subscriptionPlan; // STARTER, PROFESSIONAL

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public InventoryValuationMethod getInventoryValuationMethod() {
        return inventoryValuationMethod;
    }

    public void setInventoryValuationMethod(InventoryValuationMethod inventoryValuationMethod) {
        this.inventoryValuationMethod = inventoryValuationMethod;
    }

    public boolean isSubscriptionActive() {
        return subscriptionActive;
    }

    public void setSubscriptionActive(boolean subscriptionActive) {
        this.subscriptionActive = subscriptionActive;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }
}
