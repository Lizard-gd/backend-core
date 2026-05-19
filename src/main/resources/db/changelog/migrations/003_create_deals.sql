--liquibase formatted sql
--changeset Lizard:BCORE-32-3
--comment: Создание таблицы deals (без внешних ключей, пока только как хранилище)

CREATE TABLE deals (
                       id VARCHAR(36) PRIMARY KEY NOT NULL,
                       lead_id VARCHAR(36) NOT NULL,
                       amount DECIMAL(19,2) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_deals_lead_id ON deals(lead_id);
CREATE INDEX idx_deals_status ON deals(status);

--rollback DROP TABLE deals;