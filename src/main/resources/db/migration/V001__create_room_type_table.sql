-- V001__create_room_type_table.sql
-- ROOM_TYPE: category of rooms sharing capacity and nightly price

CREATE SEQUENCE room_type_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE room_type
(
    id          BIGINT         DEFAULT NEXT VALUE FOR room_type_seq PRIMARY KEY,
    name        VARCHAR(50)    NOT NULL,
    description VARCHAR(500),
    capacity    INTEGER        NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,

    CONSTRAINT uk_room_type_name UNIQUE (name),
    CONSTRAINT ck_room_type_capacity CHECK (capacity BETWEEN 1 AND 10),
    CONSTRAINT ck_room_type_price CHECK (price BETWEEN 0 AND 10000)
);
