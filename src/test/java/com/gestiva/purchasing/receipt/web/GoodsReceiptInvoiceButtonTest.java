package com.gestiva.purchasing.receipt.web;

import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptInvoiceButtonTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long RECEIPT_ID = 10L;
    private static final Long ORDER_ID = 20L;

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private GoodsReceiptLineRepository goodsReceiptLineRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierInvoiceRepository supplierInvoiceRepository;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private Environment environment;

    @InjectMocks
    private GoodsReceiptWebService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "environment", environment);

        GoodsReceipt receipt = new GoodsReceipt();
        ReflectionTestUtils.setField(receipt, "id", RECEIPT_ID);

        receipt.setTenantId(TENANT_ID);
        receipt.setCompanyId(COMPANY_ID);
        receipt.setPurchaseOrderId(ORDER_ID);
        receipt.setSupplierId(30L);
        receipt.setReceiptNumber("GR-JUNIT-001");
        receipt.setReceiptDate(LocalDate.of(2026, 10, 31));

        lenient().when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        lenient().when(goodsReceiptRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                ))
                .thenReturn(Optional.of(receipt));

        lenient().when(goodsReceiptLineRepository
                .findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT_ID, RECEIPT_ID
                ))
                .thenReturn(List.of());

        lenient().when(supplierRepository.findByTenantIdAndId(
                TENANT_ID, 30L
        )).thenReturn(Optional.empty());

        lenient().when(supplierInvoiceRepository
                .findFirstByTenantIdAndCompanyIdAndGoodsReceiptId(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                ))
                .thenReturn(Optional.empty());
    }

    private void configureEnvironment(String profile, boolean enabled) {
        when(environment.getActiveProfiles())
                .thenReturn(new String[]{profile});

        lenient().when(environment.getProperty(
                "gestiva.purchasing.supplier-invoice.enabled",
                Boolean.class,
                false
        )).thenReturn(enabled);
    }

    private void configureOrder(String status) {
        PurchaseOrder order = new PurchaseOrder();
        order.setStatus(status);

        when(purchaseOrderRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, ORDER_ID
                ))
                .thenReturn(Optional.of(order));
    }

    private GoodsReceiptDetailView loadDetail() {
        return service.getDetail(TENANT_ID, RECEIPT_ID);
    }

    @Test
    void shouldHideButtonWhenPropertyDisabled() {
        configureEnvironment("test", false);

        assertFalse(loadDetail().isCanCreateSupplierInvoice());
    }

    @Test
    void shouldHideButtonOutsideTestProfile() {
        configureEnvironment("dev", true);

        assertFalse(loadDetail().isCanCreateSupplierInvoice());
    }

    @Test
    void shouldShowButtonForConfirmedOrderWithoutInvoice() {
        configureEnvironment("test", true);
        configureOrder("CONFIRMED");

        assertTrue(loadDetail().isCanCreateSupplierInvoice());
    }

    @Test
    void shouldHideButtonForDraftOrder() {
        configureEnvironment("test", true);
        configureOrder("DRAFT");

        assertFalse(loadDetail().isCanCreateSupplierInvoice());
    }

    @Test
    void shouldHideButtonWhenInvoiceAlreadyExists() {
        configureEnvironment("test", true);

        SupplierInvoice existing = new SupplierInvoice();

        when(supplierInvoiceRepository
                .findFirstByTenantIdAndCompanyIdAndGoodsReceiptId(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                ))
                .thenReturn(Optional.of(existing));

        assertFalse(loadDetail().isCanCreateSupplierInvoice());
    }
}
