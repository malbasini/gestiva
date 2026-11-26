package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingRequestValidatorRobustnessTest {

    private static final class Request {
        Long tenantId = 2L;
        Long companyId = 1L;
        Long warehouseId = 5L;
        Long itemId = 10L;
        Long sourceLocationId = 100L;
        Long targetLocationId = 200L;
        BigDecimal quantity = new BigDecimal("7.000");
        LocalDate movementDate = LocalDate.of(2026, 11, 24);
        String notes = null;
        String operationKey = "APA-TRANSFER-001";

        void validate() {
            InventoryLocationTransferPostingRequestValidator.validate(
                    tenantId,
                    companyId,
                    warehouseId,
                    itemId,
                    sourceLocationId,
                    targetLocationId,
                    quantity,
                    movementDate,
                    notes,
                    operationKey
            );
        }
    }

    private static void accept(Consumer<Request> changes) {
        var request = new Request();
        changes.accept(request);

        assertDoesNotThrow(
                request::validate
        );
    }

    private static void reject(Consumer<Request> changes) {
        var request = new Request();
        changes.accept(request);

        assertThrows(
                BusinessException.class,
                request::validate
        );
    }

    // 3 richieste valide.

    @Test
    void acceptsOrdinaryValidRequest() {
        accept(request -> {});
    }

    @Test
    void acceptsMaximumDecimalQuantity() {
        accept(request ->
                request.quantity =
                        new BigDecimal("999999999999.999")
        );
    }

    @Test
    void acceptsMaximumKeyAndNotesLength() {
        accept(request -> {
            request.operationKey = "K".repeat(120);
            request.notes = "N".repeat(1000);
        });
    }

    // Identificativi di contesto e ubicazioni V13.

    @Test
    void rejectsInvalidTenant() {
        reject(request -> request.tenantId = null);
    }

    @Test
    void rejectsInvalidCompany() {
        reject(request -> request.companyId = 0L);
    }

    @Test
    void rejectsInvalidWarehouse() {
        reject(request -> request.warehouseId = -1L);
    }

    @Test
    void rejectsInvalidItem() {
        reject(request -> request.itemId = 0L);
    }

    @Test
    void acceptsUnlocatedSource() {
        accept(request -> request.sourceLocationId = null);
    }

    @Test
    void rejectsInvalidSourceLocation() {
        reject(request -> request.sourceLocationId = 0L);
    }

    @Test
    void rejectsNegativeSourceLocation() {
        reject(request -> request.sourceLocationId = -1L);
    }

    @Test
    void rejectsNullTargetLocation() {
        reject(request -> request.targetLocationId = null);
    }

    @Test
    void rejectsBothLocationsNull() {
        reject(request -> {
            request.sourceLocationId = null;
            request.targetLocationId = null;
        });
    }

    @Test
    void rejectsZeroTargetWithUnlocatedSource() {
        reject(request -> {
            request.sourceLocationId = null;
            request.targetLocationId = 0L;
        });
    }

    @Test
    void rejectsNegativeTargetWithUnlocatedSource() {
        reject(request -> {
            request.sourceLocationId = null;
            request.targetLocationId = -1L;
        });
    }

    @Test
    void rejectsInvalidTargetLocation() {
        reject(request -> request.targetLocationId = -3L);
    }

    // 1 verifica di diversità delle ubicazioni.

    @Test
    void rejectsSameSourceAndTargetLocation() {
        reject(request -> request.targetLocationId = 100L);
    }

    // 5 verifiche quantitative.

    @Test
    void rejectsNullQuantity() {
        reject(request -> request.quantity = null);
    }

    @Test
    void rejectsZeroQuantity() {
        reject(request ->
                request.quantity = new BigDecimal("0.000")
        );
    }

    @Test
    void rejectsNegativeQuantity() {
        reject(request ->
                request.quantity = new BigDecimal("-1.000")
        );
    }

    @Test
    void rejectsMoreThanThreeSignificantDecimals() {
        reject(request ->
                request.quantity = new BigDecimal("1.0001")
        );
    }

    @Test
    void rejectsQuantityExceedingDecimal15Scale3() {
        reject(request ->
                request.quantity =
                        new BigDecimal("1000000000000.000")
        );
    }

    // 1 verifica della data obbligatoria.

    @Test
    void rejectsMissingMovementDate() {
        reject(request -> request.movementDate = null);
    }

    // 4 verifiche della chiave di idempotenza.

    @Test
    void rejectsNullOperationKey() {
        reject(request -> request.operationKey = null);
    }

    @Test
    void rejectsBlankOperationKey() {
        reject(request -> request.operationKey = "   ");
    }

    @Test
    void rejectsOperationKeyLongerThan120() {
        reject(request ->
                request.operationKey = "K".repeat(121)
        );
    }

    @Test
    void rejectsOperationKeyWithSurroundingWhitespace() {
        reject(request ->
                request.operationKey = " APA-TRANSFER-001 "
        );
    }

    // 1 verifica delle note.

    @Test
    void rejectsNotesLongerThan1000() {
        reject(request ->
                request.notes = "N".repeat(1001)
        );
    }
}
