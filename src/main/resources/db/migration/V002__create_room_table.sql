CREATE SEQUENCE room_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE room
(
    id           BIGINT      DEFAULT nextval('room_seq') PRIMARY KEY,
    number       VARCHAR(10) NOT NULL UNIQUE,
    floor        INTEGER     NOT NULL CHECK (floor >= 0),
    room_type_id BIGINT      NOT NULL,
    status       VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'CLEANING', 'MAINTENANCE')),
    notes        VARCHAR(500),
    CONSTRAINT fk_room_room_type FOREIGN KEY (room_type_id) REFERENCES room_type (id)
);
