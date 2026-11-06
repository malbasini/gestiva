package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftPersistenceWriterTransactionIT
        extends AbstractMySqlIntegrationTest {

    private static final String NUMBER = "SI-TEST-TRANSACTION";
    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 5);

    @Autowired
    private SupplierInvoicePartialDraftPersistenceWriter writer;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard gate;

    @MockitoBean
    private SupplierInvoiceRepository invoices;

    @MockitoBean
    private SupplierInvoiceLineRepository lines;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private SupplierInvoicePartialDraftPlan plan() {
        var line = new SupplierInvoicePartialDraftPlan.Line(
                1,
                101L,
                1L,
                "Articolo collaudo",
                9L,
                "BOX",
                "PZ",
                bd("10.000000"),
                bd("0.700"),
                bd("72.00"),
                bd("0.00"),
                bd("22.00"),
                bd("50.40"),
                bd("11.09"),
                bd("61.49")
        );

        return new SupplierInvoicePartialDraftPlan(
                2L, 1L, 302L, 100L, 1L, "EUR",
                List.of(line),
                bd("50.40"),
                bd("11.09"),
                bd("61.49")
        );
    }

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

    private TransactionTemplate transaction(
            int isolation,
            boolean readOnly) {

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(isolation);
        tx.setReadOnly(readOnly);

        return tx;
    }

    @Test
    void rejectsCallWithoutTransaction() throws Exception {
        verifyDatabase();

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        assertThrows(
                IllegalTransactionStateException.class,
                () -> writer.write(plan(), NUMBER, DATE)
        );

        verifyNoInteractions(gate, invoices, lines);

        System.out.println(
                "WRITER TRANSACTION: MANDATORY senza TX rifiutato"
        );
    }

    @Test
    void rejectsRepeatableReadBeforePersistence()
            throws Exception {

        verifyDatabase();

        assertThrows(
                BusinessException.class,
                () -> transaction(
                        TransactionDefinition
                                .ISOLATION_REPEATABLE_READ,
                        false
                ).execute(status ->
                        writer.write(plan(), NUMBER, DATE)
                )
        );

        verify(gate).requireEnabled();
        verifyNoInteractions(invoices, lines);

        System.out.println(
                "WRITER TRANSACTION: REPEATABLE_READ rifiutato"
        );
    }

    @Test
    void rejectsReadOnlyBeforePersistence()
            throws Exception {

        verifyDatabase();

        assertThrows(
                BusinessException.class,
                () -> transaction(
                        TransactionDefinition
                                .ISOLATION_READ_COMMITTED,
                        true
                ).execute(status ->
                        writer.write(plan(), NUMBER, DATE)
                )
        );

        verify(gate).requireEnabled();
        verifyNoInteractions(invoices, lines);

        System.out.println(
                "WRITER TRANSACTION: READ_ONLY rifiutato"
        );
    }

    @Test
    void writableReadCommittedReachesBothRepositories()
            throws Exception {

        verifyDatabase();

        SupplierInvoice saved = mock(SupplierInvoice.class);
        when(saved.getId()).thenReturn(700L);

        when(invoices.save(any(SupplierInvoice.class)))
                .thenReturn(saved);

        Long result = transaction(
                TransactionDefinition.ISOLATION_READ_COMMITTED,
                false
        ).execute(status -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            Long id = writer.write(
                    plan(), NUMBER, DATE
            );

            status.setRollbackOnly();
            return id;
        });

        assertEquals(700L, result);

        var order = inOrder(gate, invoices, lines);

        order.verify(gate).requireEnabled();
        order.verify(invoices)
                .save(any(SupplierInvoice.class));
        order.verify(lines).saveAll(anyList());

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        System.out.println(
                "WRITER TRANSACTION: READ_COMMITTED scrivibile OK"
        );
        System.out.println(
                "WRITER TRANSACTION: testata prima delle righe OK"
        );
        System.out.println(
                "WRITER TRANSACTION: nessuna INSERT applicativa"
        );
    }
}
