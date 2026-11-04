package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPartialFifoLineCalculatorTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static void equal(
            String expected,
            BigDecimal actual) {

        assertEquals(
                0,
                bd(expected).compareTo(actual)
        );
    }

    private static void verifyMovementTotal(
            GoodsReceiptPartialFifoLineCalculator.PlannedLine line) {

        BigDecimal movementTotal =
                line.quantityReceived()
                        .multiply(line.unitCost())
                        .setScale(4, RoundingMode.HALF_UP);

        assertEquals(
                0,
                line.totalCost().compareTo(movementTotal),
                "Movimento FIFO e costo documento non quadrano."
        );
    }

    @Test
    void firstReceiptEightPiecesReconciles() {

        var line =
                GoodsReceiptPartialFifoLineCalculator.calculate(
                        bd("20.000"),
                        bd("0.000"),
                        bd("8.000"),
                        bd("144.00")
                );

        equal("8.000", line.quantityReceived());
        equal("7.2000", line.unitCost());
        equal("57.6000", line.totalCost());

        verifyMovementTotal(line);

        System.out.println(
                "PARTIAL FIFO LINE: primo ricevimento 8 PZ = 57.6000"
        );
    }

    @Test
    void secondReceiptCompletesOriginalCost() {

        var first =
                GoodsReceiptPartialFifoLineCalculator.calculate(
                        bd("20.000"),
                        bd("0.000"),
                        bd("8.000"),
                        bd("144.00")
                );

        var second =
                GoodsReceiptPartialFifoLineCalculator.calculate(
                        bd("20.000"),
                        bd("8.000"),
                        bd("12.000"),
                        bd("144.00")
                );

        equal("12.000", second.quantityReceived());
        equal("7.2000", second.unitCost());
        equal("86.4000", second.totalCost());

        verifyMovementTotal(first);
        verifyMovementTotal(second);

        equal(
                "144.0000",
                first.totalCost().add(second.totalCost())
        );

        System.out.println(
                "PARTIAL FIFO LINE: totale riconciliato = 144.0000"
        );
    }

    @Test
    void rejectsNonRepresentableFifoCost() {

        // 1 / 3 = 0.3333 con quattro decimali.
        // 3 x 0.3333 = 0.9999, non 1.0000.
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialFifoLineCalculator.calculate(
                        bd("3.000"),
                        bd("0.000"),
                        bd("3.000"),
                        bd("1.00")
                )
        );

        System.out.println(
                "PARTIAL FIFO LINE: costo non riconciliabile rifiutato"
        );
    }

    @Test
    void rejectsOverReceipt() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialFifoLineCalculator.calculate(
                        bd("20.000"),
                        bd("8.000"),
                        bd("13.000"),
                        bd("144.00")
                )
        );

        System.out.println(
                "PARTIAL FIFO LINE: quantità eccedente rifiutata"
        );
    }

    @Test
    void rejectsMissingEconomicData() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialFifoLineCalculator.calculate(
                        bd("20.000"),
                        bd("0.000"),
                        bd("8.000"),
                        null
                )
        );

        System.out.println(
                "PARTIAL FIFO LINE: costo mancante rifiutato"
        );
    }
}
