-- liquibase formatted sql

-- changeset user:1766732739226-1 splitStatements:false
ALTER TABLE "users" ALTER COLUMN "created_at" TYPE TIMESTAMP WITHOUT TIME ZONE USING ("created_at"::TIMESTAMP WITHOUT TIME ZONE);

-- changeset user:1766732739226-2 splitStatements:false
ALTER TABLE "user_sessions" ALTER COLUMN "expiration_date" TYPE TIMESTAMP WITHOUT TIME ZONE USING ("expiration_date"::TIMESTAMP WITHOUT TIME ZONE);

-- changeset user:1766732739226-3 splitStatements:false
ALTER TABLE "users" ALTER COLUMN  "name" SET NOT NULL;

-- changeset user:1766732739226-4 splitStatements:false
ALTER TABLE "user_role" ALTER COLUMN "role_id" TYPE INTEGER USING ("role_id"::INTEGER);

-- changeset user:1766732739226-5 splitStatements:false
ALTER TABLE "roles" ALTER COLUMN  "role_name" SET NOT NULL;

