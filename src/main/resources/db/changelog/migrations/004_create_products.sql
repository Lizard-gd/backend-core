--liquibase formatted sql
--changeset Lizard:BCORE-32-4
--comment: Создание таблицы products для справочника товаров/услуг

CREATE TABLE products (
                          id UUID PRIMARY KEY NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          sku VARCHAR(100) UNIQUE NOT NULL,
                          price DECIMAL(19, 2) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_active ON products(active);

--rollback DROP TABLE products;