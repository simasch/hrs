-- V003__create_guest_table.sql
-- GUEST: person who stays at the hotel

CREATE SEQUENCE guest_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE guest
(
    id          BIGINT       DEFAULT NEXT VALUE FOR guest_seq PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(30),
    street      VARCHAR(200),
    postal_code VARCHAR(20),
    city        VARCHAR(100),
    country     VARCHAR(100)
);

CREATE INDEX ix_guest_email ON guest (email);
