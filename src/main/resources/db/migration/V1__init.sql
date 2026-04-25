CREATE TABLE ai_conversation_log
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id         BIGINT      NOT NULL,
    created_at        datetime    NOT NULL,
    updated_at        datetime    NOT NULL,
    user_id           BIGINT      NOT NULL,
    prompt_text       LONGTEXT    NOT NULL,
    response_text     LONGTEXT NULL,
    scope_type        VARCHAR(30) NOT NULL,
    action_type       VARCHAR(30) NULL,
    confirmed_by_user BIT(1)      NOT NULL,
    CONSTRAINT pk_ai_conversation_log PRIMARY KEY (id)
);

CREATE TABLE app_user
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id     BIGINT       NOT NULL,
    created_at    datetime     NOT NULL,
    updated_at    datetime     NOT NULL,
    first_name    VARCHAR(120) NOT NULL,
    last_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    `admin`       BIT(1)       NOT NULL,
    locale_code   VARCHAR(10) NULL,
    CONSTRAINT pk_app_user PRIMARY KEY (id)
);

CREATE TABLE attachment
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id    BIGINT       NOT NULL,
    created_at   datetime     NOT NULL,
    updated_at   datetime     NOT NULL,
    entity_name  VARCHAR(80)  NOT NULL,
    entity_id    BIGINT       NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes   BIGINT       NOT NULL,
    CONSTRAINT pk_attachment PRIMARY KEY (id)
);

CREATE TABLE audit_log
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id      BIGINT       NOT NULL,
    created_at     datetime     NOT NULL,
    updated_at     datetime     NOT NULL,
    user_id        BIGINT NULL,
    entity_name    VARCHAR(100) NOT NULL,
    entity_id      BIGINT       NOT NULL,
    action_code    VARCHAR(30)  NOT NULL,
    old_value_json LONGTEXT NULL,
    new_value_json LONGTEXT NULL,
    CONSTRAINT pk_audit_log PRIMARY KEY (id)
);

CREATE TABLE company_profile
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id     BIGINT       NOT NULL,
    created_at    datetime     NOT NULL,
    updated_at    datetime     NOT NULL,
    legal_name    VARCHAR(180) NOT NULL,
    trade_name    VARCHAR(180) NULL,
    vat_number    VARCHAR(30) NULL,
    tax_code      VARCHAR(30) NULL,
    email         VARCHAR(180) NULL,
    phone         VARCHAR(50) NULL,
    website       VARCHAR(255) NULL,
    currency_code VARCHAR(3) NULL,
    locale_code   VARCHAR(10) NULL,
    CONSTRAINT pk_company_profile PRIMARY KEY (id)
);

CREATE TABLE contact_person
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id   BIGINT       NOT NULL,
    created_at  datetime     NOT NULL,
    updated_at  datetime     NOT NULL,
    customer_id BIGINT NULL,
    supplier_id BIGINT NULL,
    first_name  VARCHAR(120) NOT NULL,
    last_name   VARCHAR(120) NOT NULL,
    email       VARCHAR(180) NULL,
    phone       VARCHAR(50) NULL,
    role_title  VARCHAR(120) NULL,
    CONSTRAINT pk_contact_person PRIMARY KEY (id)
);

CREATE TABLE crm_activity
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id        BIGINT       NOT NULL,
    created_at       datetime     NOT NULL,
    updated_at       datetime     NOT NULL,
    lead_id          BIGINT NULL,
    customer_id      BIGINT NULL,
    opportunity_id   BIGINT NULL,
    type             VARCHAR(40)  NOT NULL,
    subject          VARCHAR(200) NOT NULL,
    notes            LONGTEXT NULL,
    assigned_user_id BIGINT NULL,
    due_at           datetime NULL,
    status           VARCHAR(30)  NOT NULL,
    CONSTRAINT pk_crm_activity PRIMARY KEY (id)
);

CREATE TABLE crm_note
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id      BIGINT   NOT NULL,
    created_at     datetime NOT NULL,
    updated_at     datetime NOT NULL,
    lead_id        BIGINT NULL,
    customer_id    BIGINT NULL,
    opportunity_id BIGINT NULL,
    content        LONGTEXT NOT NULL,
    author_user_id BIGINT   NOT NULL,
    CONSTRAINT pk_crm_note PRIMARY KEY (id)
);

CREATE TABLE customer
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id  BIGINT       NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime     NOT NULL,
    name       VARCHAR(180) NOT NULL,
    vat_number VARCHAR(30) NULL,
    tax_code   VARCHAR(30) NULL,
    email      VARCHAR(180) NULL,
    phone      VARCHAR(50) NULL,
    type       VARCHAR(30) NULL,
    status     VARCHAR(30) NULL,
    CONSTRAINT pk_customer PRIMARY KEY (id)
);

CREATE TABLE document_sequence
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id     BIGINT      NOT NULL,
    created_at    datetime    NOT NULL,
    updated_at    datetime    NOT NULL,
    sequence_code VARCHAR(40) NOT NULL,
    year_value    INT         NOT NULL,
    next_number   INT         NOT NULL,
    prefix        VARCHAR(30) NULL,
    CONSTRAINT pk_document_sequence PRIMARY KEY (id)
);

