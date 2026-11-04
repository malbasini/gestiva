package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialPlanningTransactionIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private GoodsReceiptPartialPlanningService planning;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager manager;

    @MockitoBean
    private PurchaseOrderRepository orders;

    @MockitoBean
    private PurchaseOrderLineRepository lines;

    @MockitoBean
    private GoodsReceiptPartialHistoryReader history;

    @MockitoBean
    private CompanyContext companies;

    private void verifyDatabase() throws Exception {
        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", connection.getCatalog());

            String username =
                    connection.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@")
            );
        }
    }

    private GoodsReceipt header() {
        GoodsReceipt receipt = new GoodsReceipt();

        receipt.setTenantId(2L);
        receipt.setCompanyId(1L);
        receipt.setPurchaseOrderId(31L);
        receipt.setSupplierId(12L);
        receipt.setWarehouseId(7L);
        receipt.setReceiptNumber("GR-TEST-1905");
        receipt.setReceiptDate(LocalDate.of(2026, 11, 4));

        return receipt;
    }

    private record TransactionState(
            Long connectionId,
            String transactionName,
            boolean active
    ) {
    }

    private TransactionState state() {
        Long connectionId = jdbc.queryForObject(
                "SELECT CONNECTION_ID()", Long.class
        );

        return new TransactionState(
                connectionId,
                TransactionSynchronizationManager
                        .getCurrentTransactionName(),
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );
    }

    @Test
    void planningAndSnapshotShareTheSameTransaction()
            throws Exception {

        verifyDatabase();

        PurchaseOrder order = mock(PurchaseOrder.class);
        when(order.getId()).thenReturn(31L);
        when(order.getTenantId()).thenReturn(2L);
        when(order.getCompanyId()).thenReturn(1L);
        when(order.getSupplierId()).thenReturn(12L);
        when(order.getStatus()).thenReturn("CONFIRMED");

        PurchaseOrderLine line = mock(PurchaseOrderLine.class);
        when(line.getId()).thenReturn(101L);
        when(line.getTenantId()).thenReturn(2L);
        when(line.getPurchaseOrderId()).thenReturn(31L);
        when(line.getUomId()).thenReturn(5L);
        when(line.getQuantity())
                .thenReturn(new BigDecimal("2.000"));
        when(line.getConversionFactor())
                .thenReturn(new BigDecimal("10.000000"));
        when(line.getLineSubtotal())
                .thenReturn(new BigDecimal("144.00"));
        when(line.getItemId()).thenReturn(50L);
        when(line.getDescription()).thenReturn("Articolo test");

        var orderLines = List.of(line);

        AtomicReference<TransactionState> atOrder =
                new AtomicReference<>();

        AtomicReference<TransactionState> atHistory =
                new AtomicReference<>();

        when(companies.requireCurrentCompanyId()).thenReturn(1L);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 31L
        )).thenAnswer(invocation -> {
            atOrder.set(state());
            return Optional.of(order);
        });

        when(lines.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                2L, 31L
        )).thenReturn(orderLines);

        when(history.read(2L, 1L, 31L, orderLines))
                .thenAnswer(invocation -> {
                    atHistory.set(state());
                    return Map.of(
                            101L, new BigDecimal("8.000")
                    );
                });

        var plan = planning.plan(
                header(),
                Map.of(101L, new BigDecimal("12.000"))
        );

        assertNotNull(atOrder.get());
        assertNotNull(atHistory.get());

        assertTrue(atOrder.get().active());
        assertTrue(atHistory.get().active());

        assertNotNull(atOrder.get().connectionId());
        assertEquals(
                atOrder.get().connectionId(),
                atHistory.get().connectionId()
        );

        assertNotNull(atOrder.get().transactionName());
        assertEquals(
                atOrder.get().transactionName(),
                atHistory.get().transactionName()
        );

        assertTrue(
                atOrder.get().transactionName()
                        .contains("GoodsReceiptPartialPlanningService.plan")
        );

        var resultLines = plan.buildLines(500L);

        assertEquals(1, resultLines.size());
        assertEquals(
                0,
                new BigDecimal("86.4000").compareTo(
                        resultLines.get(0).getTotalCost()
                )
        );

        var calls = inOrder(orders, lines, history);

        calls.verify(orders)
                .findLockedByTenantIdAndCompanyIdAndId(
                        2L, 1L, 31L
                );
        calls.verify(lines)
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        2L, 31L
                );
        calls.verify(history)
                .read(2L, 1L, 31L, orderLines);

        verify(history, times(1))
                .read(2L, 1L, 31L, orderLines);

        System.out.println(
                "PLANNING MYSQL: stessa connessione e transazione"
        );
        System.out.println(
                "PLANNING MYSQL: blocco ordine prima dello storico"
        );
        System.out.println(
                "PLANNING MYSQL: costo FIFO = 86.4000"
        );
    }

    @Test
    void rejectsOuterRepeatableReadBeforeDocumentLookup()
            throws Exception {

        verifyDatabase();

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        tx.setReadOnly(false);
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_REPEATABLE_READ
        );

        tx.execute(status -> {

            BusinessException error = assertThrows(
                    BusinessException.class,
                    () -> planning.plan(
                            header(),
                            Map.of(
                                    101L,
                                    new BigDecimal("12.000")
                            )
                    )
            );

            assertTrue(
                    error.getMessage().contains("READ COMMITTED")
            );

            verifyNoInteractions(
                    companies, orders, lines, history
            );

            status.setRollbackOnly();
            return null;
        });

        System.out.println(
                "PLANNING MYSQL: REPEATABLE READ rifiutato"
        );
        System.out.println(
                "PLANNING MYSQL: nessuna lettura documentale"
        );
    }
}
