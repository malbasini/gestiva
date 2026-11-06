package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Writer interno della fatturazione fornitori parziale.
 *
 * Non assegna numeratori, non acquisisce il lock del PO
 * e non genera effetti contabili.
 *
 * Il futuro coordinatore di creazione deve chiamarlo
 * nella STESSA transazione in cui ha acquisito il lock
 * dell'ordine e costruito il piano.
 *
 * Non esporre questo componente a controller.
 */
@Component
class SupplierInvoicePartialDraftPersistenceWriter {

    private final SupplierInvoicePartialGenerationGuard gate;
    private final SupplierInvoicePartialIsolationGuard isolation;
    private final SupplierInvoicePartialDraftEntityMapper mapper;
    private final SupplierInvoiceRepository invoices;
    private final SupplierInvoiceLineRepository lines;

    SupplierInvoicePartialDraftPersistenceWriter(
            SupplierInvoicePartialGenerationGuard gate,
            SupplierInvoicePartialIsolationGuard isolation,
            SupplierInvoicePartialDraftEntityMapper mapper,
            SupplierInvoiceRepository invoices,
            SupplierInvoiceLineRepository lines) {

        this.gate = gate;
        this.isolation = isolation;
        this.mapper = mapper;
        this.invoices = invoices;
        this.lines = lines;
    }

    @Transactional(
            propagation = Propagation.MANDATORY,
            rollbackFor = Exception.class
    )
    public Long write(
            SupplierInvoicePartialDraftPlan plan,
            String invoiceNumber,
            LocalDate invoiceDate) {

        // Verifiche preliminari, prima di qualsiasi INSERT.
        gate.requireEnabled();
        isolation.requireReadCommittedWriteTransaction();

        if (plan == null) {
            throw new BusinessException(
                    "Piano fattura parziale mancante."
            );
        }

        // Il mapper valida numero e data e costruisce
        // una testata DRAFT con gli importi già allocati.
        SupplierInvoice header =
                mapper.toHeader(
                        plan,
                        invoiceNumber,
                        invoiceDate
                );

        if (header.getId() != null
                || !"DRAFT".equals(header.getStatus())) {
            throw new BusinessException(
                    "Testata DRAFT non valida."
            );
        }

        SupplierInvoice saved = invoices.save(header);

        if (saved == null
                || saved.getId() == null
                || saved.getId() <= 0) {
            throw new BusinessException(
                    "ID fattura parziale non generato."
            );
        }

        // Copia senza ricalcolo quantità/prezzi/IVA.
        var rows = mapper.toLines(
                plan,
                saved.getId()
        );

        if (rows.isEmpty()) {
            throw new BusinessException(
                    "Fattura parziale senza righe."
            );
        }

        lines.saveAll(rows);

        // Nessuna prima nota, scadenza o registrazione.
        return saved.getId();
    }
}