CREATE TABLE document_template
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id     BIGINT       NOT NULL,
    created_at    datetime     NOT NULL,
    updated_at    datetime     NOT NULL,
    code          VARCHAR(80)  NOT NULL,
    name          VARCHAR(180) NOT NULL,
    document_type VARCHAR(30)  NOT NULL,
    template_html LONGTEXT     NOT NULL,
    active        BIT(1)       NOT NULL,
    CONSTRAINT pk_document_template PRIMARY KEY (id)
);

CREATE TABLE `lead`
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id           BIGINT       NOT NULL,
    created_at          datetime     NOT NULL,
    updated_at          datetime     NOT NULL,
    company_name        VARCHAR(180) NOT NULL,
    contact_name        VARCHAR(120) NULL,
    email               VARCHAR(180) NULL,
    phone               VARCHAR(50) NULL,
    source              VARCHAR(40)  NOT NULL,
    status              VARCHAR(40)  NOT NULL,
    assigned_to         VARCHAR(80) NULL,
    estimated_value     DECIMAL(15, 2) NULL,
    expected_close_date date NULL,
    CONSTRAINT pk_lead PRIMARY KEY (id)
);

CREATE TABLE opportunity
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id           BIGINT       NOT NULL,
    created_at          datetime     NOT NULL,
    updated_at          datetime     NOT NULL,
    lead_id             BIGINT NULL,
    customer_id         BIGINT NULL,
    title               VARCHAR(180) NOT NULL,
    stage_code          VARCHAR(40)  NOT NULL,
    amount              DECIMAL(15, 2) NULL,
    probability         INT NULL,
    expected_close_date date NULL,
    status              VARCHAR(30) NULL,
    CONSTRAINT pk_opportunity PRIMARY KEY (id)
);

CREATE TABLE payment_reminder
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id           BIGINT      NOT NULL,
    created_at          datetime    NOT NULL,
    updated_at          datetime    NOT NULL,
    payment_schedule_id BIGINT      NOT NULL,
    channel             VARCHAR(30) NOT NULL,
    sent_at             datetime NULL,
    status              VARCHAR(30) NOT NULL,
    CONSTRAINT pk_payment_reminder PRIMARY KEY (id)
);

CREATE TABLE payment_schedule
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id     BIGINT         NOT NULL,
    created_at    datetime       NOT NULL,
    updated_at    datetime       NOT NULL,
    customer_id   BIGINT NULL,
    supplier_id   BIGINT NULL,
    direction     VARCHAR(20)    NOT NULL,
    document_type VARCHAR(80)    NOT NULL,
    document_id   BIGINT         NOT NULL,
    due_date      date           NOT NULL,
    amount        DECIMAL(15, 2) NOT NULL,
    status        VARCHAR(30)    NOT NULL,
    CONSTRAINT pk_payment_schedule PRIMARY KEY (id)
);

CREATE TABLE permission
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime     NOT NULL,
    updated_at    datetime     NOT NULL,
    code          VARCHAR(120) NOT NULL,
    module_code   VARCHAR(120) NOT NULL,
    `description` VARCHAR(200) NOT NULL,
    CONSTRAINT pk_permission PRIMARY KEY (id)
);

CREATE TABLE quote
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id       BIGINT         NOT NULL,
    created_at      datetime       NOT NULL,
    updated_at      datetime       NOT NULL,
    customer_id     BIGINT         NOT NULL,
    quote_number    VARCHAR(50)    NOT NULL,
    quote_date      date           NOT NULL,
    valid_until     date           NOT NULL,
    status          VARCHAR(30)    NOT NULL,
    currency_code   VARCHAR(3)     NOT NULL,
    subtotal_amount DECIMAL(15, 2) NOT NULL,
    tax_amount      DECIMAL(15, 2) NOT NULL,
    total_amount    DECIMAL(15, 2) NOT NULL,
    notes           LONGTEXT NULL,
    CONSTRAINT pk_quote PRIMARY KEY (id)
);

CREATE TABLE quote_line
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id     BIGINT         NOT NULL,
    created_at    datetime       NOT NULL,
    updated_at    datetime       NOT NULL,
    quote_id      BIGINT         NOT NULL,
    line_no       INT            NOT NULL,
    `description` VARCHAR(255)   NOT NULL,
    quantity      DECIMAL(15, 3) NOT NULL,
    unit_price    DECIMAL(15, 2) NOT NULL,
    discount_pct  DECIMAL(5, 2) NULL,
    tax_pct       DECIMAL(5, 2) NULL,
    line_total    DECIMAL(15, 2) NOT NULL,
    CONSTRAINT pk_quote_line PRIMARY KEY (id)
);

