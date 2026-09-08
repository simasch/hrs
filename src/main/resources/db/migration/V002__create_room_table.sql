-- V002__create_room_table.sql
-- ROOM: one of the physical rooms together with its housekeeping state

CREATE SEQUENCE room_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE room
(
    id                  BIGINT       DEFAULT NEXT VALUE FOR room_seq PRIMARY KEY,
    room_number         INTEGER      NOT NULL,
    room_type_id        BIGINT       NOT NULL,
    cleaning_status     VARCHAR(20)  NOT NULL,
    out_of_service_note VARCHAR(500),

    CONSTRAINT uk_room_room_number UNIQUE (room_number),
    CONSTRAINT fk_room_room_type FOREIGN KEY (room_type_id) REFERENCES room_type (id),
    CONSTRAINT ck_room_cleaning_status CHECK (cleaning_status IN ('CLEAN', 'DIRTY', 'OUT_OF_SERVICE')),
    -- The out-of-service note must be present when the room is out of service
    CONSTRAINT ck_room_out_of_service_note CHECK (cleaning_status <> 'OUT_OF_SERVICE' OR out_of_service_note IS NOT NULL)
);

CREATE INDEX ix_room_room_type_id ON room (room_type_id);
