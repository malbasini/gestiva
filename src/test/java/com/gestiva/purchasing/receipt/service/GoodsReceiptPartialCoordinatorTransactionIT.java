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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@TestPropertySource(properties = {
    "gestiva.purchasing.partial-goods-receipt.enabled=true"
})
class GoodsReceiptPartialCoordinatorTransactionIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private GoodsReceiptPartialCoordinator coordinator;

    @Autowired
    private JdbcTemplate jdbc;

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
    private DocumentSequenceService sequences;

    @MockitoBean
    private GoodsReceiptPartialPersistenceWriter writer;

    private record TxState(
            Long connectionId,
            int isolation,
            boolean active,
            boolean readOnly
    ) {}

    private TxState state() {

        return jdbc.execute(
                (org.springframework.jdbc.core.ConnectionCallback<TxState>)
                connection -> new TxState(
                        jdbc.queryForObject(
                                "SELECT CONNECTION_ID()", Long.class
                        ),
                        connection.getTransactionIsolation(),
                        TransactionSynchronizationManager
                                .isActualTransactionActive(),
                        TransactionSynchronizationManager
                                .isCurrentTransactionReadOnly()
                )
        );
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

    private GoodsReceipt header() {

        GoodsReceipt result = new GoodsReceipt();

        result.setTenantId(2L);
        result.setCompanyId(1L);
        result.setPurchaseOrderId(31L);
        result.setSupplierId(12L);
        result.setWarehouseId(7L);
        result.setReceiptDate(LocalDate.of(2026, 11, 5));

        return result;
    }

    private void setupValidSnapshot(
            AtomicReference<TxState> atOrder,
            AtomicReference<TxState> atHistory) {

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
        when(line.getQuantity()).thenReturn(new BigDecimal("2.000"));
        when(line.getConversionFactor())
                .thenReturn(new BigDecimal("10.000000"));
        when(line.getLineSubtotal())
                .thenReturn(new BigDecimal("144.00"));
        when(line.getItemId()).thenReturn(50L);
        when(line.getDescription()).thenReturn("Articolo test");

        List<PurchaseOrderLine> lines = List.of(line);

        when(companies.requireCurrentCompanyId()).thenReturn(1L);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 31L
        )).thenAnswer(invocation -> {
            atOrder.set(state());
            return Optional.of(order);
        });

        when(orderLines.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                2L, 31L
        )).thenReturn(lines);

        when(history.read(2L, 1L, 31L, lines))
                .thenAnswer(invocation -> {
                    atHistory.set(state());
                    return Map.of(
                            101L,
                            new BigDecimal("8.000")
                    );
                });

        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.isActive()).thenReturn(true);

        when(warehouses.findByTenantIdAndCompanyIdAndId(
                2L, 1L, 7L
        )).thenReturn(Optional.of(warehouse));

        when(sequences.nextNumber(
                2L, 1L, DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-1911");
    }

    @Test
    void writerAndSnapshotShareOneRealTransaction()
            throws Exception {

        verifyDatabase();

        AtomicReference<TxState> atOrder = new AtomicReference<>();
        AtomicReference<TxState> atHistory = new AtomicReference<>();
        AtomicReference<TxState> atWriter = new AtomicReference<>();

        setupValidSnapshot(atOrder, atHistory);

        when(writer.write(any())).thenAnswer(invocation -> {
            atWriter.set(state());

            var plan = (GoodsReceiptPartialDocumentAssembler.DocumentPlan)
                    invocation.getArgument(0);

            assertEquals(
                    "GR-TEST-1911",
                    plan.header().getReceiptNumber()
            );

            var rows = plan.buildLines(500L);

            assertEquals(1, rows.size());
            assertEquals(
                    0,
                    new BigDecimal("86.4000").compareTo(
                            rows.get(0).getTotalCost()
                    )
            );

            return 500L;
        });

        Long result = coordinator.create(
                header(),
                Map.of(101L, new BigDecimal("12.000"))
        );

        assertEquals(500L, result);

        TxState first = atOrder.get();
        TxState second = atHistory.get();
        TxState third = atWriter.get();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        for (TxState tx : List.of(first, second, third)) {
            assertTrue(tx.active());
            assertFalse(tx.readOnly());
            assertEquals(
                    Connection.TRANSACTION_READ_COMMITTED,
                    tx.isolation()
            );
            assertEquals(first.connectionId(), tx.connectionId());
        }

        var calls = inOrder(
                orders, orderLines, history, warehouses,
                receipts, sequences, writer
        );

        calls.verify(orders)
                .findLockedByTenantIdAndCompanyIdAndId(
                        2L, 1L, 31L
                );

        calls.verify(orderLines)
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        2L, 31L
                );

        calls.verify(history)
                .read(eq(2L), eq(1L), eq(31L), anyList());

        calls.verify(warehouses)
                .findByTenantIdAndCompanyIdAndId(2L, 1L, 7L);

        calls.verify(receipts)
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        2L, 1L, 31L
                );

        calls.verify(sequences).nextNumber(
                2L, 1L, DocumentSequenceService.GOODS_RECEIPT
        );

        calls.verify(writer).write(any());

        System.out.println(
                "COORDINATOR MYSQL: unica transazione READ COMMITTED"
        );
        System.out.println(
                "COORDINATOR MYSQL: stessa connessione fino al writer"
        );
        System.out.println(
                "COORDINATOR MYSQL: costo FIFO 86.4000"
        );
    }

    @Test
    void writerExceptionRollsBackWholeTransaction()
            throws Exception {

        verifyDatabase();

        AtomicReference<TxState> atOrder = new AtomicReference<>();
        AtomicReference<TxState> atHistory = new AtomicReference<>();
        AtomicInteger completion = new AtomicInteger(-1);

        setupValidSnapshot(atOrder, atHistory);

        when(writer.write(any())).thenAnswer(invocation -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {
                                @Override
                                public void afterCompletion(int status) {
                                    completion.set(status);
                                }
                            }
                    );

            throw new IllegalStateException(
                    "Errore simulato nel writer."
            );
        });

        assertThrows(
                IllegalStateException.class,
                () -> coordinator.create(
                        header(),
                        Map.of(101L, new BigDecimal("12.000"))
                )
        );

        assertNotNull(atOrder.get());
        assertNotNull(atHistory.get());

        assertEquals(
                TransactionSynchronization.STATUS_ROLLED_BACK,
                completion.get()
        );

        verify(writer, times(1)).write(any());

        System.out.println(
                "COORDINATOR MYSQL: eccezione writer propagata"
        );
        System.out.println(
                "COORDINATOR MYSQL: rollback transazionale confermato"
        );
    }
}
