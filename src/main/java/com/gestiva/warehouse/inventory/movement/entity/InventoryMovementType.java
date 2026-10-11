package com.gestiva.warehouse.inventory.movement.entity;

/**
 * Tipologie operative delle movimentazioni di magazzino.
 *
 * Il tipo descrive la causale del movimento.
 * Il segno della quantità determina invece
 * l'effetto sulla giacenza.
 */
public enum InventoryMovementType {

    /**
     * Carico da acquisto, produzione o altro ingresso.
     */
    RECEIPT,

    /**
     * Scarico per vendita, consumo o altra uscita.
     */
    ISSUE,

    /**
     * Trasferimento tra magazzini o ubicazioni.
     * Produce due registrazioni collegate:
     * una negativa e una positiva.
     */
    TRANSFER,

    /**
     * Rettifica inventariale.
     * Può avere quantità positiva o negativa.
     */
    ADJUSTMENT,

    /**
     * Rientro merce da cliente.
     */
    RETURN_IN,

    /**
     * Restituzione merce al fornitore.
     */
    RETURN_OUT
}