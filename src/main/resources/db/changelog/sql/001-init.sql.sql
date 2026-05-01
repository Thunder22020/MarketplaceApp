-- liquibase formatted sql

-- changeset danila:1777653895118-1
CREATE TABLE users
(
    id            UUID         NOT NULL,
    username      VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

-- changeset danila:1777653895118-2
ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

