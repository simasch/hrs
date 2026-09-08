-- V900__uc001_search_available_rooms_test_data.sql
-- Test data for UC-001 Search Available Rooms (UC001SearchAvailableRoomsTest).
-- Reservation dates are relative to CURRENT_DATE because a search must not start in the past (BR-002).
--
-- Room types (ordered by price):        rooms
--   9001 Single       cap 1, CHF 120    101 CLEAN, 102 DIRTY
--   9002 Double       cap 2, CHF 180    201 CLEAN, 202 CLEAN, 203 OUT_OF_SERVICE (BR-004)
--   9003 Family Suite cap 4, CHF 320    301 CLEAN
--
-- Reservations:
--   room 301: today+10 .. today+13 CONFIRMED  -> Family Suite blocked for these nights (BR-003)
--   room 201: today+10 .. today+12 CANCELLED  -> does not block (BR-003)
--   room 202: today+7  .. today+10 CONFIRMED  -> guest departs on today+10, room free from today+10 (BR-003)

INSERT INTO room_type (id, name, description, capacity, price)
VALUES (9001, 'Single', 'Cosy room with a single bed', 1, 120.00),
       (9002, 'Double', 'Spacious room with a double bed', 2, 180.00),
       (9003, 'Family Suite', 'Two connected rooms for the whole family', 4, 320.00);

INSERT INTO room (id, room_number, room_type_id, cleaning_status, out_of_service_note)
VALUES (9101, 101, 9001, 'CLEAN', NULL),
       (9102, 102, 9001, 'DIRTY', NULL),
       (9201, 201, 9002, 'CLEAN', NULL),
       (9202, 202, 9002, 'CLEAN', NULL),
       (9203, 203, 9002, 'OUT_OF_SERVICE', 'Water damage, renovation in progress'),
       (9301, 301, 9003, 'CLEAN', NULL);

INSERT INTO guest (id, first_name, last_name, email)
VALUES (9001, 'Anna', 'Muster', 'anna.muster@example.com');

INSERT INTO reservation (id, reservation_number, guest_id, room_id, arrival_date, departure_date,
                         number_of_guests, total_price, source, status, created_at)
VALUES (9001, 'UC001-R1', 9001, 9301, DATEADD('DAY', 10, CURRENT_DATE), DATEADD('DAY', 13, CURRENT_DATE),
        4, 960.00, 'ONLINE', 'CONFIRMED', CURRENT_TIMESTAMP),
       (9002, 'UC001-R2', 9001, 9201, DATEADD('DAY', 10, CURRENT_DATE), DATEADD('DAY', 12, CURRENT_DATE),
        2, 360.00, 'ONLINE', 'CANCELLED', CURRENT_TIMESTAMP),
       (9003, 'UC001-R3', 9001, 9202, DATEADD('DAY', 7, CURRENT_DATE), DATEADD('DAY', 10, CURRENT_DATE),
        2, 540.00, 'ONLINE', 'CONFIRMED', CURRENT_TIMESTAMP);
