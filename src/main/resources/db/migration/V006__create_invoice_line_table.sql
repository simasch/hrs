CREATE SEQUENCE invoice_line_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE invoice_line
(
    id          BIGINT         DEFAULT nextval('invoice_line_seq') PRIMARY KEY,
    invoice_id  BIGINT         NOT NULL,
    description VARCHAR(200)   NOT NULL,
    quantity    INTEGER        NOT NULL CHECK (quantity >= 1),
    unit_price  DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    line_total  DECIMAL(10, 2) NOT NULL CHECK (line_total >= 0),
    CONSTRAINT fk_invoice_line_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id),
    CONSTRAINT chk_invoice_line_total CHECK (line_total = quantity * unit_price)
);

CREATE INDEX idx_invoice_line_invoice ON invoice_line (invoice_id);
