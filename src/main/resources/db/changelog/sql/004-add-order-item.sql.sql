-- liquibase formatted sql

-- changeset danila:1778573856181-1
CREATE TABLE order_items
(
    order_id               UUID           NOT NULL,
    product_id             UUID           NOT NULL,
    quantity               INTEGER        NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (order_id, product_id)
);

-- changeset danila:1778573856181-2
ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

-- changeset danila:1778573856181-3
ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

