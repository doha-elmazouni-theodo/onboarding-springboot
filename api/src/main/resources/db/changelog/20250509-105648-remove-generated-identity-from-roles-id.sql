-- liquibase formatted sql

-- changeset iassiyadi:1746782300802-1
ALTER TABLE roles ALTER COLUMN id DROP IDENTITY IF EXISTS;

-- changeset iassiyadi:1746782300802-2
ALTER TABLE roles ALTER COLUMN id DROP DEFAULT;

-- changeset iassiyadi:1746782300802-3
ALTER TABLE roles ALTER COLUMN id SET NOT NULL;
