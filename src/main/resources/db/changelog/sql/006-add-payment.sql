-- liquibase formatted sql

-- changeset danila:1779308879558-1
CREATE TABLE payments(
    id UUID NOT NULL,
    order_id UUID NOT NULL,
    external_id VARCHAR(256),
    confirmation_url VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

-- changeset danila:1779308879558-2
ALTER TABLE payments
    ADD CONSTRAINT FK_PAYMENTS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

