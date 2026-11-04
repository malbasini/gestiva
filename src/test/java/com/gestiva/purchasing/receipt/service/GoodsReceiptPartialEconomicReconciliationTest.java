package com.gestiva.purchasing.receipt.service;

import com.gestiva.purchasing.invoice.service
        .SupplierInvoicePartialBillabilityValidator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPartialEconomicReconciliationTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static void equalAmount(
            String expected, BigDecimal actual) {
        assertEquals(
                0, bd(expected).compareTo(actual),
                "Importo o quantità non corrispondente."
        );
    }

    @Test
    void twoReceiptsReconcileOriginalOrder() {

        BigDecimal ordered = bd("20.000");
        BigDecimal conversionFactor = bd("10.000000");

        BigDecimal first = bd("8.000");
        BigDecimal second = bd("12.000");

        BigDecimal cost1 =
                GoodsReceiptPartialCostAllocator.allocate(
                        ordered, bd("0.000"),
                        first, bd("144.00")
                );

        BigDecimal cost2 =
                GoodsReceiptPartialCostAllocator.allocate(
                        ordered, first,
                        second, bd("144.00")
                );

        var invoice1 =
                SupplierInvoicePartialBillabilityValidator.validate(
                        ordered, bd("0.000"),
                        first, conversionFactor,
                        bd("144.00"), bd("31.68"), bd("175.68")
                );

        var invoice2 =
                SupplierInvoicePartialBillabilityValidator.validate(
                        ordered, first,
                        second, conversionFactor,
                        bd("144.00"), bd("31.68"), bd("175.68")
                );

        equalAmount("8.000", first);
        equalAmount("12.000", second);
        equalAmount("20.000", first.add(second));

        equalAmount("0.800", invoice1.commercialQuantity());
        equalAmount("1.200", invoice2.commercialQuantity());

        equalAmount(
                "2.000",
                invoice1.commercialQuantity().add(
                        invoice2.commercialQuantity()
                )
        );

        equalAmount("57.6000", cost1);
        equalAmount("86.4000", cost2);
        equalAmount("144.0000", cost1.add(cost2));

        equalAmount("57.60", invoice1.subtotal());
        equalAmount("86.40", invoice2.subtotal());

        equalAmount(
                "144.00",
                invoice1.subtotal().add(invoice2.subtotal())
        );

        equalAmount("12.67", invoice1.tax());
        equalAmount("19.01", invoice2.tax());

        equalAmount(
                "31.68",
                invoice1.tax().add(invoice2.tax())
        );

        equalAmount("70.27", invoice1.total());
        equalAmount("105.41", invoice2.total());

        equalAmount(
                "175.68",
                invoice1.total().add(invoice2.total())
        );

        System.out.println(
                "PARTIAL ECONOMICS: quantità 8 + 12 = 20 PZ"
        );
        System.out.println(
                "PARTIAL ECONOMICS: commerciale 0.8 + 1.2 = 2 BOX"
        );
        System.out.println(
                "PARTIAL ECONOMICS: FIFO 57.6000 + 86.4000 = 144.0000"
        );
        System.out.println(
                "PARTIAL ECONOMICS: imponibile = 144.00"
        );
        System.out.println(
                "PARTIAL ECONOMICS: IVA = 31.68"
        );
        System.out.println(
                "PARTIAL ECONOMICS: totale = 175.68"
        );
    }

    @Test
    void excessiveSecondReceiptIsRejected() {

        assertThrows(
                RuntimeException.class,
                () -> GoodsReceiptPartialCostAllocator.allocate(
                        bd("20.000"),
                        bd("8.000"),
                        bd("13.000"),
                        bd("144.00")
                )
        );

        assertThrows(
                RuntimeException.class,
                () -> SupplierInvoicePartialBillabilityValidator.validate(
                        bd("20.000"),
                        bd("8.000"),
                        bd("13.000"),
                        bd("10.000000"),
                        bd("144.00"),
                        bd("31.68"),
                        bd("175.68")
                )
        );

        System.out.println(
                "PARTIAL ECONOMICS: superamento ordine rifiutato"
        );
    }

    @Test
    void nonRepresentableCommercialQuantityIsRejected() {

        assertThrows(
                RuntimeException.class,
                () -> SupplierInvoicePartialBillabilityValidator.validate(
                        bd("20.000"),
                        bd("0.000"),
                        bd("0.001"),
                        bd("10.000000"),
                        bd("144.00"),
                        bd("31.68"),
                        bd("175.68")
                )
        );

        System.out.println(
                "PARTIAL ECONOMICS: conversione UOM non esatta rifiutata"
        );
    }
}
