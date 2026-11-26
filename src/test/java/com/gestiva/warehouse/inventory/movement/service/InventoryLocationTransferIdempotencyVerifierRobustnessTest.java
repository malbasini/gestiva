package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferIdempotencyVerifierRobustnessTest {

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 24);

    private static final class Request {
        Long tenantId = 2L;
        Long companyId = 1L;
        Long warehouseId = 5L;
        Long itemId = 10L;
        Long sourceLocationId = 100L;
        Long targetLocationId = 200L;
        BigDecimal quantity = new BigDecimal("7.000");
        LocalDate movementDate = DATE;

        void verify(InventoryLocationTransfer existing) {
            InventoryLocationTransferIdempotencyVerifier.verifySameRequest(
                    existing,
                    tenantId,
                    companyId,
                    warehouseId,
                    itemId,
                    sourceLocationId,
                    targetLocationId,
                    quantity,
                    movementDate
            );
        }
    }

    private static InventoryLocationTransfer existingTransfer() {
        InventoryLocationTransfer transfer =
                new InventoryLocationTransfer();

        transfer.setTenantId(2L);
        transfer.setCompanyId(1L);
        transfer.setWarehouseId(5L);
        transfer.setItemId(10L);
        transfer.setSourceLocationId(100L);
        transfer.setTargetLocationId(200L);
        transfer.setQuantity(new BigDecimal("7.000"));
        transfer.setMovementDate(DATE);

        return transfer;
    }

    private static void accept(
            Consumer<InventoryLocationTransfer> existingChanges,
            Consumer<Request> requestChanges) {

        var existing = existingTransfer();
        var request = new Request();

        existingChanges.accept(existing);
        requestChanges.accept(request);

        assertDoesNotThrow(() -> request.verify(existing));
    }

    private static void reject(
            Consumer<InventoryLocationTransfer> existingChanges,
            Consumer<Request> requestChanges) {

        var existing = existingTransfer();
        var request = new Request();

        existingChanges.accept(existing);
        requestChanges.accept(request);

        assertThrows(
                BusinessException.class,
                () -> request.verify(existing)
        );
    }

    // 1-2: richieste equivalenti.

    @Test
    void acceptsIdenticalRequest() {
        accept(existing -> {}, request -> {});
    }

    @Test
    void acceptsEquivalentQuantityWithDifferentScale() {
        accept(
                existing -> existing.setQuantity(
                        new BigDecimal("7.00")
                ),
                request -> request.quantity =
                        new BigDecimal("7.000")
        );
    }

    // 3: trasferimento inesistente.

    @Test
    void rejectsNullExistingTransfer() {
        var request = new Request();

        assertThrows(
                BusinessException.class,
                () -> request.verify(null)
        );
    }

    // 4-9: isolamento degli identificativi.

    @Test
    void rejectsDifferentTenant() {
        reject(
                existing -> existing.setTenantId(3L),
                request -> {}
        );
    }

    @Test
    void rejectsDifferentCompany() {
        reject(
                existing -> existing.setCompanyId(4L),
                request -> {}
        );
    }

    @Test
    void rejectsDifferentWarehouse() {
        reject(
                existing -> existing.setWarehouseId(6L),
                request -> {}
        );
    }

    @Test
    void rejectsDifferentItem() {
        reject(
                existing -> existing.setItemId(11L),
                request -> {}
        );
    }

    @Test
    void rejectsDifferentSourceLocation() {
        reject(
                existing -> existing.setSourceLocationId(101L),
                request -> {}
        );
    }

    @Test
    void rejectsDifferentTargetLocation() {
        reject(
                existing -> existing.setTargetLocationId(201L),
                request -> {}
        );
    }

    // 10: data FIFO/movimento.

    @Test
    void rejectsDifferentMovementDate() {
        reject(
                existing -> existing.setMovementDate(
                        DATE.plusDays(1)
                ),
                request -> {}
        );
    }

    // 11-15: quantità e precisione.

    @Test
    void rejectsDifferentQuantity() {
        reject(
                existing -> existing.setQuantity(
                        new BigDecimal("8.000")
                ),
                request -> {}
        );
    }

    @Test
    void rejectsNullExistingQuantity() {
        reject(
                existing -> existing.setQuantity(null),
                request -> {}
        );
    }

    @Test
    void rejectsNullRequestedQuantity() {
        reject(
                existing -> {},
                request -> request.quantity = null
        );
    }

    @Test
    void rejectsMoreThanThreeSignificantDecimals() {
        reject(
                existing -> {},
                request -> request.quantity =
                        new BigDecimal("7.0001")
        );
    }

    @Test
    void rejectsQuantityBeyondDecimal15Scale3() {
        reject(
                existing -> {},
                request -> request.quantity =
                        new BigDecimal("1000000000000.000")
        );
    }

    // 16-18: integrità strutturale della richiesta.

    @Test
    void rejectsSameSourceAndTargetLocation() {
        reject(
                existing -> {},
                request -> request.targetLocationId = 100L
        );
    }

    @Test
    void rejectsMissingMovementDate() {
        reject(
                existing -> {},
                request -> request.movementDate = null
        );
    }

    @Test
    void rejectsInvalidCompanyIdentifier() {
        reject(
                existing -> {},
                request -> request.companyId = 0L
        );
    }

    @Test
    void acceptsIdenticalUnlocatedRequest() {
        accept(
                existing -> existing.setSourceLocationId(null),
                request -> request.sourceLocationId = null
        );
    }

    @Test
    void rejectsLocatedRequestForUnlocatedTransfer() {
        reject(
                existing -> existing.setSourceLocationId(null),
                request -> {}
        );
    }

    @Test
    void rejectsUnlocatedRequestForLocatedTransfer() {
        reject(
                existing -> {},
                request -> request.sourceLocationId = null
        );
    }

    @Test
    void rejectsMatchingZeroSourceIdentifiers() {
        reject(
                existing -> existing.setSourceLocationId(0L),
                request -> request.sourceLocationId = 0L
        );
    }

    @Test
    void rejectsMatchingNegativeSourceIdentifiers() {
        reject(
                existing -> existing.setSourceLocationId(-1L),
                request -> request.sourceLocationId = -1L
        );
    }

    @Test
    void rejectsUnlocatedRequestWithNullTarget() {
        reject(
                existing -> {
                    existing.setSourceLocationId(null);
                    existing.setTargetLocationId(null);
                },
                request -> {
                    request.sourceLocationId = null;
                    request.targetLocationId = null;
                }
        );
    }

    @Test
    void rejectsUnlocatedRequestWithZeroTarget() {
        reject(
                existing -> {
                    existing.setSourceLocationId(null);
                    existing.setTargetLocationId(0L);
                },
                request -> {
                    request.sourceLocationId = null;
                    request.targetLocationId = 0L;
                }
        );
    }

    @Test
    void rejectsUnlocatedRequestWithDifferentQuantity() {
        reject(
                existing -> existing.setSourceLocationId(null),
                request -> {
                    request.sourceLocationId = null;
                    request.quantity = new BigDecimal("8.000");
                }
        );
    }

    @Test
    void rejectsUnlocatedRequestWithDifferentCompany() {
        reject(
                existing -> existing.setSourceLocationId(null),
                request -> {
                    request.sourceLocationId = null;
                    request.companyId = 99L;
                }
        );
    }
}
