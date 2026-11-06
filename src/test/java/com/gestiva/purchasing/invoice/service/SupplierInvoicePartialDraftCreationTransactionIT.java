package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftCreationTransactionIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 302L;
    private static final Long ORDER = 100L;
    private static final Long SUPPLIER = 1L;

    @Autowired
    private SupplierInvoicePartialDraftCreationService service;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private SupplierInvoicePartialPreparationService preparation;

    @MockitoBean
    private SupplierInvoicePartialDraftAssembler assembler;

    @MockitoBean
    private DocumentSequenceService sequences;

    @MockitoBean
    private SupplierInvoicePartialDraftPersistenceWriter writer;

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private void verifyDatabase() throws Exception {
        try (Connection c = jdbc.getDataSource().getConnection()) {
            assertEquals("gestiva_test", c.getCatalog());

            String user = c.getMetaData().getUserName();

            assertTrue(
                    user.equals("gestiva_test_runner")
                    || user.startsWith("gestiva_test_runner@")
            );
        }
    }

    private long connectionId() {
        Long id = jdbc.queryForObject(
                "SELECT CONNECTION_ID()", Long.class
        );
        assertNotNull(id);
        return id;
    }

    private void verifyTransaction() {
        assertTrue(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        assertFalse(
                TransactionSynchronizationManager
                        .isCurrentTransactionReadOnly()
        );

        Integer isolation = jdbc.execute(
                (org.springframework.jdbc.core.ConnectionCallback<Integer>)
                        Connection::getTransactionIsolation
        );

        assertEquals(
                Connection.TRANSACTION_READ_COMMITTED,
                isolation
        );
    }

    private SupplierInvoicePartialPreparationService.Preparation
    prepared() {

        PurchaseOrder order = mock(PurchaseOrder.class);
        when(order.getId()).thenReturn(ORDER);
        when(order.getSupplierId()).thenReturn(SUPPLIER);

        GoodsReceipt receipt = mock(GoodsReceipt.class);
        when(receipt.getId()).thenReturn(RECEIPT);
        when(receipt.getPurchaseOrderId()).thenReturn(ORDER);
        when(receipt.getSupplierId()).thenReturn(SUPPLIER);

        return new SupplierInvoicePartialPreparationService.Preparation(
                TENANT, COMPANY, receipt, order, List.of()
        );
    }

    private SupplierInvoicePartialDraftPlan plan() {
        var line = new SupplierInvoicePartialDraftPlan.Line(
                1, 101L, 1L, "Articolo test",
                9L, "BOX", "PZ",
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
                TENANT, COMPANY, RECEIPT, ORDER,
                SUPPLIER, "EUR", List.of(line),
                bd("50.40"), bd("11.09"), bd("61.49")
        );
    }

    @Test
    void allStagesShareOneRealTransaction()
            throws Exception {

        verifyDatabase();

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        var data = prepared();
        var plan = plan();

        AtomicReference<Long> jdbcConnection =
                new AtomicReference<>();

        when(preparation.prepare(TENANT, RECEIPT))
                .thenAnswer(invocation -> {
                    verifyTransaction();
                    jdbcConnection.set(connectionId());
                    return data;
                });

        when(assembler.assemble(data))
                .thenAnswer(invocation -> {
                    verifyTransaction();
                    assertEquals(
                            jdbcConnection.get(),
                            connectionId()
                    );
                    return plan;
                });

        when(sequences.nextNumber(
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        )).thenAnswer(invocation -> {
            verifyTransaction();
            assertEquals(
                    jdbcConnection.get(),
                    connectionId()
            );
            return "SI-TEST-2056";
        });

        when(writer.write(
                eq(plan),
                eq("SI-TEST-2056"),
                any(LocalDate.class)
        )).thenAnswer(invocation -> {
            verifyTransaction();
            assertEquals(
                    jdbcConnection.get(),
                    connectionId()
            );
            return 700L;
        });

        assertEquals(
                700L,
                service.create(TENANT, RECEIPT)
        );

        assertNotNull(jdbcConnection.get());

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        var ordered = inOrder(
                preparation, assembler, sequences, writer
        );

        ordered.verify(preparation).prepare(TENANT, RECEIPT);
        ordered.verify(assembler).assemble(data);
        ordered.verify(sequences).nextNumber(
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        );
        ordered.verify(writer).write(
                eq(plan),
                eq("SI-TEST-2056"),
                any(LocalDate.class)
        );

        System.out.println(
                "CREATION TX: stessa connessione JDBC OK"
        );
        System.out.println(
                "CREATION TX: READ_COMMITTED scrivibile OK"
        );
        System.out.println(
                "CREATION TX: ordine operazioni OK"
        );
        System.out.println(
                "CREATION TX: nessuna INSERT applicativa"
        );
    }

    @Test
    void writerFailureRollsBackCoordinatorTransaction()
            throws Exception {

        verifyDatabase();

        var data = prepared();
        var plan = plan();

        AtomicInteger completion = new AtomicInteger(-1);

        when(preparation.prepare(TENANT, RECEIPT))
                .thenAnswer(invocation -> {
                    verifyTransaction();

                    TransactionSynchronizationManager
                            .registerSynchronization(
                                    new TransactionSynchronization() {
                                        @Override
                                        public void afterCompletion(
                                                int status) {
                                            completion.set(status);
                                        }
                                    }
                            );

                    return data;
                });

        when(assembler.assemble(data)).thenReturn(plan);

        when(sequences.nextNumber(
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        )).thenReturn("SI-TEST-2056-RBK");

        when(writer.write(
                eq(plan),
                eq("SI-TEST-2056-RBK"),
                any(LocalDate.class)
        )).thenThrow(new IllegalStateException(
                "IT2056: errore writer"
        ));

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        assertEquals(
                "IT2056: errore writer",
                failure.getMessage()
        );

        assertEquals(
                TransactionSynchronization.STATUS_ROLLED_BACK,
                completion.get()
        );

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        verify(writer).write(
                eq(plan),
                eq("SI-TEST-2056-RBK"),
                any(LocalDate.class)
        );

        System.out.println(
                "CREATION TX: errore writer propagato OK"
        );
        System.out.println(
                "CREATION TX: rollback Spring confermato"
        );
        System.out.println(
                "CREATION TX: nessuna fattura generata"
        );
    }
}
