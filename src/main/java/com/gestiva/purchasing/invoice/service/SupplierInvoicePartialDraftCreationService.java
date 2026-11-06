package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.settings.sequence.service.DocumentSequenceService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Coordinatore interno della futura creazione DRAFT
 * delle fatture fornitori da ricevimenti parziali.
 *
 * Non esposto ad alcun controller.
 * Non modifica il servizio tradizionale.
 *
 * Il lock pessimistico dell'ordine viene acquisito
 * dalla preparazione e mantenuto nella stessa
 * transazione fino al completamento della scrittura.
 *
 * Non accetta piani precalcolati dall'esterno.
 * Nessun effetto contabile o scadenzario.
 */
@Service
class SupplierInvoicePartialDraftCreationService {

    private final SupplierInvoicePartialPreparationService
            preparation;

    private final SupplierInvoicePartialDraftAssembler
            assembler;

    private final DocumentSequenceService sequences;

    private final SupplierInvoicePartialDraftPersistenceWriter
            writer;

    SupplierInvoicePartialDraftCreationService(
            SupplierInvoicePartialPreparationService preparation,
            SupplierInvoicePartialDraftAssembler assembler,
            DocumentSequenceService sequences,
            SupplierInvoicePartialDraftPersistenceWriter writer) {

        this.preparation = preparation;
        this.assembler = assembler;
        this.sequences = sequences;
        this.writer = writer;
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Long create(Long tenantId, Long goodsReceiptId) {

        // La preparazione verifica prima il feature gate
        // e l'isolamento JDBC effettivo.
        // Successivamente acquisisce il lock del PO.
        var prepared = preparation.prepare(
                tenantId,
                goodsReceiptId
        );

        // Il piano è costruito mentre il lock
        // dell'ordine è ancora mantenuto.
        if (prepared == null
                || prepared.receipt() == null
                || prepared.purchaseOrder() == null) {
            throw new BusinessException(
                    "Preparazione fattura parziale non valida."
            );
        }

        var plan = assembler.assemble(prepared);

        if (plan == null
                || !Objects.equals(
                        plan.tenantId(), tenantId)
                || !Objects.equals(
                        plan.goodsReceiptId(), goodsReceiptId)
                || !Objects.equals(
                        plan.tenantId(), prepared.tenantId())
                || !Objects.equals(
                        plan.companyId(), prepared.companyId())
                || !Objects.equals(
                        plan.purchaseOrderId(),
                        prepared.purchaseOrder().getId())
                || !Objects.equals(
                        plan.supplierId(),
                        prepared.purchaseOrder().getSupplierId())
                || !Objects.equals(
                        prepared.receipt().getId(),
                        plan.goodsReceiptId())
                || !Objects.equals(
                        prepared.receipt().getPurchaseOrderId(),
                        plan.purchaseOrderId())
                || !Objects.equals(
                        prepared.receipt().getSupplierId(),
                        plan.supplierId())) {

            throw new BusinessException(
                    "Piano fattura parziale incoerente "
                    + "con la preparazione transazionale."
            );
        }

        // Il numero viene assegnato soltanto
        // dopo la costruzione del piano validato.
        // L'incremento partecipa alla stessa TX.
        String invoiceNumber = sequences.nextNumber(
                plan.tenantId(),
                plan.companyId(),
                DocumentSequenceService.SUPPLIER_INVOICE
        );

        // Writer MANDATORY: controlla nuovamente
        // feature gate e isolamento effettivo.
        // Salva esclusivamente testata e righe DRAFT.
        return writer.write(
                plan,
                invoiceNumber,
                LocalDate.now()
        );
    }
}
