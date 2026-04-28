CREATE SEQUENCE guest_seq START WITH 1 INCREMENT BY 1 CACHE 50;

CREATE TABLE guest
(
    id            BIGINT       DEFAULT nextval('guest_seq') PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(200) NOT NULL CHECK (email LIKE '%_@_%.__%'),
    phone         VARCHAR(30),
    address       VARCHAR(300),
    date_of_birth DATE,
    created_at    TIMESTAMP    NOT NULL
);
