package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialSnapshotIsolationIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private GoodsReceiptPartialSnapshotService service;

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

            assertEquals(
                    "gestiva_test",
                    connection.getCatalog()
            );

            String username =
                    connection.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@"),
                    "Utente MySQL non autorizzato."
            );
        }
    }

    private int actualIsolation() {
        Integer value = jdbc.execute(
                (ConnectionCallback<Integer>)
                Connection::getTransactionIsolation
        );
        assertNotNull(value);
        return value;
    }

    @Test
    void directProxyUsesReadCommittedAndReadsAfterLock()
            throws Exception {

        verifyDatabase();

        var order = mock(PurchaseOrder.class);
        when(order.getStatus()).thenReturn("CONFIRMED");

        var line = mock(PurchaseOrderLine.class);
        when(line.getId()).thenReturn(101L);
        when(line.getQuantity())
                .thenReturn(new BigDecimal("2.000"));
        when(line.getConversionFactor())
                .thenReturn(new BigDecimal("10.000000"));

        var orderLines = List.of(line);

        AtomicInteger isolationAtOrder =
                new AtomicInteger(-1);

        AtomicInteger isolationAtHistory =
                new AtomicInteger(-1);

        when(companies.requireCurrentCompanyId())
                .thenReturn(1L);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 31L
        )).thenAnswer(invocation -> {
            isolationAtOrder.set(actualIsolation());
            return Optional.of(order);
        });

        when(lines.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                2L, 31L
        )).thenReturn(orderLines);

        when(history.read(2L, 1L, 31L, orderLines))
                .thenAnswer(invocation -> {
                    isolationAtHistory.set(actualIsolation());
                    return Map.of(
                            101L,
                            new BigDecimal("8.000")
                    );
                });

        var snapshot = service.prepare(
                2L,
                31L,
                Map.of(101L, new BigDecimal("12.000"))
        );

        assertEquals(
                Connection.TRANSACTION_READ_COMMITTED,
                isolationAtOrder.get()
        );

        assertEquals(
                Connection.TRANSACTION_READ_COMMITTED,
                isolationAtHistory.get()
        );

        assertEquals(
                0,
                new BigDecimal("8.000").compareTo(
                        snapshot.alreadyReceived().get(101L)
                )
        );

        assertEquals(
                0,
                new BigDecimal("12.000").compareTo(
                        snapshot.validatedQuantities().get(101L)
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

        verifyNoMoreInteractions(history);

        System.out.println(
                "SNAPSHOT MYSQL: READ COMMITTED confermato"
        );
        System.out.println(
                "SNAPSHOT MYSQL: ordine prima dello storico"
        );
        System.out.println(
                "SNAPSHOT MYSQL: quantità storica letta una volta"
        );
    }

    @Test
    void outerRepeatableReadIsRejectedBeforeOrderLookup()
            throws Exception {

        verifyDatabase();

        TransactionTemplate transaction =
                new TransactionTemplate(manager);

        transaction.setReadOnly(false);
        transaction.setIsolationLevel(
                TransactionDefinition.ISOLATION_REPEATABLE_READ
        );

        transaction.execute(status -> {

            assertEquals(
                    Connection.TRANSACTION_REPEATABLE_READ,
                    actualIsolation()
            );

            BusinessException error = assertThrows(
                    BusinessException.class,
                    () -> service.prepare(
                            2L,
                            31L,
                            Map.of(
                                    101L,
                                    new BigDecimal("8.000")
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
                "SNAPSHOT MYSQL: REPEATABLE READ rifiutato"
        );
        System.out.println(
                "SNAPSHOT MYSQL: nessuna lettura documentale"
        );
    }
}
