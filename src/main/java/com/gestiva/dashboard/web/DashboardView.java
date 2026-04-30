package com.gestiva.dashboard.web;

import java.util.List;

public class DashboardView {

    private long customersCount;
    private long quotesCount;
    private long ordersCount;

    private String formattedQuotesTotal;
    private String formattedOrdersTotal;

    private List<DashboardQuoteItemView> latestQuotes;
    private List<DashboardOrderItemView> latestOrders;

    public long getCustomersCount() {
        return customersCount;
    }

    public void setCustomersCount(long customersCount) {
        this.customersCount = customersCount;
    }

    public long getQuotesCount() {
        return quotesCount;
    }

    public void setQuotesCount(long quotesCount) {
        this.quotesCount = quotesCount;
    }

    public long getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(long ordersCount) {
        this.ordersCount = ordersCount;
    }

    public String getFormattedQuotesTotal() {
        return formattedQuotesTotal;
    }

    public void setFormattedQuotesTotal(String formattedQuotesTotal) {
        this.formattedQuotesTotal = formattedQuotesTotal;
    }

    public String getFormattedOrdersTotal() {
        return formattedOrdersTotal;
    }

    public void setFormattedOrdersTotal(String formattedOrdersTotal) {
        this.formattedOrdersTotal = formattedOrdersTotal;
    }

    public List<DashboardQuoteItemView> getLatestQuotes() {
        return latestQuotes;
    }

    public void setLatestQuotes(List<DashboardQuoteItemView> latestQuotes) {
        this.latestQuotes = latestQuotes;
    }

    public List<DashboardOrderItemView> getLatestOrders() {
        return latestOrders;
    }

    public void setLatestOrders(List<DashboardOrderItemView> latestOrders) {
        this.latestOrders = latestOrders;
    }
}
