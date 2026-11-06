package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialDraftCreationLockIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 9992061L;

    @Autowired
    private SupplierInvoicePartialDraftCreationService creation;

    @Autowired
    private PurchaseOrderRepository orders;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard gate;

    @MockitoBean
    private CompanyContext companyContext;

    @MockitoBean
    private GoodsReceiptRepository receipts;

    @MockitoBean
    private GoodsReceiptLineRepository receiptLines;

    @MockitoBean
    private SupplierInvoiceRepository invoices;

    @MockitoBean
    private SupplierInvoicePartialDraftAssembler assembler;

    @MockitoBean
    private DocumentSequenceService sequences;

    @MockitoBean
    private SupplierInvoicePartialDraftPersistenceWriter writer;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private TransactionTemplate transaction() {
        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        tx.setReadOnly(false);
        return tx;
    }

    private Long connectionId() {
        Long result = jdbc.queryForObject(
                "SELECT CONNECTION_ID()", Long.class
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

            String username =
                    connection.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@")
            );
        }
    }

    private SupplierInvoicePartialDraftPlan plan(
            Long orderId,
            Long supplierId) {

        var line = new SupplierInvoicePartialDraftPlan.Line(
                1,
                101L,
                1L,
                "Collaudo lock fattura parziale",
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
                orderId,
                supplierId,
                "EUR",
                List.of(line),
                bd("57.60"),
                bd("12.67"),
                bd("70.27")
        );
    }

    @Test
    @Timeout(30)
    void orderLockRemainsHeldWhileWriterIsExecuting()
            throws Exception {

        verifyDatabase();

        // Usiamo un ordine confermato esistente.
        // Non modifichiamo alcuna sua riga.
        Map<String, Object> reference = jdbc.queryForMap("""
                SELECT id, supplier_id
                FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND status = 'CONFIRMED'
                  AND supplier_id IS NOT NULL
                ORDER BY id ASC
                LIMIT 1
                """, TENANT, COMPANY);

        Long orderId =
                ((Number) reference.get("id")).longValue();

        Long supplierId =
                ((Number) reference.get("supplier_id")).longValue();

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setId(RECEIPT);
        receipt.setTenantId(TENANT);
        receipt.setCompanyId(COMPANY);
        receipt.setPurchaseOrderId(orderId);
        receipt.setSupplierId(supplierId);

        var draftPlan = plan(orderId, supplierId);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(receipts.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, RECEIPT
        )).thenReturn(Optional.of(receipt));

        when(receiptLines
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT, COMPANY, RECEIPT
                )).thenReturn(List.of(new GoodsReceiptLine()));

        when(assembler.assemble(any()))
                .thenReturn(draftPlan);

        when(sequences.nextNumber(
                TENANT,
                COMPANY,
                DocumentSequenceService.SUPPLIER_INVOICE
        )).thenReturn("SI-TEST-2061");

        CountDownLatch writerReached =
                new CountDownLatch(1);

        CountDownLatch releaseWriter =
                new CountDownLatch(1);

        CountDownLatch secondReady =
                new CountDownLatch(1);

        CountDownLatch secondLocked =
                new CountDownLatch(1);

        when(writer.write(
                eq(draftPlan),
                eq("SI-TEST-2061"),
                any(LocalDate.class)
        )).thenAnswer(invocation -> {

            // Siamo nella fase di scrittura,
            // dopo il lock del PO e la numerazione.
            writerReached.countDown();

            if (!releaseWriter.await(
                    10, TimeUnit.SECONDS
            )) {
                throw new IllegalStateException(
                        "Timeout rilascio writer"
                );
            }

            return 700L;
        });

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {
            Future<Long> first = executor.submit(() ->
                    transaction().execute(status -> {

                        Long connection = connectionId();

                        Long result = creation.create(
                                TENANT, RECEIPT
                        );

                        assertEquals(700L, result);

                        status.setRollbackOnly();

                        return connection;
                    })
            );

            if (!writerReached.await(
                    10, TimeUnit.SECONDS
            )) {
                if (first.isDone()) {
                    first.get(2, TimeUnit.SECONDS);
                }
                fail("Il coordinatore non ha raggiunto il writer.");
            }

            System.out.println(
                    "CREATION LOCK: transazione A nel writer"
            );

            Future<Long> second = executor.submit(() ->
                    transaction().execute(status -> {

                        Long connection = connectionId();

                        secondReady.countDown();

                        var locked = orders
                                .findLockedByTenantIdAndCompanyIdAndId(
                                        TENANT, COMPANY, orderId
                                );

                        assertTrue(locked.isPresent());
                        assertEquals(
                                orderId,
                                locked.get().getId()
                        );

                        secondLocked.countDown();

                        status.setRollbackOnly();

                        return connection;
                    })
            );

            assertTrue(
                    secondReady.await(10, TimeUnit.SECONDS),
                    "La transazione B non è partita."
            );

            // B non deve superare il lock
            // mentre A è ancora nel writer.
            assertFalse(
                    secondLocked.await(
                            500, TimeUnit.MILLISECONDS
                    ),
                    "B ha acquisito il lock mentre A era nel writer."
            );

            assertFalse(
                    second.isDone(),
                    "B si è conclusa prima del rilascio di A."
            );

            System.out.println(
                    "CREATION LOCK: transazione B in attesa"
            );

            // A completa il writer e termina la TX.
            releaseWriter.countDown();

            Long firstConnection =
                    first.get(10, TimeUnit.SECONDS);

            Long secondConnection =
                    second.get(10, TimeUnit.SECONDS);

            assertNotNull(firstConnection);
            assertNotNull(secondConnection);

            assertNotEquals(
                    firstConnection,
                    secondConnection,
                    "Le transazioni devono usare connessioni distinte."
            );

            assertEquals(0L, secondLocked.getCount());

            verify(gate, times(1)).requireEnabled();

            verify(writer, times(1)).write(
                    eq(draftPlan),
                    eq("SI-TEST-2061"),
                    any(LocalDate.class)
            );

            System.out.println(
                    "CREATION LOCK: B acquisisce dopo rilascio A"
            );
            System.out.println(
                    "CREATION LOCK: connessioni MySQL distinte"
            );
            System.out.println(
                    "CREATION LOCK: lock mantenuto fino al writer"
            );
            System.out.println(
                    "CREATION LOCK: nessuna INSERT applicativa"
            );

        } finally {
            releaseWriter.countDown();
            executor.shutdownNow();

            assertTrue(
                    executor.awaitTermination(
                            10, TimeUnit.SECONDS
                    ),
                    "Thread di test non terminati."
            );
        }
    }
}
