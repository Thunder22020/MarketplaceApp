-- liquibase formatted sql

-- changeset danila:1777665365280-1
CREATE TABLE products
(
    id          UUID                        NOT NULL,
    title       VARCHAR(255)                NOT NULL,
    description TEXT,
    seller_id   UUID                        NOT NULL,
    status      VARCHAR(255)                NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    price       DECIMAL(19, 2)              NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

-- changeset danila:1777665365280-2
ALTER TABLE products
    ADD CONSTRAINT FK_PRODUCTS_ON_SELLER FOREIGN KEY (seller_id) REFERENCES users (id);

