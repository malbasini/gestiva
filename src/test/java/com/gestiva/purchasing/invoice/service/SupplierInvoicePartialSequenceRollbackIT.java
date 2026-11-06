package com.gestiva.purchasing.invoice.service;

import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialSequenceRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;

    private static final String TYPE =
            DocumentSequenceService.SUPPLIER_INVOICE;

    @Autowired
    private DocumentSequenceService sequences;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    private static final String READ_NUMBER = """
            SELECT next_number
            FROM document_sequence
            WHERE tenant_id = ?
              AND company_id = ?
              AND document_type = ?
            """;

    private long currentNumber() {
        Long value = jdbc.queryForObject(
                READ_NUMBER,
                Long.class,
                TENANT,
                COMPANY,
                TYPE
        );
        assertNotNull(value);
        return value;
    }

    private void verifyTestDatabase() throws Exception {
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
                    || username.startsWith("gestiva_test_runner@")
            );
        }

        Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                Long.class,
                TENANT,
                COMPANY,
                TYPE
        );

        assertEquals(
                1L,
                count,
                "Numeratore SUPPLIER_INVOICE non configurato."
        );
    }

    @Test
    void realInvoiceSequenceIncrementIsRolledBack()
            throws Exception {

        verifyTestDatabase();

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        long before = currentNumber();

        AtomicReference<Long> during =
                new AtomicReference<>();

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        tx.setReadOnly(false);

        try {
            tx.execute(status -> {

                assertTrue(
                        TransactionSynchronizationManager
                                .isActualTransactionActive()
                );

                assertFalse(
                        TransactionSynchronizationManager
                                .isCurrentTransactionReadOnly()
                );

                String invoiceNumber =
                        sequences.nextNumber(
                                TENANT,
                                COMPANY,
                                TYPE
                        );

                assertNotNull(invoiceNumber);
                assertFalse(invoiceNumber.isBlank());
                assertTrue(invoiceNumber.length() <= 50);

                // Forza la scrittura JPA nel database,
                // sempre dentro la transazione corrente.
                entityManager.flush();

                long updated = currentNumber();
                during.set(updated);

                assertEquals(
                        before + 1,
                        updated,
                        "Incremento non visibile su MySQL."
                );

                System.out.println(
                        "INVOICE SEQUENCE: incremento MySQL visibile"
                );

                // Nessun commit: annulliamo l'incremento.
                status.setRollbackOnly();

                return null;
            });

        } finally {
            // Anche in caso di eccezione verifichiamo
            // che il valore iniziale sia ripristinato.
            long after = currentNumber();

            assertEquals(
                    before,
                    after,
                    "ROLLBACK FALLITO: numeratore modificato!"
            );

            System.out.println(
                    "INVOICE SEQUENCE: rollback next_number OK"
            );
        }

        assertNotNull(during.get());

        assertEquals(
                before + 1,
                during.get()
        );

        System.out.println(
                "INVOICE SEQUENCE: valore iniziale e finale identici"
        );
        System.out.println(
                "INVOICE SEQUENCE: nessuna fattura generata"
        );
    }
}
