-- liquibase formatted sql

-- changeset danila:1778573784204-1
CREATE TABLE orders
(
    id           UUID                        NOT NULL,
    status       VARCHAR(255)                NOT NULL,
    customer_id  UUID                        NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    total_amount DECIMAL(19, 2)              NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

-- changeset danila:1778573784204-2
ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES users (id);

