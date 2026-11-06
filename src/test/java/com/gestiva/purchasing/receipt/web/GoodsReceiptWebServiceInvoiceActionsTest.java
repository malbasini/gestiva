package com.gestiva.purchasing.receipt.web;

import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.item.repository.ItemRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptWebServiceInvoiceActionsTest {

    private GoodsReceiptRepository receipts;
    private GoodsReceiptLineRepository receiptLines;
    private PurchaseOrderRepository orders;
    private PurchaseOrderLineRepository orderLines;
    private SupplierInvoiceRepository invoices;
    private CompanyContext companyContext;
    private MockEnvironment environment;
    private GoodsReceiptWebService service;

    private static final String GENERAL =
            "gestiva.purchasing.supplier-invoice.enabled";
    private static final String PARTIAL =
            "gestiva.purchasing.supplier-invoice.partial.enabled";

    @BeforeEach
    void setup() {
        receipts = mock(GoodsReceiptRepository.class);
        receiptLines = mock(GoodsReceiptLineRepository.class);
        orders = mock(PurchaseOrderRepository.class);
        orderLines = mock(PurchaseOrderLineRepository.class);
        invoices = mock(SupplierInvoiceRepository.class);
        companyContext = mock(CompanyContext.class);
        environment = new MockEnvironment();

        service = new GoodsReceiptWebService(
                receipts,
                receiptLines,
                mock(ItemRepository.class),
                mock(SupplierRepository.class),
                orders,
                orderLines,
                invoices,
                companyContext
        );

        ReflectionTestUtils.setField(
                service, "environment", environment
        );

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(1L);

        GoodsReceipt receipt = mock(GoodsReceipt.class);
        when(receipt.getId()).thenReturn(302L);
        when(receipt.getCompanyId()).thenReturn(1L);
        when(receipt.getPurchaseOrderId()).thenReturn(100L);
        when(receipt.getSupplierId()).thenReturn(40L);
        when(receipt.getReceiptNumber()).thenReturn("GR-00302");
        when(receipt.getReceiptDate())
                .thenReturn(LocalDate.of(2026, 11, 6));

        when(receipts.findByTenantIdAndCompanyIdAndId(
                2L, 1L, 302L
        )).thenReturn(Optional.of(receipt));

        when(invoices.findFirstByTenantIdAndCompanyIdAndGoodsReceiptId(
                2L, 1L, 302L
        )).thenReturn(Optional.empty());

        PurchaseOrder order = mock(PurchaseOrder.class);
        when(order.getId()).thenReturn(100L);
        when(order.getStatus()).thenReturn("CONFIRMED");
        when(order.getSupplierId()).thenReturn(40L);

        when(orders.findByTenantIdAndCompanyIdAndId(
                2L, 1L, 100L
        )).thenReturn(Optional.of(order));

        PurchaseOrderLine poLine = mock(PurchaseOrderLine.class);
        when(poLine.getId()).thenReturn(200L);
        when(poLine.getItemId()).thenReturn(10L);
        when(poLine.getUomId()).thenReturn(9L);
        when(poLine.getQuantity())
                .thenReturn(new BigDecimal("2"));
        when(poLine.getConversionFactor())
                .thenReturn(new BigDecimal("10"));

        when(orderLines.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                2L, 100L
        )).thenReturn(List.of(poLine));

        received("20.000");
        enableFlags(true, true);
        environment.setActiveProfiles("test");
        role("ADMIN");
    }

    private void received(String quantity) {
        GoodsReceiptLine line = mock(GoodsReceiptLine.class);
        when(line.getPurchaseOrderLineId()).thenReturn(200L);
        when(line.getItemId()).thenReturn(10L);
        when(line.getQuantityReceived())
                .thenReturn(new BigDecimal(quantity));

        when(receiptLines
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        2L, 1L, 302L
                ))
                .thenReturn(List.of(line));
    }

    private void enableFlags(boolean general, boolean partial) {
        environment.setProperty(GENERAL, Boolean.toString(general));
        environment.setProperty(PARTIAL, Boolean.toString(partial));
    }

    private void role(String role) {
        var authentication = new UsernamePasswordAuthenticationToken(
                "test-user",
                "not-a-password",
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private GoodsReceiptDetailView detail() {
        return service.getDetail(2L, 302L);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void completeReceiptShowsTraditionalOnly() {
        var view = detail();

        assertTrue(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
        assertTrue(view.isCanCreateSupplierInvoice());
    }

    @Test
    void partialReceiptShowsPartialOnly() {
        received("8.000");

        var view = detail();

        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertTrue(view.isCanCreatePartialSupplierInvoice());
        assertFalse(view.isCanCreateSupplierInvoice());
    }

    @Test
    void partialReceiptWithDisabledFlagShowsNothing() {
        received("8.000");
        enableFlags(true, false);

        var view = detail();

        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
        assertFalse(view.isCanCreateSupplierInvoice());
    }

    @Test
    void excessQuantityShowsNothing() {
        received("21.000");

        var view = detail();

        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
        assertFalse(view.isCanCreateSupplierInvoice());
    }

    @Test
    void alreadyInvoicedReceiptShowsNothing() {
        SupplierInvoice invoice = mock(SupplierInvoice.class);
        when(invoice.getId()).thenReturn(501L);
        when(invoice.getInvoiceNumber()).thenReturn("SI-00501");

        when(invoices.findFirstByTenantIdAndCompanyIdAndGoodsReceiptId(
                2L, 1L, 302L
        )).thenReturn(Optional.of(invoice));

        var view = detail();

        assertTrue(view.isHasSupplierInvoice());
        assertEquals(501L, view.getSupplierInvoiceId());
        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
        assertFalse(view.isCanCreateSupplierInvoice());

        verifyNoInteractions(orderLines);
    }

    @Test
    void warehouseRoleShowsNothing() {
        received("8.000");
        role("WAREHOUSE");

        var view = detail();

        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
        assertFalse(view.isCanCreateSupplierInvoice());

        verifyNoInteractions(orderLines);
    }

    @Test
    void productionProfileShowsNothingEvenWithFlags() {
        environment.setActiveProfiles("prod");

        var view = detail();

        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
        assertFalse(view.isCanCreateSupplierInvoice());

        verifyNoInteractions(orderLines);
    }

    @Test
    void unconfirmedOrderShowsNothing() {
        PurchaseOrder order = mock(PurchaseOrder.class);
        when(order.getStatus()).thenReturn("DRAFT");

        when(orders.findByTenantIdAndCompanyIdAndId(
                2L, 1L, 100L
        )).thenReturn(Optional.of(order));

        var view = detail();

        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
        assertFalse(view.isCanCreateSupplierInvoice());

        verifyNoInteractions(orderLines);
    }
}
