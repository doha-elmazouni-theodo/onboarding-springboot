-- liquibase formatted sql

-- changeset medto:1735498081764-1
ALTER TABLE user_sessions ALTER COLUMN user_id SET NOT NULL;
