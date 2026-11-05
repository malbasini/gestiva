package com.gestiva.purchasing.receipt.service;

import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.support.AbstractMySqlIntegrationTest;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.location.entity.Warehouse;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
    "gestiva.purchasing.partial-goods-receipt.enabled=true"
})
class GoodsReceiptPartialSequenceRollbackIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private GoodsReceiptPartialCoordinator coordinator;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private PurchaseOrderRepository orders;

    @MockitoBean
    private PurchaseOrderLineRepository orderLines;

    @MockitoBean
    private GoodsReceiptPartialHistoryReader history;

    @MockitoBean
    private CompanyContext companies;

    @MockitoBean
    private WarehouseRepository warehouses;

    @MockitoBean
    private GoodsReceiptRepository receipts;

    @MockitoBean
    private GoodsReceiptPartialPersistenceWriter writer;

    // Il DocumentSequenceService è REALE.
    // Il suo repository NON viene simulato.

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long ORDER = 31L;

    private static final String DOCUMENT_TYPE =
            DocumentSequenceService.GOODS_RECEIPT;

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
                DOCUMENT_TYPE
        );

        assertNotNull(value);
        return value;
    }

    private void verifyDatabase() throws Exception {

        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", connection.getCatalog());

            String username =
                    connection.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@"),
                    "Utente MySQL non autorizzato."
            );
        }

        Integer rows = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                Integer.class,
                TENANT,
                COMPANY,
                DOCUMENT_TYPE
        );

        assertEquals(
                1, rows,
                "Numeratore GOODS_RECEIPT di collaudo non configurato."
        );
    }

    private GoodsReceipt header() {

        GoodsReceipt result = new GoodsReceipt();

        result.setTenantId(TENANT);
        result.setCompanyId(COMPANY);
        result.setPurchaseOrderId(ORDER);
        result.setSupplierId(12L);
        result.setWarehouseId(7L);
        result.setReceiptDate(LocalDate.of(2026, 11, 5));

        return result;
    }

    private void configureRepositories() {

        PurchaseOrder order = mock(PurchaseOrder.class);

        when(order.getId()).thenReturn(ORDER);
        when(order.getTenantId()).thenReturn(TENANT);
        when(order.getCompanyId()).thenReturn(COMPANY);
        when(order.getSupplierId()).thenReturn(12L);
        when(order.getStatus()).thenReturn("CONFIRMED");

        PurchaseOrderLine line = mock(PurchaseOrderLine.class);

        when(line.getId()).thenReturn(101L);
        when(line.getTenantId()).thenReturn(TENANT);
        when(line.getPurchaseOrderId()).thenReturn(ORDER);
        when(line.getUomId()).thenReturn(5L);
        when(line.getQuantity())
                .thenReturn(new BigDecimal("2.000"));
        when(line.getConversionFactor())
                .thenReturn(new BigDecimal("10.000000"));
        when(line.getLineSubtotal())
                .thenReturn(new BigDecimal("144.00"));
        when(line.getItemId()).thenReturn(50L);
        when(line.getDescription()).thenReturn("Articolo test");

        List<PurchaseOrderLine> lines = List.of(line);

        when(companies.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        )).thenReturn(Optional.of(order));

        when(orderLines.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                TENANT, ORDER
        )).thenReturn(lines);

        when(history.read(TENANT, COMPANY, ORDER, lines))
                .thenReturn(Map.of(
                        101L, new BigDecimal("8.000")
                ));

        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.isActive()).thenReturn(true);

        when(warehouses.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, 7L
        )).thenReturn(Optional.of(warehouse));

        when(receipts.existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                TENANT, COMPANY, ORDER
        )).thenReturn(false);
    }

    @Test
    void realSequenceUpdateIsRolledBackAfterWriterFailure()
            throws Exception {

        verifyDatabase();
        configureRepositories();

        long before = currentNumber();

        AtomicReference<Long> numberDuringTransaction =
                new AtomicReference<>();

        when(writer.write(any())).thenAnswer(invocation -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            assertFalse(
                    TransactionSynchronizationManager
                            .isCurrentTransactionReadOnly()
            );

            var plan =
                    (GoodsReceiptPartialDocumentAssembler.DocumentPlan)
                    invocation.getArgument(0);

            assertNotNull(plan.header().getReceiptNumber());

            // Forza l'UPDATE del numeratore su MySQL,
            // prima di generare l'eccezione.
            entityManager.flush();

            long during = currentNumber();

            numberDuringTransaction.set(during);

            assertEquals(
                    before + 1,
                    during,
                    "Numeratore non aggiornato nella transazione."
            );

            throw new IllegalStateException(
                    "Errore simulato dopo UPDATE reale del numeratore."
            );
        });

        assertThrows(
                IllegalStateException.class,
                () -> coordinator.create(
                        header(),
                        Map.of(
                                101L,
                                new BigDecimal("12.000")
                        )
                )
        );

        assertNotNull(
                numberDuringTransaction.get(),
                "Il writer simulato non è stato raggiunto."
        );

        // Il coordinatore ha concluso la transazione
        // con rollback prima di questa verifica.
        long after = currentNumber();

        assertEquals(
                before,
                after,
                "ROLLBACK FALLITO: numeratore modificato!"
        );

        verify(writer, times(1)).write(any());

        System.out.println(
                "SEQUENCE MYSQL: UPDATE reale eseguito"
        );
        System.out.println(
                "SEQUENCE MYSQL: incremento visibile nella transazione"
        );
        System.out.println(
                "SEQUENCE MYSQL: rollback ripristina next_number"
        );
        System.out.println(
                "SEQUENCE MYSQL: valore iniziale e finale identici"
        );
    }
}
