package com.gestiva.purchasing.invoice.service;

import com.gestiva.accounting.due.service.PaymentDueService;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("integration")
class SupplierInvoiceRollbackIT extends AbstractMySqlIntegrationTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;

    @Autowired
    private SupplierInvoiceService supplierInvoiceService;

    @Autowired
    private JdbcTemplate jdbc;

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private CompanyContext companyContext;

    @MockitoBean
    private JournalAutoPostingService journalAutoPostingService;

    @MockitoBean
    private PaymentDueService paymentDueService;

    @Test
    void registerSupplierInvoice_shouldRollbackStatusOnPostingFailure() {

        // Numero univoco: non utilizziamo fatture preesistenti.
        String invoiceNumber =
                "IT-RBK-" + UUID.randomUUID().toString();

        Long invoiceId = null;

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        try {

            // Fixture autonoma nel solo database gestiva_test.
            // Verrà eliminata nel blocco finally.
            jdbc.update(
                    """
                    INSERT INTO supplier_invoice (
                        tenant_id,
                        company_id,
                        invoice_number,
                        invoice_date,
                        supplier_id,
                        goods_receipt_id,
                        purchase_order_id,
                        status,
                        currency_code,
                        notes,
                        subtotal_amount,
                        tax_amount,
                        total_amount,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT_ID,
                    COMPANY_ID,
                    invoiceNumber,
                    java.sql.Date.valueOf(
                            LocalDate.of(2026, 10, 31)
                    ),
                    1L,
                    1L,
                    1L,
                    "DRAFT",
                    "EUR",
                    "Fixture rollback integrazione",
                    new BigDecimal("100.00"),
                    new BigDecimal("22.00"),
                    new BigDecimal("122.00")
            );

            invoiceId = jdbc.queryForObject(
                    """
                    SELECT id
                    FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND invoice_number = ?
                    """,
                    Long.class,
                    TENANT_ID,
                    COMPANY_ID,
                    invoiceNumber
            );

            assertNotNull(invoiceId);

            Long finalInvoiceId = invoiceId;

            // Verifichiamo che l'UPDATE sia arrivato al DB
            // prima di simulare l'errore contabile.
            doAnswer(invocation -> {

                entityManager.flush();

                Integer registered = jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM supplier_invoice
                        WHERE id = ?
                          AND tenant_id = ?
                          AND company_id = ?
                          AND status = 'REGISTERED'
                        """,
                        Integer.class,
                        finalInvoiceId,
                        TENANT_ID,
                        COMPANY_ID
                );

                assertEquals(1, registered);

                throw new IllegalStateException(
                        "IT: errore simulato contabilizzazione"
                );

            }).when(journalAutoPostingService)
                    .postSupplierInvoice(
                            anyLong(),
                            any(LocalDate.class),
                            anyString(),
                            any(BigDecimal.class),
                            any(BigDecimal.class),
                            any(BigDecimal.class),
                            anyString(),
                            anyLong(),
                            anyString()
                    );

            // Questa chiamata attraversa il proxy transazionale
            // Spring del servizio reale.
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> supplierInvoiceService
                            .registerSupplierInvoice(
                                    TENANT_ID,
                                    finalInvoiceId
                            )
            );

            assertEquals(
                    "IT: errore simulato contabilizzazione",
                    exception.getMessage()
            );

            // Nuova lettura dopo la conclusione della transazione
            // del servizio: lo stato deve essere tornato DRAFT.
            String status = jdbc.queryForObject(
                    """
                    SELECT status
                    FROM supplier_invoice
                    WHERE id = ?
                      AND tenant_id = ?
                      AND company_id = ?
                    """,
                    String.class,
                    finalInvoiceId,
                    TENANT_ID,
                    COMPANY_ID
            );

            assertEquals("DRAFT", status);

            verify(journalAutoPostingService, times(1))
                    .postSupplierInvoice(
                            anyLong(),
                            any(LocalDate.class),
                            anyString(),
                            any(BigDecimal.class),
                            any(BigDecimal.class),
                            any(BigDecimal.class),
                            anyString(),
                            anyLong(),
                            anyString()
                    );

            // L'errore contabile impedisce di arrivare
            // alla creazione della scadenza.
            verifyNoInteractions(paymentDueService);

        } finally {

            // Pulizia limitata alla sola fattura creata dal test.
            // Non modifica altre fatture o società.
            jdbc.update(
                    """
                    DELETE FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND invoice_number = ?
                    """,
                    TENANT_ID,
                    COMPANY_ID,
                    invoiceNumber
            );
        }
    }
}
