package com.gestiva.dashboard.web;

import java.util.ArrayList;
import java.util.List;

public class DashboardView {

    // KPI principali
    private long openQuotesCount;
    private long openSalesOrdersCount;
    private long receivablesDueCount;
    private long payablesDueCount;

    // Ciclo attivo
    private long deliveryNotesToInvoiceCount;
    private long openSalesInvoicesCount;

    // Ciclo passivo
    private long openPurchaseOrdersCount;
    private long goodsReceiptsToInvoiceCount;
    private long openSupplierInvoicesCount;

    // Contabilità
    private String formattedPeriodRevenue;
    private String formattedPeriodCost;
    private String formattedPeriodResult;

    // IVA
    private String formattedVatSales;
    private String formattedVatPurchases;
    private String formattedVatBalance;

    // Magazzino
    private long lowStockItemsCount;
    private long stockManagedItemsCount;

    // Liste recenti
    private List<DashboardRecentDocumentView> recentSalesDocuments = new ArrayList<>();
    private List<DashboardRecentInventoryMovementView> recentInventoryMovements = new ArrayList<>();

    public long getOpenQuotesCount() {
        return openQuotesCount;
    }

    public void setOpenQuotesCount(long openQuotesCount) {
        this.openQuotesCount = openQuotesCount;
    }

    public long getOpenSalesOrdersCount() {
        return openSalesOrdersCount;
    }

    public void setOpenSalesOrdersCount(long openSalesOrdersCount) {
        this.openSalesOrdersCount = openSalesOrdersCount;
    }

    public long getReceivablesDueCount() {
        return receivablesDueCount;
    }

    public void setReceivablesDueCount(long receivablesDueCount) {
        this.receivablesDueCount = receivablesDueCount;
    }

    public long getPayablesDueCount() {
        return payablesDueCount;
    }

    public void setPayablesDueCount(long payablesDueCount) {
        this.payablesDueCount = payablesDueCount;
    }

    public long getDeliveryNotesToInvoiceCount() {
        return deliveryNotesToInvoiceCount;
    }

    public void setDeliveryNotesToInvoiceCount(long deliveryNotesToInvoiceCount) {
        this.deliveryNotesToInvoiceCount = deliveryNotesToInvoiceCount;
    }

    public long getOpenSalesInvoicesCount() {
        return openSalesInvoicesCount;
    }

    public void setOpenSalesInvoicesCount(long openSalesInvoicesCount) {
        this.openSalesInvoicesCount = openSalesInvoicesCount;
    }

    public long getOpenPurchaseOrdersCount() {
        return openPurchaseOrdersCount;
    }

    public void setOpenPurchaseOrdersCount(long openPurchaseOrdersCount) {
        this.openPurchaseOrdersCount = openPurchaseOrdersCount;
    }

    public long getGoodsReceiptsToInvoiceCount() {
        return goodsReceiptsToInvoiceCount;
    }

    public void setGoodsReceiptsToInvoiceCount(long goodsReceiptsToInvoiceCount) {
        this.goodsReceiptsToInvoiceCount = goodsReceiptsToInvoiceCount;
    }

    public long getOpenSupplierInvoicesCount() {
        return openSupplierInvoicesCount;
    }

    public void setOpenSupplierInvoicesCount(long openSupplierInvoicesCount) {
        this.openSupplierInvoicesCount = openSupplierInvoicesCount;
    }

    public String getFormattedPeriodRevenue() {
        return formattedPeriodRevenue;
    }

    public void setFormattedPeriodRevenue(String formattedPeriodRevenue) {
        this.formattedPeriodRevenue = formattedPeriodRevenue;
    }

    public String getFormattedPeriodCost() {
        return formattedPeriodCost;
    }

    public void setFormattedPeriodCost(String formattedPeriodCost) {
        this.formattedPeriodCost = formattedPeriodCost;
    }

    public String getFormattedPeriodResult() {
        return formattedPeriodResult;
    }

    public void setFormattedPeriodResult(String formattedPeriodResult) {
        this.formattedPeriodResult = formattedPeriodResult;
    }

    public String getFormattedVatSales() {
        return formattedVatSales;
    }

    public void setFormattedVatSales(String formattedVatSales) {
        this.formattedVatSales = formattedVatSales;
    }

    public String getFormattedVatPurchases() {
        return formattedVatPurchases;
    }

    public void setFormattedVatPurchases(String formattedVatPurchases) {
        this.formattedVatPurchases = formattedVatPurchases;
    }

    public String getFormattedVatBalance() {
        return formattedVatBalance;
    }

    public void setFormattedVatBalance(String formattedVatBalance) {
        this.formattedVatBalance = formattedVatBalance;
    }

    public long getLowStockItemsCount() {
        return lowStockItemsCount;
    }

    public void setLowStockItemsCount(long lowStockItemsCount) {
        this.lowStockItemsCount = lowStockItemsCount;
    }

    public long getStockManagedItemsCount() {
        return stockManagedItemsCount;
    }

    public void setStockManagedItemsCount(long stockManagedItemsCount) {
        this.stockManagedItemsCount = stockManagedItemsCount;
    }

    public List<DashboardRecentDocumentView> getRecentSalesDocuments() {
        return recentSalesDocuments;
    }

    public void setRecentSalesDocuments(List<DashboardRecentDocumentView> recentSalesDocuments) {
        this.recentSalesDocuments = recentSalesDocuments;
    }

    public List<DashboardRecentInventoryMovementView> getRecentInventoryMovements() {
        return recentInventoryMovements;
    }

    public void setRecentInventoryMovements(List<DashboardRecentInventoryMovementView> recentInventoryMovements) {
        this.recentInventoryMovements = recentInventoryMovements;
    }
}