-- Gestiva V2 - Note di credito clienti e fornitori.
-- Migrazione strutturale. Nessuna registrazione contabile automatica.
--
-- Gli importi sono memorizzati positivi.
-- Il segno contabile dipende dal tipo documento.
-- Le rettifiche cumulative e l'isolamento multi-company
-- sono validati dai servizi applicativi.

CREATE TABLE credit_note (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    credit_note_type VARCHAR(20) NOT NULL,
    credit_note_number VARCHAR(50) NOT NULL,
    credit_note_date DATE NOT NULL,
    original_invoice_id BIGINT NOT NULL,
    party_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    notes VARCHAR(1000) DEFAULT NULL,
    subtotal_amount DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_credit_note_tenant_company_id
        (tenant_id, company_id, id),

    UNIQUE KEY uk_credit_note_company_type_number
        (tenant_id, company_id, credit_note_type, credit_note_number),

    KEY idx_credit_note_original_invoice
        (tenant_id, company_id, credit_note_type, original_invoice_id),

    KEY idx_credit_note_company_date
        (tenant_id, company_id, credit_note_date),

    KEY idx_credit_note_company_status
        (tenant_id, company_id, status),

    KEY idx_credit_note_company_party
        (tenant_id, company_id, credit_note_type, party_id),

    CONSTRAINT chk_credit_note_type
        CHECK (credit_note_type IN ('SUPPLIER', 'CUSTOMER')),

    CONSTRAINT chk_credit_note_amounts
        CHECK (
            subtotal_amount >= 0
            AND tax_amount >= 0
            AND total_amount >= 0
            AND total_amount = subtotal_amount + tax_amount
        )

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE credit_note_line (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    credit_note_id BIGINT NOT NULL,
    original_invoice_line_id BIGINT DEFAULT NULL,
    line_no INT NOT NULL,
    item_id BIGINT DEFAULT NULL,
    description VARCHAR(500) NOT NULL,
    quantity DECIMAL(15,3) DEFAULT NULL,
    unit_of_measure VARCHAR(20) DEFAULT NULL,
    uom_id BIGINT DEFAULT NULL,
    uom_code VARCHAR(20) DEFAULT NULL,
    base_uom_code VARCHAR(20) DEFAULT NULL,
    conversion_factor DECIMAL(19,6) DEFAULT NULL,
    unit_price DECIMAL(15,2) DEFAULT NULL,
    discount_pct DECIMAL(7,2) DEFAULT NULL,
    tax_pct DECIMAL(7,2) NOT NULL,
    line_subtotal DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) NOT NULL,
    line_total DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_credit_note_line_number
        (tenant_id, company_id, credit_note_id, line_no),

    KEY idx_credit_note_line_header
        (tenant_id, company_id, credit_note_id),

    KEY idx_credit_note_line_original
        (tenant_id, company_id, original_invoice_line_id),

    CONSTRAINT chk_credit_note_line_amounts
        CHECK (
            (quantity IS NULL OR quantity > 0)
            AND tax_pct >= 0
            AND line_subtotal >= 0
            AND tax_amount >= 0
            AND line_total >= 0
            AND line_total = line_subtotal + tax_amount
        ),

    CONSTRAINT fk_credit_note_line_header
        FOREIGN KEY (tenant_id, company_id, credit_note_id)
        REFERENCES credit_note(tenant_id, company_id, id)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- Una sola scrittura contabile per ogni nota di credito
-- registrata, separatamente per tenant e società.
-- I riferimenti NULL non partecipano alla UNIQUE MySQL.

ALTER TABLE journal_entry
    ADD COLUMN supplier_credit_note_unique_ref BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN reference_type = 'SUPPLIER_CREDIT_NOTE'
                THEN reference_id
                ELSE NULL
            END
        ) STORED,
    ADD COLUMN customer_credit_note_unique_ref BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN reference_type = 'CUSTOMER_CREDIT_NOTE'
                THEN reference_id
                ELSE NULL
            END
        ) STORED,
    ADD UNIQUE KEY uk_journal_entry_supplier_credit_note
        (tenant_id, company_id, supplier_credit_note_unique_ref),
    ADD UNIQUE KEY uk_journal_entry_customer_credit_note
        (tenant_id, company_id, customer_credit_note_unique_ref);


-- =====================================================
-- GESTIVA V2 - POSIZIONE FINANZIARIA NOTE DI CREDITO
-- =====================================================
-- La posizione è creata solo per una nota registrata.
-- Il documento originale e i suoi pagamenti non cambiano.
-- Le compensazioni e i rimborsi saranno operazioni
-- distinte, verificabili e tracciabili.
--
-- La tabella supporta entrambi i tipi di nota;
-- nella prima fase viene attivata solo per SUPPLIER.

CREATE TABLE credit_note_financial_position (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    credit_note_id BIGINT NOT NULL,
    position_type VARCHAR(20) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    original_amount DECIMAL(15,2) NOT NULL,
    compensated_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    refunded_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    available_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_credit_note_position_company_note
        (tenant_id, company_id, credit_note_id),

    UNIQUE KEY uk_credit_note_position_company_id
        (tenant_id, company_id, id),

    KEY idx_credit_note_position_company_status
        (tenant_id, company_id, position_type, status),

    CONSTRAINT fk_credit_note_position_note
        FOREIGN KEY (tenant_id, company_id, credit_note_id)
        REFERENCES credit_note(tenant_id, company_id, id),

    CONSTRAINT chk_credit_note_position_type
        CHECK (position_type IN ('SUPPLIER', 'CUSTOMER')),

    CONSTRAINT chk_credit_note_position_status
        CHECK (status IN (
            'OPEN',
            'PARTIALLY_SETTLED',
            'SETTLED'
        )),

    CONSTRAINT chk_credit_note_position_amounts
        CHECK (
            original_amount > 0
            AND compensated_amount >= 0
            AND refunded_amount >= 0
            AND available_amount >= 0
            AND compensated_amount + refunded_amount
                <= original_amount
            AND available_amount
                = original_amount
                  - compensated_amount
                  - refunded_amount
        ),

    CONSTRAINT chk_credit_note_position_status_amount
        CHECK (
            (status = 'OPEN'
                AND available_amount = original_amount)
            OR
            (status = 'PARTIALLY_SETTLED'
                AND available_amount > 0
                AND available_amount < original_amount)
            OR
            (status = 'SETTLED'
                AND available_amount = 0)
        )

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


-- =====================================================
-- GESTIVA_CREDIT_NOTE_SEQUENCE_BOOTSTRAP_V9
-- =====================================================
-- Inizializzazione per società già presenti.
-- Le sequenze esistenti non vengono modificate.
-- Le nuove società sono gestite da CompanySettingsService.

INSERT INTO document_sequence (
    tenant_id,
    company_id,
    document_type,
    prefix,
    next_number,
    padding_size,
    created_at,
    updated_at
)
SELECT
    c.tenant_id,
    c.id,
    t.document_type,
    t.prefix,
    1,
    5,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM company c
CROSS JOIN (
    SELECT
        'SUPPLIER_CREDIT_NOTE' AS document_type,
        'SCN-' AS prefix
    UNION ALL
    SELECT
        'CUSTOMER_CREDIT_NOTE' AS document_type,
        'CCN-' AS prefix
) t
WHERE NOT EXISTS (
    SELECT 1
    FROM document_sequence ds
    WHERE ds.tenant_id = c.tenant_id
      AND ds.company_id = c.id
      AND ds.document_type = t.document_type
);
