-- Gestiva V2
-- Impedisce più ricezioni merci per lo stesso ordine
-- all'interno della medesima società e tenant.
--
-- PREREQUISITO OPERATIVO:
-- Verificare l'assenza di duplicati prima di eseguire Flyway.
--
-- Questa migrazione NON viene eseguita automaticamente
-- dalla sua semplice creazione.

ALTER TABLE goods_receipt
    ADD CONSTRAINT uk_goods_receipt_tenant_company_purchase_order
    UNIQUE (tenant_id, company_id, purchase_order_id);
