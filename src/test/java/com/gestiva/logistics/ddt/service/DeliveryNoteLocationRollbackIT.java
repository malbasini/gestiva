package com.gestiva.logistics.ddt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DeliveryNoteLocationRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final long TENANT = 2L;
    private static final long COMPANY = 3L;
    private static final long CUSTOMER = 1L;
    private static final long WAREHOUSE = 2L;
    private static final long LOCATION = 2L;
    private static final long ITEM = 1L;
    private static final long SEQUENCE = 19L;

    @Autowired JdbcTemplate jdbc;
    @Autowired DeliveryNoteService deliveryNoteService;
    @Autowired PlatformTransactionManager transactionManager;

    @MockitoBean CompanyContext companyContext;

    private int count(String sql, Object... args) {
        Integer value = jdbc.queryForObject(
                sql, Integer.class, args
        );
        return value == null ? 0 : value;
    }

    private BigDecimal quantity(String sql, Object... args) {
        BigDecimal value = jdbc.queryForObject(
                sql, BigDecimal.class, args
        );
        return value == null ? BigDecimal.ZERO : value;
    }

    private long insertOrder(String number) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            var ps = connection.prepareStatement("""
                INSERT INTO sales_order (
                    tenant_id, company_id, customer_id,
                    order_number, order_date, status,
                    currency_code, subtotal_amount,
                    tax_amount, total_amount,
                    created_at, updated_at
                ) VALUES (
                    ?, ?, ?, ?, CURRENT_DATE, 'FULFILLED',
                    'EUR', 170.00, 37.40, 207.40,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """, Statement.RETURN_GENERATED_KEYS);

            ps.setLong(1, TENANT);
            ps.setLong(2, COMPANY);
            ps.setLong(3, CUSTOMER);
            ps.setString(4, number);
            return ps;
        }, keys);

        assertNotNull(keys.getKey());
        return keys.getKey().longValue();
    }

    private void insertOrderLine(long orderId) {
        jdbc.update("""
            INSERT INTO sales_order_line (
                tenant_id, company_id, sales_order_id,
                line_no, description, quantity,
                unit_of_measure, unit_price, line_total,
                discount_pct, tax_pct, tax_amount,
                item_id, conversion_factor,
                created_at, updated_at
            ) VALUES (
                ?, ?, ?, 1, 'Fixture DDT rollback ubicazione',
                2.000, 'PZ', 85.00, 170.00,
                0.00, 22.00, 37.40,
                ?, 1.000000,
                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
            """, TENANT, COMPANY, orderId, ITEM);
    }

    @Test
    void insufficientLocationStockRollsBackDocumentAndSequence() {
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY);

        assertEquals(1, count("""
            SELECT COUNT(*) FROM customer
            WHERE id = ? AND tenant_id = ?
            """, CUSTOMER, TENANT));

        assertEquals(1, count("""
            SELECT COUNT(*) FROM warehouse
            WHERE id = ? AND tenant_id = ?
              AND company_id = ? AND active = 1
            """, WAREHOUSE, TENANT, COMPANY));

        assertEquals(1, count("""
            SELECT COUNT(*) FROM warehouse_location
            WHERE id = ? AND tenant_id = ?
              AND warehouse_id = ? AND active = 1
            """, LOCATION, TENANT, WAREHOUSE));

        assertEquals(1, count("""
            SELECT COUNT(*) FROM document_sequence
            WHERE id = ? AND tenant_id = ?
              AND company_id = ?
              AND document_type = 'DELIVERY_NOTE'
            """, SEQUENCE, TENANT, COMPANY));

        BigDecimal available = quantity("""
            SELECT COALESCE(SUM(
                CASE
                  WHEN movement_type IN ('IN', 'ADJUSTMENT_IN')
                    THEN quantity
                  WHEN movement_type IN ('OUT', 'ADJUSTMENT_OUT')
                    THEN -quantity
                  ELSE 0
                END
            ), 0)
            FROM inventory_movement
            WHERE tenant_id = ? AND company_id = ?
              AND warehouse_id = ? AND item_id = ?
              AND location_id = ?
            """, TENANT, COMPANY, WAREHOUSE, ITEM, LOCATION);

        assertEquals(0, available.compareTo(
                new BigDecimal("1.000")));

        Long sequenceBefore = jdbc.queryForObject("""
            SELECT next_number FROM document_sequence
            WHERE id = ?
            """, Long.class, SEQUENCE);

        BigDecimal fifoBefore = quantity("""
            SELECT COALESCE(SUM(remaining_qty), 0)
            FROM inventory_layer
            WHERE tenant_id = ? AND company_id = ?
              AND warehouse_id = ? AND item_id = ?
              AND location_id = ? AND closed = 0
            """, TENANT, COMPANY, WAREHOUSE, ITEM, LOCATION);

        assertEquals(0, fifoBefore.compareTo(
                new BigDecimal("1.000")));

        int movementsBefore = count("""
            SELECT COUNT(*) FROM inventory_movement
            WHERE tenant_id = ? AND company_id = ?
              AND warehouse_id = ? AND item_id = ?
            """, TENANT, COMPANY, WAREHOUSE, ITEM);

        int consumptionsBefore = count("""
            SELECT COUNT(*) FROM inventory_layer_consumption
            WHERE tenant_id = ? AND company_id = ?
              AND warehouse_id = ?
            """, TENANT, COMPANY, WAREHOUSE);

        String orderNumber = "SO-RB-" +
                UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 16);

        AtomicLong orderId = new AtomicLong();

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        BusinessException failure = assertThrows(
                BusinessException.class,
                () -> tx.execute(status -> {
                    long id = insertOrder(orderNumber);
                    orderId.set(id);

                    insertOrderLine(id);

                    deliveryNoteService.createFromSalesOrder(
                            TENANT, id, WAREHOUSE, LOCATION
                    );

                    fail("Scarico di 2 PZ non rifiutato");
                    return null;
                })
        );

        assertTrue(
                failure.getMessage().contains(
                        "Disponibilità insufficiente"),
                "Errore inatteso: " + failure.getMessage()
        );

        assertTrue(orderId.get() > 0);

        assertEquals(0, count("""
            SELECT COUNT(*) FROM sales_order
            WHERE id = ?
            """, orderId.get()));

        assertEquals(0, count("""
            SELECT COUNT(*) FROM sales_order_line
            WHERE sales_order_id = ?
            """, orderId.get()));

        assertEquals(0, count("""
            SELECT COUNT(*) FROM delivery_note
            WHERE sales_order_id = ?
            """, orderId.get()));

        assertEquals(movementsBefore, count("""
            SELECT COUNT(*) FROM inventory_movement
            WHERE tenant_id = ? AND company_id = ?
              AND warehouse_id = ? AND item_id = ?
            """, TENANT, COMPANY, WAREHOUSE, ITEM));

        assertEquals(consumptionsBefore, count("""
            SELECT COUNT(*) FROM inventory_layer_consumption
            WHERE tenant_id = ? AND company_id = ?
              AND warehouse_id = ?
            """, TENANT, COMPANY, WAREHOUSE));

        assertEquals(0, fifoBefore.compareTo(quantity("""
            SELECT COALESCE(SUM(remaining_qty), 0)
            FROM inventory_layer
            WHERE tenant_id = ? AND company_id = ?
              AND warehouse_id = ? AND item_id = ?
              AND location_id = ? AND closed = 0
            """, TENANT, COMPANY, WAREHOUSE, ITEM, LOCATION)));

        assertEquals(sequenceBefore, jdbc.queryForObject("""
            SELECT next_number FROM document_sequence
            WHERE id = ?
            """, Long.class, SEQUENCE));

        System.out.println(
                "DDT LOCATION ROLLBACK: PASS — "
                + "documenti, movimenti, FIFO e numeratore invariati"
        );
    }
}
