-- V005__create_reservation_table.sql
-- RESERVATION: booking of one room for one guest over a date range

CREATE SEQUENCE reservation_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE reservation
(
    id                 BIGINT         DEFAULT NEXT VALUE FOR reservation_seq PRIMARY KEY,
    reservation_number VARCHAR(20)    NOT NULL,
    guest_id           BIGINT         NOT NULL,
    room_id            BIGINT         NOT NULL,
    created_by_user_id BIGINT,
    arrival_date       DATE           NOT NULL,
    departure_date     DATE           NOT NULL,
    number_of_guests   INTEGER        NOT NULL,
    total_price        DECIMAL(10, 2) NOT NULL,
    source             VARCHAR(20)    NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    created_at         TIMESTAMP      NOT NULL,
    checked_in_at      TIMESTAMP,
    checked_out_at     TIMESTAMP,

    CONSTRAINT uk_reservation_number UNIQUE (reservation_number),
    CONSTRAINT fk_reservation_guest FOREIGN KEY (guest_id) REFERENCES guest (id),
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES room (id),
    CONSTRAINT fk_reservation_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES app_user (id),
    CONSTRAINT ck_reservation_number_of_guests CHECK (number_of_guests BETWEEN 1 AND 10),
    CONSTRAINT ck_reservation_total_price CHECK (total_price BETWEEN 0 AND 1000000),
    CONSTRAINT ck_reservation_source CHECK (source IN ('ONLINE', 'ON_SITE')),
    CONSTRAINT ck_reservation_status CHECK (status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')),
    -- Departure date must be after arrival date
    CONSTRAINT ck_reservation_dates CHECK (departure_date > arrival_date),
    -- Created-by user is required when the reservation was made on site
    CONSTRAINT ck_reservation_created_by CHECK (source <> 'ON_SITE' OR created_by_user_id IS NOT NULL),
    -- Checked-in time is required once the guest is checked in or checked out
    CONSTRAINT ck_reservation_checked_in_at CHECK (status NOT IN ('CHECKED_IN', 'CHECKED_OUT') OR checked_in_at IS NOT NULL),
    -- Checked-out time is required once the guest is checked out
    CONSTRAINT ck_reservation_checked_out_at CHECK (status <> 'CHECKED_OUT' OR checked_out_at IS NOT NULL)
);

CREATE INDEX ix_reservation_guest_id ON reservation (guest_id);
CREATE INDEX ix_reservation_room_id ON reservation (room_id);
CREATE INDEX ix_reservation_created_by_user_id ON reservation (created_by_user_id);
-- Supports availability searches by room and date range
CREATE INDEX ix_reservation_room_dates ON reservation (room_id, arrival_date, departure_date);
