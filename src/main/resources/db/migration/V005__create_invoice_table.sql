CREATE SEQUENCE invoice_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE invoice
(
    id             BIGINT         DEFAULT nextval('invoice_seq') PRIMARY KEY,
    invoice_number VARCHAR(30)    NOT NULL UNIQUE,
    reservation_id BIGINT         NOT NULL UNIQUE,
    issued_at      TIMESTAMP      NOT NULL,
    total_amount   DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    status         VARCHAR(20)    NOT NULL CHECK (status IN ('OPEN', 'PAID', 'CANCELLED')),
    paid_at        TIMESTAMP,
    CONSTRAINT fk_invoice_reservation FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);
