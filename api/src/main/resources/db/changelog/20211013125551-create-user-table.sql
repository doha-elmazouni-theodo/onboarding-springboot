-- liquibase formatted sql

-- changeset adil:1634126168524-1
CREATE TABLE users
(
    id         UUID PRIMARY KEY            NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(255)
);

-- changeset adil:1634126168524-2
ALTER TABLE users
    ADD CONSTRAINT UC_USERSNAME_COL UNIQUE (name);
