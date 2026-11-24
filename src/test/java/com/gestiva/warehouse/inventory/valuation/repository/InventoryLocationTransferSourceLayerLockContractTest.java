package com.gestiva.warehouse.inventory.valuation.repository;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferSourceLayerLockContractTest {

    @Test
    void transferSourceLayersMustBeLockedAndFifoOrdered() {

        Method method;

        try {
            method = InventoryLayerRepository.class.getMethod(
                    "findTransferSourceLayersForUpdate",
                    Long.class,
                    Long.class,
                    Long.class,
                    Long.class,
                    Long.class
            );
        } catch (NoSuchMethodException exception) {
            fail("STOP-AOH-SOURCE-LAYER-LOCK-NON-IMPLEMENTATO");
            return;
        }

        assertEquals(
                List.class,
                method.getReturnType(),
                "La query deve restituire una lista di layer."
        );

        Lock lock = method.getAnnotation(Lock.class);

        assertNotNull(
                lock,
                "STOP-AOH-LOCK-ANNOTATION-MANCANTE"
        );

        assertEquals(
                LockModeType.PESSIMISTIC_WRITE,
                lock.value(),
                "STOP-AOH-LOCK-NON-PESSIMISTIC-WRITE"
        );

        Query query = method.getAnnotation(Query.class);

        assertNotNull(
                query,
                "STOP-AOH-QUERY-ANNOTATION-MANCANTE"
        );

        String jpql = query.value()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);

        String[] required = {
                "l.tenantid = :tenantid",
                "l.companyid = :companyid",
                "l.warehouseid = :warehouseid",
                "l.itemid = :itemid",
                "l.locationid = :sourcelocationid",
                "l.closed = false",
                "order by l.layerdate asc, l.id asc"
        };

        for (String fragment : required) {
            assertTrue(
                    jpql.contains(fragment),
                    "STOP-AOH-FILTRO-FIFO-MANCANTE: " + fragment
            );
        }
    }
}
