-- GESTIVA V2 - V11
-- Compensazioni note di credito cliente.
--
-- Presuppone la struttura V9/V10.
-- Non modifica fatture, note, scadenze o movimenti contabili.
-- Le compensazioni saranno gestite da un servizio transazionale.

CREATE TABLE customer_credit_note_compensation (
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

    UNIQUE KEY uk_ccn_compensation_operation
        (tenant_id, company_id, operation_key),

    KEY idx_ccn_compensation_position
        (tenant_id, company_id,
         credit_note_financial_position_id),

    KEY idx_ccn_compensation_due
        (tenant_id, company_id, payment_due_id),

    KEY idx_ccn_compensation_date
        (tenant_id, company_id, compensation_date),

    CONSTRAINT fk_ccn_compensation_position
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

    CONSTRAINT fk_ccn_compensation_due
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

    CONSTRAINT chk_ccn_compensation_amount
        CHECK (amount > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
