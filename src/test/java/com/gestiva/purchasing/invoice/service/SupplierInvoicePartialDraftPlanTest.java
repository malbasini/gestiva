package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialDraftPlanTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private SupplierInvoicePartialDraftPlan.Line line(
            int number,
            long orderLineId,
            String quantity,
            String subtotal,
            String tax,
            String total) {

        return new SupplierInvoicePartialDraftPlan.Line(
                number,
                orderLineId,
                50L,
                "Articolo test",
                5L,
                "BOX",
                "PZ",
                bd("10.000000"),
                bd(quantity),
                bd("72.00"),
                bd("0.00"),
                bd("22.00"),
                bd(subtotal),
                bd(tax),
                bd(total)
        );
    }

    private SupplierInvoicePartialDraftPlan plan(
            List<SupplierInvoicePartialDraftPlan.Line> lines,
            String subtotal,
            String tax,
            String total) {

        return new SupplierInvoicePartialDraftPlan(
                2L,
                1L,
                301L,
                100L,
                40L,
                "EUR",
                lines,
                bd(subtotal),
                bd(tax),
                bd(total)
        );
    }

    @Test
    void threePartialReceiptsReconcileExactly() {
        var first = plan(
                List.of(line(
                        1, 101L, "0.800",
                        "57.60", "12.67", "70.27"
                )),
                "57.60", "12.67", "70.27"
        );

        var second = plan(
                List.of(line(
                        1, 101L, "0.700",
                        "50.40", "11.09", "61.49"
                )),
                "50.40", "11.09", "61.49"
        );

        var third = plan(
                List.of(line(
                        1, 101L, "0.500",
                        "36.00", "7.92", "43.92"
                )),
                "36.00", "7.92", "43.92"
        );

        BigDecimal quantities =
                first.lines().get(0).commercialQuantity()
                        .add(second.lines().get(0)
                                .commercialQuantity())
                        .add(third.lines().get(0)
                                .commercialQuantity());

        assertEquals(
                0, bd("2.000").compareTo(quantities)
        );

        BigDecimal subtotal =
                first.subtotal().add(second.subtotal())
                        .add(third.subtotal());

        BigDecimal tax =
                first.tax().add(second.tax())
                        .add(third.tax());

        BigDecimal total =
                first.total().add(second.total())
                        .add(third.total());

        assertEquals(0, bd("144.00").compareTo(subtotal));
        assertEquals(0, bd("31.68").compareTo(tax));
        assertEquals(0, bd("175.68").compareTo(total));
    }

    @Test
    void acceptsCumulativeRoundingWithoutRecalculation() {
        var draft = plan(
                List.of(line(
                        1, 101L, "0.800",
                        "57.60", "12.67", "70.27"
                )),
                "57.60", "12.67", "70.27"
        );

        assertEquals(
                bd("12.67"), draft.lines().get(0).tax()
        );

        assertEquals(
                bd("70.27"), draft.lines().get(0).total()
        );
    }

    @Test
    void rejectsDuplicatePurchaseOrderLines() {
        var first = line(
                1, 101L, "0.800",
                "57.60", "12.67", "70.27"
        );

        var duplicate = line(
                2, 101L, "0.700",
                "50.40", "11.09", "61.49"
        );

        assertThrows(
                BusinessException.class,
                () -> plan(
                        List.of(first, duplicate),
                        "108.00", "23.76", "131.76"
                )
        );
    }

    @Test
    void rejectsNonConsecutiveLineNumbers() {
        var first = line(
                1, 101L, "0.800",
                "57.60", "12.67", "70.27"
        );

        var second = line(
                3, 102L, "0.700",
                "50.40", "11.09", "61.49"
        );

        assertThrows(
                BusinessException.class,
                () -> plan(
                        List.of(first, second),
                        "108.00", "23.76", "131.76"
                )
        );
    }

    @Test
    void rejectsHeaderTotalsDifferentFromLines() {
        assertThrows(
                BusinessException.class,
                () -> plan(
                        List.of(line(
                                1, 101L, "0.800",
                                "57.60", "12.67", "70.27"
                        )),
                        "57.60", "12.68", "70.28"
                )
        );
    }

    @Test
    void rejectsLineWithIncoherentAmounts() {
        assertThrows(
                BusinessException.class,
                () -> line(
                        1, 101L, "0.800",
                        "57.60", "12.67", "70.28"
                )
        );
    }

    @Test
    void rejectsCommercialQuantityWithExcessPrecision() {
        assertThrows(
                BusinessException.class,
                () -> line(
                        1, 101L, "0.8001",
                        "57.60", "12.67", "70.27"
                )
        );
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThrows(
                BusinessException.class,
                () -> line(
                        1, 101L, "0.800",
                        "-57.60", "12.67", "70.27"
                )
        );
    }

    @Test
    void rejectsAmountsBeyondDecimalCapacity() {
        assertThrows(
                BusinessException.class,
                () -> line(
                        1, 101L, "0.800",
                        "10000000000000.00",
                        "0.00",
                        "10000000000000.00"
                )
        );
    }

    @Test
    void rejectsInvalidDiscount() {
        assertThrows(
                BusinessException.class,
                () -> new SupplierInvoicePartialDraftPlan.Line(
                        1,
                        101L,
                        50L,
                        "Articolo test",
                        5L,
                        "BOX",
                        "PZ",
                        bd("10.000000"),
                        bd("0.800"),
                        bd("72.00"),
                        bd("101.00"),
                        bd("22.00"),
                        bd("57.60"),
                        bd("12.67"),
                        bd("70.27")
                )
        );
    }

    @Test
    void linesAreDefensivelyCopied() {
        List<SupplierInvoicePartialDraftPlan.Line> input =
                new ArrayList<>();

        input.add(line(
                1, 101L, "0.800",
                "57.60", "12.67", "70.27"
        ));

        var draft = plan(
                input, "57.60", "12.67", "70.27"
        );

        input.clear();

        assertEquals(1, draft.lines().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> draft.lines().clear()
        );
    }

    @Test
    void rejectsMissingOrInvalidHeader() {
        assertThrows(
                BusinessException.class,
                () -> new SupplierInvoicePartialDraftPlan(
                        2L,
                        1L,
                        301L,
                        100L,
                        40L,
                        "EURO",
                        List.of(line(
                                1, 101L, "0.800",
                                "57.60", "12.67", "70.27"
                        )),
                        bd("57.60"),
                        bd("12.67"),
                        bd("70.27")
                )
        );
    }
}
