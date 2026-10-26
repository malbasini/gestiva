-- Gestiva V2
-- Prezzo di acquisto anagrafico per unita di misura base.
-- Il prezzo di vendita esistente rimane in item.base_price.
-- Nessun valore viene attribuito automaticamente agli articoli esistenti.

ALTER TABLE `item`
    ADD COLUMN `purchase_price` DECIMAL(15,2) NULL
    AFTER `base_price`;
