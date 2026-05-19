--liquibase formatted sql
--changeset Lizard:BCORE-32-1
--comment: Создание таблицы companies

CREATE TABLE companies (
                           id UUID PRIMARY KEY NOT NULL,
                           name VARCHAR(255) NOT NULL,
                           industry VARCHAR(100)
);

--rollback DROP TABLE companies;