-- liquibase formatted sql

-- changeset adil:1635776416409-1
CREATE TABLE roles
(
    id        BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
    role_name VARCHAR(255)
);

-- changeset adil:1635776416409-2
CREATE TABLE user_role
(
    user_id UUID   NOT NULL,
    role_id BIGINT NOT NULL
);

-- changeset adil:1635776416409-3
ALTER TABLE user_role
    ADD CONSTRAINT FKj345gk1bovqvfame88rcx7yyx
        FOREIGN KEY (user_id) REFERENCES users (id);

-- changeset adil:1635776416409-4
ALTER TABLE user_role
    ADD CONSTRAINT FKt7e7djp752sqn6w22i6ocqy6q
        FOREIGN KEY (role_id) REFERENCES roles (id);
