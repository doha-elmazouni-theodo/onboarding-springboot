-- liquibase formatted sql

-- changeset adil:1634910343599-1
ALTER TABLE users ALTER COLUMN password SET NOT NULL;

-- changeset adil:1634910343599-2
ALTER TABLE users ALTER COLUMN username SET NOT NULL;
