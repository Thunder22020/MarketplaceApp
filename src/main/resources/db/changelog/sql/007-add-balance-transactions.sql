-- liquibase formatted sql

-- changeset danila:1779603591649-1
CREATE TABLE balance_transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    order_id UUID,
    payment_id UUID,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- changeset danila:1779603591649-2
ALTER TABLE balance_transactions
    ADD CONSTRAINT FK_BALANCE_TRANSACTIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

-- changeset danila:1779603591649-3
ALTER TABLE balance_transactions
    ADD CONSTRAINT FK_BALANCE_TRANSACTIONS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

-- changeset danila:1779603591649-4
ALTER TABLE balance_transactions
    ADD CONSTRAINT FK_BALANCE_TRANSACTIONS_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payments (id);

-- changeset danila:1779603591649-5
ALTER TABLE balance_transactions
    ADD CONSTRAINT UC_BALANCE_TRANSACTIONS_PAYMENT_USER_TYPE UNIQUE (payment_id, user_id, type);
