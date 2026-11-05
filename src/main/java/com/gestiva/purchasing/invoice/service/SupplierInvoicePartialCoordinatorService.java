package com.gestiva.purchasing.invoice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordina la pianificazione di una fattura fornitore
 * derivata da un ricevimento parziale.
 *
 * La preparazione acquisisce il lock pessimistico
 * dell'ordine, mantenuto fino alla fine della transazione.
 *
 * Non salva fatture, righe, scritture contabili
 * o scadenzari e non consuma numeratori.
 *
 * Il piano restituito è utilizzabile per consultazione
 * e verifica: NON deve essere persistito successivamente
 * fuori dalla transazione che lo ha generato.
 */
@Service
public class SupplierInvoicePartialCoordinatorService {

    private final SupplierInvoicePartialPreparationService
            preparationService;

    private final SupplierInvoicePartialDraftAssembler
            assembler;

    public SupplierInvoicePartialCoordinatorService(
            SupplierInvoicePartialPreparationService preparationService,
            SupplierInvoicePartialDraftAssembler assembler) {

        this.preparationService = preparationService;
        this.assembler = assembler;
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public SupplierInvoicePartialDraftPlan plan(
            Long tenantId,
            Long goodsReceiptId) {

        // La preparazione verifica nell'ordine:
        // feature gate, isolamento JDBC effettivo,
        // identificativi, società, ricevimento,
        // lock ordine, duplicazioni e righe.
        var prepared = preparationService.prepare(
                tenantId,
                goodsReceiptId
        );

        // Il lock rimane nella stessa transazione
        // mentre il planner legge lo storico JPA.
        return assembler.assemble(prepared);
    }
}
