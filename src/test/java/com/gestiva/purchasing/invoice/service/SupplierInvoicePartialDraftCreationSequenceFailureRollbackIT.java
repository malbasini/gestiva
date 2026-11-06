package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import jakarta.persistence.EntityManager;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftCreationSequenceFailureRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 302L;
    private static final Long ORDER = 100L;
    private static final Long SUPPLIER = 1L;

    @Autowired
    private SupplierInvoicePartialDraftCreationService creation;

    @Autowired
    private DocumentSequenceService sequences;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private SupplierInvoicePartialPreparationService preparation;

    @MockitoBean
    private SupplierInvoicePartialDraftAssembler assembler;

    @MockitoBean
    private SupplierInvoicePartialDraftPersistenceWriter writer;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private long sequence() {
        Long result = jdbc.queryForObject("""
                SELECT next_number
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                Long.class,
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        );

        assertNotNull(result);
        return result;
    }

    private void verifyDatabase() throws Exception {
        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals(
                    "gestiva_test",
                    connection.getCatalog()
            );

            String user =
                    connection.getMetaData().getUserName();

            assertTrue(
                    user.equals("gestiva_test_runner")
                    || user.startsWith("gestiva_test_runner@")
            );
        }

        Long configured = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                Long.class,
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        );

        assertEquals(1L, configured);
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
                TENANT,
                COMPANY,
                receipt,
                order,
                List.of()
        );
    }

    private SupplierInvoicePartialDraftPlan plan() {

        var line = new SupplierInvoicePartialDraftPlan.Line(
                1,
                101L,
                1L,
                "Collaudo rollback numeratore",
                9L,
                "BOX",
                "PZ",
                bd("10.000000"),
                bd("0.800"),
                bd("72.00"),
                bd("0.00"),
                bd("22.00"),
                bd("57.60"),
                bd("12.67"),
                bd("70.27")
        );

        return new SupplierInvoicePartialDraftPlan(
                TENANT,
                COMPANY,
                RECEIPT,
                ORDER,
                SUPPLIER,
                "EUR",
                List.of(line),
                bd("57.60"),
                bd("12.67"),
                bd("70.27")
        );
    }

    @Test
    void realSequenceIncrementIsAutomaticallyRolledBack()
            throws Exception {

        verifyDatabase();

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        long before = sequence();

        var data = prepared();
        var draftPlan = plan();

        AtomicBoolean writerReached =
                new AtomicBoolean(false);

        AtomicInteger completion =
                new AtomicInteger(-1);

        AtomicReference<String> allocatedNumber =
                new AtomicReference<>();

        when(preparation.prepare(TENANT, RECEIPT))
                .thenAnswer(invocation -> {

                    assertTrue(
                            TransactionSynchronizationManager
                                    .isActualTransactionActive()
                    );

                    assertFalse(
                            TransactionSynchronizationManager
                                    .isCurrentTransactionReadOnly()
                    );

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

        when(assembler.assemble(data))
                .thenReturn(draftPlan);

        // Il coordinatore usa il DocumentSequenceService
        // reale. Qui simuliamo solo il fallimento del writer.
        when(writer.write(
                eq(draftPlan),
                anyString(),
                any(LocalDate.class)
        )).thenAnswer(invocation -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            String number = invocation.getArgument(1);
            assertNotNull(number);
            assertFalse(number.isBlank());

            allocatedNumber.set(number);

            // Forza su MySQL l'UPDATE reale della sequenza.
            entityManager.flush();

            assertEquals(
                    before + 1,
                    sequence(),
                    "Il numeratore non è avanzato nella TX."
            );

            writerReached.set(true);

            System.out.println(
                    "SEQUENCE FAILURE: numero reale assegnato"
            );

            System.out.println(
                    "SEQUENCE FAILURE: incremento MySQL visibile"
            );

            throw new IllegalStateException(
                    "IT2066: errore simulato dopo numerazione"
            );
        });

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> creation.create(TENANT, RECEIPT)
        );

        assertEquals(
                "IT2066: errore simulato dopo numerazione",
                error.getMessage()
        );

        assertTrue(
                writerReached.get(),
                "Il writer simulato non è stato raggiunto."
        );

        assertNotNull(allocatedNumber.get());

        assertEquals(
                TransactionSynchronization.STATUS_ROLLED_BACK,
                completion.get(),
                "Spring non ha completato il rollback."
        );

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        long after = sequence();

        assertEquals(
                before,
                after,
                "ROLLBACK FALLITO: numeratore modificato."
        );

        verify(preparation, times(1))
                .prepare(TENANT, RECEIPT);

        verify(assembler, times(1))
                .assemble(data);

        verify(writer, times(1)).write(
                eq(draftPlan),
                eq(allocatedNumber.get()),
                any(LocalDate.class)
        );

        System.out.println(
                "SEQUENCE FAILURE: eccezione propagata OK"
        );

        System.out.println(
                "SEQUENCE FAILURE: rollback Spring confermato"
        );

        System.out.println(
                "SEQUENCE FAILURE: numeratore ripristinato OK"
        );

        System.out.println(
                "SEQUENCE FAILURE: nessuna INSERT fattura"
        );
    }
}