CREATE TABLE `role`
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id   BIGINT       NOT NULL,
    created_at  datetime     NOT NULL,
    updated_at  datetime     NOT NULL,
    code        VARCHAR(80)  NOT NULL,
    name        VARCHAR(120) NOT NULL,
    system_role BIT(1)       NOT NULL,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE role_permission
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id     BIGINT   NOT NULL,
    created_at    datetime NOT NULL,
    updated_at    datetime NOT NULL,
    role_id       BIGINT   NOT NULL,
    permission_id BIGINT   NOT NULL,
    CONSTRAINT pk_role_permission PRIMARY KEY (id)
);

CREATE TABLE sales_order
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id     BIGINT         NOT NULL,
    created_at    datetime       NOT NULL,
    updated_at    datetime       NOT NULL,
    customer_id   BIGINT         NOT NULL,
    quote_id      BIGINT NULL,
    order_number  VARCHAR(50)    NOT NULL,
    order_date    date           NOT NULL,
    status        VARCHAR(30)    NOT NULL,
    currency_code VARCHAR(3)     NOT NULL,
    total_amount  DECIMAL(15, 2) NOT NULL,
    CONSTRAINT pk_sales_order PRIMARY KEY (id)
);

CREATE TABLE sales_order_line
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id      BIGINT         NOT NULL,
    created_at     datetime       NOT NULL,
    updated_at     datetime       NOT NULL,
    sales_order_id BIGINT         NOT NULL,
    line_no        INT            NOT NULL,
    `description`  VARCHAR(255)   NOT NULL,
    quantity       DECIMAL(15, 3) NOT NULL,
    unit_price     DECIMAL(15, 2) NOT NULL,
    line_total     DECIMAL(15, 2) NOT NULL,
    CONSTRAINT pk_sales_order_line PRIMARY KEY (id)
);

CREATE TABLE supplier
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id  BIGINT       NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime     NOT NULL,
    name       VARCHAR(180) NOT NULL,
    vat_number VARCHAR(30) NULL,
    email      VARCHAR(180) NULL,
    phone      VARCHAR(50) NULL,
    status     VARCHAR(30) NULL,
    CONSTRAINT pk_supplier PRIMARY KEY (id)
);

CREATE TABLE tenant
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime     NOT NULL,
    updated_at       datetime     NOT NULL,
    name             VARCHAR(120) NOT NULL,
    slug             VARCHAR(120) NOT NULL,
    email            VARCHAR(180) NOT NULL,
    status           VARCHAR(30)  NOT NULL,
    default_locale   VARCHAR(10)  NOT NULL,
    default_currency VARCHAR(3)   NOT NULL,
    CONSTRAINT pk_tenant PRIMARY KEY (id)
);

CREATE TABLE tenant_module
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime    NOT NULL,
    updated_at  datetime    NOT NULL,
    tenant_id   BIGINT      NOT NULL,
    module_code VARCHAR(50) NOT NULL,
    enabled     BIT(1)      NOT NULL,
    plan_code   VARCHAR(50) NULL,
    CONSTRAINT pk_tenant_module PRIMARY KEY (id)
);

CREATE TABLE user_role
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    tenant_id  BIGINT   NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    user_id    BIGINT   NOT NULL,
    role_id    BIGINT   NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (id)
);

ALTER TABLE role_permission
    ADD CONSTRAINT uc_045e33e5f40df297190f273a7 UNIQUE (tenant_id, role_id, permission_id);

ALTER TABLE tenant_module
    ADD CONSTRAINT uc_0db55ed83d36e063f25aa8586 UNIQUE (tenant_id, module_code);

ALTER TABLE user_role
    ADD CONSTRAINT uc_230087ae510340f93a7dc04b4 UNIQUE (tenant_id, user_id, role_id);

ALTER TABLE `role`
    ADD CONSTRAINT uc_3e55e8fc59d947e28d1ad8aa5 UNIQUE (tenant_id, code);

ALTER TABLE app_user
    ADD CONSTRAINT uc_5bc5805bdb9f459160ff9129f UNIQUE (tenant_id, email);

ALTER TABLE document_sequence
    ADD CONSTRAINT uc_755af292e5dcfc1e3a6a0b3a4 UNIQUE (tenant_id, sequence_code, year_value);

ALTER TABLE sales_order
    ADD CONSTRAINT uc_a990dfe94f127c1be31f33265 UNIQUE (tenant_id, order_number);

ALTER TABLE permission
    ADD CONSTRAINT uc_c533d6f945e3cc91db5f05576 UNIQUE (code);

ALTER TABLE quote
    ADD CONSTRAINT uc_e78bafd14f0ef376c44e492d8 UNIQUE (tenant_id, quote_number);

ALTER TABLE tenant
    ADD CONSTRAINT uc_tenant_email UNIQUE (email);

ALTER TABLE tenant
    ADD CONSTRAINT uc_tenant_slug UNIQUE (slug);

CREATE INDEX idx_audit_tenant_created ON audit_log (tenant_id, created_at);

CREATE INDEX idx_audit_tenant_entity ON audit_log (tenant_id, entity_name, entity_id);

CREATE INDEX idx_customer_tenant_name ON customer (tenant_id, name);