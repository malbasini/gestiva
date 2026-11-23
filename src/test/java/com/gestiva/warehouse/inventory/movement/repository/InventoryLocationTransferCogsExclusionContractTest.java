package com.gestiva.warehouse.inventory.movement.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferCogsExclusionContractTest {

    @Test
    void costOfGoodsSoldMustExcludeInternalLocationTransfers()
            throws Exception {

        assertExcludesTransfers(
                "summarizeCostOfGoodsSoldByItem",
                Long.class,
                Long.class,
                LocalDate.class,
                LocalDate.class
        );

        assertExcludesTransfers(
                "summarizeCostOfGoodsSoldByCompanyAndItem",
                Long.class,
                Long.class,
                Long.class,
                LocalDate.class,
                LocalDate.class
        );
    }

    private static void assertExcludesTransfers(
            String methodName,
            Class<?>... parameterTypes
    ) throws Exception {

        Method method = InventoryMovementRepository.class
                .getMethod(methodName, parameterTypes);

        Query query = method.getAnnotation(Query.class);

        assertNotNull(query, "Query mancante: " + methodName);

        String jpql = query.value()
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(java.util.Locale.ROOT);

        // Il criterio deve comparire come predicato che
        // esclude LOCATION_TRANSFER dal riepilogo COGS.
        // Il solo riferimento testuale non basta.
        boolean excludesTransfers =
                Pattern.compile(
                        "(?:(?:UPPER\\s*\\(\\s*M\\.REFERENCETYPE"
                        + "\\s*\\)|M\\.REFERENCETYPE)\\s*"
                        + "(?:<>|!=)\\s*'LOCATION_TRANSFER'"
                        + "|'LOCATION_TRANSFER'\\s*"
                        + "(?:<>|!=)\\s*"
                        + "(?:UPPER\\s*\\(\\s*M\\.REFERENCETYPE"
                        + "\\s*\\)|M\\.REFERENCETYPE))"
                ).matcher(jpql).find();

        assertTrue(
                excludesTransfers,
                "STOP-ANM-COGS-TRANSFER-NON-ESCLUSO: "
                + methodName
        );
    }
}
