-- Gestiva ERP V2
-- Protezione anti-duplicazione fatture fornitori.
--
-- Una ricezione merci può generare una sola fattura
-- all'interno dello stesso tenant e della stessa azienda.

ALTER TABLE supplier_invoice
ADD CONSTRAINT uk_supplier_invoice_company_receipt
UNIQUE (tenant_id, company_id, goods_receipt_id);
