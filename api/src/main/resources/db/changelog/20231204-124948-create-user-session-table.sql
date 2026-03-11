-- liquibase formatted sql

-- changeset khemisse:1701694206814-1
CREATE TABLE user_sessions
(
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE                   NOT NULL,
    refresh_token   VARCHAR(255)                                     NOT NULL,
    user_id         UUID
);

-- changeset khemisse:1701694206814-2
ALTER TABLE user_sessions
    ADD CONSTRAINT UK5ou0u1pqftlypbt1wcsbl5hgh UNIQUE (refresh_token);

-- changeset khemisse:1701694206814-3
ALTER TABLE user_sessions
    ADD CONSTRAINT FK8klxsgb8dcjjklmqebqp1twd5
        FOREIGN KEY (user_id) REFERENCES users (id);
