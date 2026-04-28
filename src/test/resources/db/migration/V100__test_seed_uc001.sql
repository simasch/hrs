-- Test seed for UC-001 Search Available Rooms.
-- Dates are expressed relative to CURRENT_DATE so the seed stays valid every day.

INSERT INTO room_type (id, name, description, capacity, price) VALUES
    (nextval('room_type_seq'), 'Single', 'Single room',          1, 100.00),
    (nextval('room_type_seq'), 'Double', 'Double room',          2, 150.00),
    (nextval('room_type_seq'), 'Suite',  'Suite for up to four', 4, 300.00);

INSERT INTO room (id, number, floor, room_type_id, status) VALUES
    (nextval('room_seq'), '101', 1, (SELECT id FROM room_type WHERE name = 'Single'), 'AVAILABLE'),
    (nextval('room_seq'), '102', 1, (SELECT id FROM room_type WHERE name = 'Single'), 'AVAILABLE'),
    (nextval('room_seq'), '201', 2, (SELECT id FROM room_type WHERE name = 'Double'), 'AVAILABLE'),
    (nextval('room_seq'), '202', 2, (SELECT id FROM room_type WHERE name = 'Double'), 'AVAILABLE'),
    (nextval('room_seq'), '301', 3, (SELECT id FROM room_type WHERE name = 'Suite'),  'AVAILABLE');

INSERT INTO guest (id, first_name, last_name, email, created_at) VALUES
    (nextval('guest_seq'), 'Alice', 'Test', 'alice@test.example', CURRENT_TIMESTAMP);

-- Reservation matrix to exercise BR-006:
--   101: CONFIRMED today+5 .. today+8        → blocks overlapping searches
--   201: CHECKED_IN today+10 .. today+12     → blocks overlapping searches
--   202: CANCELLED today+5 .. today+8        → must NOT block (cancelled)
--   301: CHECKED_OUT today-30 .. today-25    → must NOT block (in the past, also closed out)
INSERT INTO reservation (id, confirmation_code, guest_id, room_id, check_in_date, check_out_date,
                         number_of_guests, status, total_price, created_at, cancelled_at) VALUES
    (nextval('reservation_seq'), 'TEST-CONF-101',
     (SELECT id FROM guest WHERE last_name = 'Test'),
     (SELECT id FROM room  WHERE number    = '101'),
     CURRENT_DATE + 5,  CURRENT_DATE + 8,  1, 'CONFIRMED',   300.00, CURRENT_TIMESTAMP, NULL),
    (nextval('reservation_seq'), 'TEST-CHIN-201',
     (SELECT id FROM guest WHERE last_name = 'Test'),
     (SELECT id FROM room  WHERE number    = '201'),
     CURRENT_DATE + 10, CURRENT_DATE + 12, 2, 'CHECKED_IN',  300.00, CURRENT_TIMESTAMP, NULL),
    (nextval('reservation_seq'), 'TEST-CANC-202',
     (SELECT id FROM guest WHERE last_name = 'Test'),
     (SELECT id FROM room  WHERE number    = '202'),
     CURRENT_DATE + 5,  CURRENT_DATE + 8,  2, 'CANCELLED',   450.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (nextval('reservation_seq'), 'TEST-CHOUT-301',
     (SELECT id FROM guest WHERE last_name = 'Test'),
     (SELECT id FROM room  WHERE number    = '301'),
     CURRENT_DATE - 30, CURRENT_DATE - 25, 4, 'CHECKED_OUT', 1500.00, CURRENT_TIMESTAMP, NULL);
