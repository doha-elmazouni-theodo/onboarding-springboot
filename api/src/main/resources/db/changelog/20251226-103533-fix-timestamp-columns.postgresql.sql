-- liquibase formatted sql

-- changeset user:1766745360185-1 splitStatements:false
ALTER TABLE public.users
    ALTER COLUMN created_at TYPE timestamp(6) with time zone ;

-- changeset user:1766745360185-2 splitStatements:false
ALTER TABLE public.user_sessions
    ALTER COLUMN expiration_date TYPE timestamp(6) with time zone ;
