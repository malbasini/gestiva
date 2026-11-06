-- Gestiva V2
-- Consente ricevimenti merci multipli per lo stesso
-- ordine fornitore all'interno della medesima società.
--
-- V5 aveva introdotto il vincolo UNIQUE su:
-- (tenant_id, company_id, purchase_order_id).
--
-- Il vincolo viene rimosso perché un ordine può
-- essere evaso tramite più ricevimenti parziali.
--
-- Rimangono invariati:
-- - l'univocità del numero di ricevimento;
-- - il vincolo anti-duplicazione fattura per ricevimento;
-- - tutti gli altri vincoli e indici.

ALTER TABLE goods_receipt
    DROP INDEX uk_goods_receipt_tenant_company_purchase_order;
