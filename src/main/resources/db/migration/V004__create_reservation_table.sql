CREATE SEQUENCE reservation_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE reservation
(
    id                BIGINT         DEFAULT nextval('reservation_seq') PRIMARY KEY,
    confirmation_code VARCHAR(20)    NOT NULL UNIQUE,
    guest_id          BIGINT         NOT NULL,
    room_id           BIGINT         NOT NULL,
    check_in_date     DATE           NOT NULL,
    check_out_date    DATE           NOT NULL,
    number_of_guests  INTEGER        NOT NULL CHECK (number_of_guests BETWEEN 1 AND 10),
    status            VARCHAR(20)    NOT NULL CHECK (status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')),
    total_price       DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    created_at        TIMESTAMP      NOT NULL,
    cancelled_at      TIMESTAMP,
    CONSTRAINT fk_reservation_guest FOREIGN KEY (guest_id) REFERENCES guest (id),
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES room (id),
    CONSTRAINT chk_reservation_dates CHECK (check_out_date > check_in_date)
);

CREATE INDEX idx_reservation_room_dates ON reservation (room_id, check_in_date, check_out_date);
CREATE INDEX idx_reservation_guest ON reservation (guest_id);
