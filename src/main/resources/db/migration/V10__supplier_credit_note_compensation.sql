-- GESTIVA V2 - V10
-- Compensazioni note di credito fornitore.
--
-- Non modifica i dati delle note di credito gia registrate.
-- Non genera pagamenti bancari ne scritture contabili.
-- La registrazione delle compensazioni avverra tramite
-- un servizio applicativo transazionale dedicato.

-- 1. Distinzione tra pagato e compensato nello scadenzario.
ALTER TABLE payment_due
    ADD COLUMN compensated_amount DECIMAL(15,2)
        NOT NULL DEFAULT 0.00
        AFTER paid_amount;

-- 2. Chiave composita necessaria per collegamenti
--    sicuri nel perimetro tenant/societa.
ALTER TABLE payment_due
    ADD UNIQUE KEY uk_payment_due_tenant_company_id
        (tenant_id, company_id, id);

-- 3. Vincolo sugli importi compensati.
ALTER TABLE payment_due
    ADD CONSTRAINT chk_payment_due_compensated_nonnegative
        CHECK (compensated_amount >= 0);

-- 4. Registro autonomo delle compensazioni.
CREATE TABLE supplier_credit_note_compensation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,

    credit_note_financial_position_id BIGINT NOT NULL,
    payment_due_id BIGINT NOT NULL,

    operation_key VARCHAR(100) NOT NULL,
    compensation_date DATE NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    notes VARCHAR(1000) DEFAULT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_scn_compensation_operation
        (tenant_id, company_id, operation_key),

    KEY idx_scn_compensation_position
        (tenant_id, company_id,
         credit_note_financial_position_id),

    KEY idx_scn_compensation_due
        (tenant_id, company_id, payment_due_id),

    KEY idx_scn_compensation_date
        (tenant_id, company_id, compensation_date),

    CONSTRAINT fk_scn_compensation_position
        FOREIGN KEY (
            tenant_id,
            company_id,
            credit_note_financial_position_id
        )
        REFERENCES credit_note_financial_position (
            tenant_id,
            company_id,
            id
        ),

    CONSTRAINT fk_scn_compensation_due
        FOREIGN KEY (
            tenant_id,
            company_id,
            payment_due_id
        )
        REFERENCES payment_due (
            tenant_id,
            company_id,
            id
        ),

    CONSTRAINT chk_scn_compensation_amount
        CHECK (amount > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
