-- liquibase formatted sql

-- changeset adil:1634910123885-1
ALTER TABLE users ADD COLUMN enabled BOOLEAN;

-- changeset adil:1634910123885-2
ALTER TABLE users ADD COLUMN password VARCHAR(255);

-- changeset adil:1634910123885-3
ALTER TABLE users ADD COLUMN username VARCHAR(255);

-- changeset adil:1634910123885-4
ALTER TABLE users ADD CONSTRAINT UC_USERSUSERNAME_COL UNIQUE (username);

-- changeset adil:1634910123885-5
ALTER TABLE users DROP CONSTRAINT uc_usersname_col;
