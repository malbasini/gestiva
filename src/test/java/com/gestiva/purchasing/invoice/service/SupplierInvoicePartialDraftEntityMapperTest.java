package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialDraftEntityMapperTest {

    private final SupplierInvoicePartialDraftEntityMapper mapper =
            new SupplierInvoicePartialDraftEntityMapper();

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static void amount(
            String expected,
            BigDecimal actual) {

        assertNotNull(actual);

        assertEquals(
                0,
                bd(expected).compareTo(actual)
        );
    }

    private SupplierInvoicePartialDraftPlan plan() {

        var line =
                new SupplierInvoicePartialDraftPlan.Line(
                        1,
                        101L,
                        1L,
                        "Articolo collaudo parziale",
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
                2L,
                1L,
                302L,
                100L,
                1L,
                "EUR",
                List.of(line),
                bd("50.40"),
                bd("11.09"),
                bd("61.49")
        );
    }

    @Test
    void createsDraftHeaderWithCorrectScopeAndTotals() {

        LocalDate date = LocalDate.of(
                2026, 11, 5
        );

        var header = mapper.toHeader(
                plan(),
                "SI-TEST-00001",
                date
        );

        assertNull(header.getId());

        assertEquals(2L, header.getTenantId());
        assertEquals(1L, header.getCompanyId());
        assertEquals("SI-TEST-00001",
                header.getInvoiceNumber());

        assertEquals(date, header.getInvoiceDate());
        assertEquals(1L, header.getSupplierId());
        assertEquals(302L, header.getGoodsReceiptId());
        assertEquals(100L, header.getPurchaseOrderId());

        assertEquals("DRAFT", header.getStatus());
        assertEquals("EUR", header.getCurrencyCode());

        amount("50.40", header.getSubtotalAmount());
        amount("11.09", header.getTaxAmount());
        amount("61.49", header.getTotalAmount());
    }

    @Test
    void copiesAllocatedAmountsWithoutRecalculation() {

        var lines = mapper.toLines(
                plan(), 700L
        );

        assertEquals(1, lines.size());

        var line = lines.get(0);

        assertNull(line.getId());
        assertEquals(2L, line.getTenantId());
        assertEquals(700L, line.getSupplierInvoiceId());
        assertEquals(1, line.getLineNo());

        assertEquals(1L, line.getItemId());
        assertEquals(
                "Articolo collaudo parziale",
                line.getDescription()
        );

        assertEquals(9L, line.getUomId());
        assertEquals("BOX", line.getUomCode());
        assertEquals("PZ", line.getBaseUomCode());

        amount("10.000000", line.getConversionFactor());
        amount("0.700", line.getQuantity());
        amount("72.00", line.getUnitPrice());
        amount("0.00", line.getDiscountPct());
        amount("22.00", line.getTaxPct());

        amount("50.40", line.getLineSubtotal());
        amount("11.09", line.getTaxAmount());
        amount("61.49", line.getLineTotal());

        assertThrows(
                UnsupportedOperationException.class,
                () -> lines.clear()
        );
    }

    @Test
    void rejectsMissingDocumentInformation() {

        var plan = plan();

        assertThrows(
                BusinessException.class,
                () -> mapper.toHeader(
                        null,
                        "SI-TEST-00001",
                        LocalDate.now()
                )
        );

        assertThrows(
                BusinessException.class,
                () -> mapper.toHeader(
                        plan,
                        " ",
                        LocalDate.now()
                )
        );

        assertThrows(
                BusinessException.class,
                () -> mapper.toHeader(
                        plan,
                        "SI-TEST-00001",
                        null
                )
        );

        assertThrows(
                BusinessException.class,
                () -> mapper.toLines(
                        null, 700L
                )
        );

        assertThrows(
                BusinessException.class,
                () -> mapper.toLines(
                        plan, null
                )
        );

        assertThrows(
                BusinessException.class,
                () -> mapper.toLines(
                        plan, 0L
                )
        );
    }
}
