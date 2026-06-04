package com.gestiva.dashboard.web;

import com.gestiva.accounting.v2.report.web.IncomeStatementView;
import com.gestiva.accounting.v2.report.web.IncomeStatementWebService;
import com.gestiva.accounting.vat.web.VatSettlementView;
import com.gestiva.accounting.vat.web.VatSettlementWebService;
import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.movement.entity.InventoryMovement;
import com.gestiva.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardWebService {

    private final QuoteRepository quoteRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final ItemRepository itemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final IncomeStatementWebService incomeStatementWebService;
    private final VatSettlementWebService vatSettlementWebService;

    public DashboardWebService(QuoteRepository quoteRepository,
                               SalesOrderRepository salesOrderRepository,
                               DeliveryNoteRepository deliveryNoteRepository,
                               InvoiceRepository invoiceRepository,
                               PurchaseOrderRepository purchaseOrderRepository,
                               SupplierInvoiceRepository supplierInvoiceRepository,
                               ItemRepository itemRepository,
                               InventoryMovementRepository inventoryMovementRepository,
                               IncomeStatementWebService incomeStatementWebService,
                               VatSettlementWebService vatSettlementWebService) {
        this.quoteRepository = quoteRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.itemRepository = itemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.incomeStatementWebService = incomeStatementWebService;
        this.vatSettlementWebService = vatSettlementWebService;
    }

    public DashboardView build(Long tenantId) {
        DashboardView view = new DashboardView();

        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = LocalDate.now();

        // =========================================================
        // KPI PRINCIPALI
        // =========================================================
        view.setOpenQuotesCount(readOpenQuotesCount(tenantId));
        view.setOpenSalesOrdersCount(readOpenSalesOrdersCount(tenantId));

        // TODO: agganciare PaymentDueRepository
        view.setReceivablesDueCount(0L);
        view.setPayablesDueCount(0L);

        // =========================================================
        // CICLO ATTIVO
        // =========================================================
        view.setDeliveryNotesToInvoiceCount(readDeliveryNotesToInvoiceCount(tenantId));
        view.setOpenSalesInvoicesCount(readOpenSalesInvoicesCount(tenantId));
        mapRecentSalesDocuments(view, tenantId);

        // =========================================================
        // CICLO PASSIVO
        // =========================================================
        view.setOpenPurchaseOrdersCount(readOpenPurchaseOrdersCount(tenantId));

        // TODO: agganciare GoodsReceiptRepository / logica reale
        view.setGoodsReceiptsToInvoiceCount(0L);

        view.setOpenSupplierInvoicesCount(readOpenSupplierInvoicesCount(tenantId));

        // =========================================================
        // CONTABILITÀ
        // =========================================================
        IncomeStatementView income = incomeStatementWebService.build(tenantId, from, to);
        view.setFormattedPeriodRevenue(defaultString(income.getFormattedTotalRevenue(), "0,00"));
        view.setFormattedPeriodCost(defaultString(income.getFormattedTotalCost(), "0,00"));
        view.setFormattedPeriodResult(defaultString(income.getFormattedPeriodResult(), "0,00"));

        // =========================================================
        // IVA
        // =========================================================
        VatSettlementView vat = vatSettlementWebService.calculateSettlement(tenantId, from, to);
        view.setFormattedVatSales(defaultString(vat.getFormattedSalesTaxAmount(), "0,00"));
        view.setFormattedVatPurchases(defaultString(vat.getFormattedPurchaseTaxAmount(), "0,00"));
        view.setFormattedVatBalance(defaultString(vat.getFormattedVatBalance(), "0,00"));

        // =========================================================
        // MAGAZZINO
        // =========================================================
        view.setStockManagedItemsCount(readStockManagedItemsCount(tenantId));

        // TODO: calcolare con minStockLevel + saldo reale
        view.setLowStockItemsCount(0L);

        mapRecentInventoryMovements(view, tenantId);

        return view;
    }

    private long readOpenQuotesCount(Long tenantId) {
        return quoteRepository.countByTenantIdAndStatusIn(
                tenantId,
                List.of("DRAFT", "SENT")
        );
    }

    private long readOpenSalesOrdersCount(Long tenantId) {
        return salesOrderRepository.countByTenantIdAndStatusIn(
                tenantId,
                List.of("DRAFT", "CONFIRMED")
        );
    }

    private long readDeliveryNotesToInvoiceCount(Long tenantId) {
        return deliveryNoteRepository.countToInvoiceByTenantIdAndStatusIn(
                tenantId,
                List.of("ISSUED", "CONFIRMED")
        );
    }

    private long readOpenSalesInvoicesCount(Long tenantId) {
        return invoiceRepository.countByTenantIdAndStatusIn(
                tenantId,
                List.of("ISSUED")
        );
    }

    private long readOpenPurchaseOrdersCount(Long tenantId) {
        return purchaseOrderRepository.countByTenantIdAndStatusIn(
                tenantId,
                List.of("DRAFT", "CONFIRMED")
        );
    }

    private long readOpenSupplierInvoicesCount(Long tenantId) {
        return supplierInvoiceRepository.countByTenantIdAndStatusIn(
                tenantId,
                List.of("REGISTERED")
        );
    }

    private long readStockManagedItemsCount(Long tenantId) {
        return itemRepository.countByTenantIdAndTrackStockTrue(tenantId);
    }

    private void mapRecentSalesDocuments(DashboardView view, Long tenantId) {
        List<Invoice> recentInvoices = invoiceRepository.findTop5ByTenantIdOrderByInvoiceDateDescIdDesc(tenantId);

        List<DashboardRecentDocumentView> docs = recentInvoices.stream().map(inv -> {
            DashboardRecentDocumentView d = new DashboardRecentDocumentView();
            d.setDate(formatDate(inv.getInvoiceDate()));
            d.setTypeLabel("Fattura cliente");
            d.setNumber(inv.getInvoiceNumber());
            d.setCounterparty(resolveInvoiceCounterparty(inv));
            d.setAmount(formatMoney(inv.getTotalAmount()));
            d.setDetailUrl("/invoices/" + inv.getId());
            return d;
        }).toList();

        view.setRecentSalesDocuments(docs);
    }

    private void mapRecentInventoryMovements(DashboardView view, Long tenantId) {
        List<InventoryMovement> recent = inventoryMovementRepository
                .findTop10ByTenantIdOrderByMovementDateDescIdDesc(tenantId);

        List<DashboardRecentInventoryMovementView> rows = recent.stream().map(m -> {
            DashboardRecentInventoryMovementView r = new DashboardRecentInventoryMovementView();
            r.setDate(formatDate(m.getMovementDate()));
            r.setItemCode(resolveItemCode(tenantId, m.getItemId()));
            r.setItemName(resolveItemName(tenantId, m.getItemId()));
            r.setMovementTypeLabel(toMovementTypeLabel(m.getMovementType()));
            r.setQuantity(formatQuantity(m.getQuantity()));
            return r;
        }).toList();

        view.setRecentInventoryMovements(rows);
    }

    private String resolveInvoiceCounterparty(Invoice invoice) {
        // Adatta questo metodo in base alla tua entity Invoice.
        // Se hai invoice.getCustomerName() usa quello direttamente.
        // Se hai customerId devi agganciare CustomerRepository.
        try {
            var method = invoice.getClass().getMethod("getCustomerName");
            Object value = method.invoke(invoice);
            return value != null ? value.toString() : "-";
        } catch (Exception ex) {
            return "-";
        }
    }

    private String resolveItemCode(Long tenantId, Long itemId) {
        return itemRepository.findByTenantIdAndId(tenantId, itemId)
                .map(i -> i.getCode())
                .orElse("-");
    }

    private String resolveItemName(Long tenantId, Long itemId) {
        return itemRepository.findByTenantIdAndId(tenantId, itemId)
                .map(i -> i.getName())
                .orElse("-");
    }

    private String toMovementTypeLabel(String movementType) {
        if (movementType == null) return "";
        return switch (movementType.toUpperCase()) {
            case "IN" -> "Carico";
            case "OUT" -> "Scarico";
            case "ADJUSTMENT_IN" -> "Rettifica +";
            case "ADJUSTMENT_OUT" -> "Rettifica -";
            default -> movementType;
        };
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return String.format("%02d/%02d/%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0,00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private String formatQuantity(BigDecimal value) {
        if (value == null) {
            return "0,000";
        }
        return value.setScale(3, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}