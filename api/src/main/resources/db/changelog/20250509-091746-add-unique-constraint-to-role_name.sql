-- liquibase formatted sql

-- changeset iassiyadi:1746782300801-1
ALTER TABLE roles ADD CONSTRAINT UC_ROLESROLE_NAME_COL UNIQUE (role_name);

