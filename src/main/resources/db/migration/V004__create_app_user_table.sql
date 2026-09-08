-- V004__create_app_user_table.sql
-- USER: staff account that can log in to the management functions.
-- Named app_user because USER is a reserved word in H2 and PostgreSQL.

CREATE SEQUENCE app_user_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE app_user
(
    id            BIGINT       DEFAULT NEXT VALUE FOR app_user_seq PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_app_user_username UNIQUE (username),
    CONSTRAINT ck_app_user_role CHECK (role IN ('RECEPTIONIST', 'MANAGER', 'HOUSEKEEPING'))
);
