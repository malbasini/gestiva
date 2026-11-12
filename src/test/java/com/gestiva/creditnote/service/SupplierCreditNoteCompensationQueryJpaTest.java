
package com.gestiva.creditnote.service;

import com.gestiva.accounting.due.repository.PaymentDueRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Transactional(readOnly = true)
class SupplierCreditNoteCompensationQueryJpaTest {

    @Autowired
    private PaymentDueRepository paymentDueRepository;

    @Test
    void compensableSupplierDuesQueryExecutesSuccessfully() {

        // Tenant 2, company 3, fornitore 1.
        // Esecuzione esclusivamente in lettura.
        // Nessuna creazione o modifica di documenti.

        var dues = paymentDueRepository
                .findCompensableSupplierDues(
                        2L,
                        3L,
                        1L,
                        "EUR"
                );

        assertNotNull(dues);

        // Ogni eventuale risultato deve rispettare
        // i criteri fondamentali di eleggibilita'.

        for (var due : dues) {

            assertEquals(3L, due.getCompanyId());
            assertEquals("SUPPLIER", due.getPartyType());
            assertEquals("PAYABLE", due.getDirection());
            assertEquals("SUPPLIER_INVOICE",
                    due.getReferenceType());
            assertEquals("EUR", due.getCurrencyCode());

            assertTrue(
                    List.of("OPEN", "PARTIALLY_PAID")
                            .contains(due.getStatus())
            );

            assertTrue(due.getOpenAmount().signum() > 0);
        }
    }
}
