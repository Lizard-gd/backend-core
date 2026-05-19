--liquibase formatted sql
--changeset Lizard:BCORE-32-2
--comment: Создание таблицы leads с внешним ключом на companies

CREATE TABLE leads (
                       id UUID PRIMARY KEY NOT NULL,
                       first_name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       phone VARCHAR(50) NOT NULL,
                       company_id UUID,
                       status VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       version BIGINT NOT NULL DEFAULT 0,
                       CONSTRAINT fk_leads_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE INDEX idx_leads_email ON leads(email);
CREATE INDEX idx_leads_status ON leads(status);

--rollback DROP TABLE leads;