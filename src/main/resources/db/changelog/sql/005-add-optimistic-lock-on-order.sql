-- liquibase formatted sql

-- changeset danila:1778855062818-1
ALTER TABLE orders
    ADD version BIGINT;

-- changeset danila:1778855062818-2
ALTER TABLE orders
    ALTER COLUMN version SET NOT NULL;

