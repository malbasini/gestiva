package com.gestiva.purchasing.invoice.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoiceLineUomTest {

    @Test
    void shouldPreserveCommercialUomSnapshot() {
        SupplierInvoiceLine line = new SupplierInvoiceLine();

        line.setQuantity(new BigDecimal("2.000"));
        line.setUomId(15L);
        line.setUomCode("BOX");
        line.setBaseUomCode("PZ");
        line.setConversionFactor(new BigDecimal("10.000000"));

        assertEquals(15L, line.getUomId());
        assertEquals("BOX", line.getUomCode());
        assertEquals("PZ", line.getBaseUomCode());
        assertEquals(
                0,
                new BigDecimal("10").compareTo(
                        line.getConversionFactor()
                )
        );
    }

    @Test
    void shouldConvertCommercialQuantityToBaseQuantity() {
        SupplierInvoiceLine line = new SupplierInvoiceLine();

        line.setQuantity(new BigDecimal("2.000"));
        line.setUomCode("BOX");
        line.setBaseUomCode("PZ");
        line.setConversionFactor(new BigDecimal("10.000000"));

        BigDecimal baseQuantity =
                line.getQuantity().multiply(
                        line.getConversionFactor()
                );

        assertEquals(
                0,
                new BigDecimal("20").compareTo(baseQuantity)
        );

        assertEquals("BOX", line.getUomCode());
        assertEquals("PZ", line.getBaseUomCode());
    }

    @Test
    void shouldAllowHistoricalLineWithoutUomSnapshot() {
        SupplierInvoiceLine line = new SupplierInvoiceLine();

        line.setQuantity(new BigDecimal("2.000"));

        assertNull(line.getUomId());
        assertNull(line.getUomCode());
        assertNull(line.getBaseUomCode());
        assertNull(line.getConversionFactor());

        assertEquals(
                0,
                new BigDecimal("2").compareTo(line.getQuantity())
        );
    }
}
