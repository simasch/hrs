package ch.martinelli.edu.hrs.booking.domain;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ch.martinelli.edu.hrs.db.tables.Reservation.RESERVATION;
import static ch.martinelli.edu.hrs.db.tables.Room.ROOM;
import static ch.martinelli.edu.hrs.db.tables.RoomType.ROOM_TYPE;

@Repository
public class RoomAvailabilityRepository {

    private static final List<String> BLOCKING_RESERVATION_STATUSES = List.of("CONFIRMED", "CHECKED_IN");

    private final DSLContext dsl;

    public RoomAvailabilityRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Returns rooms that match the requested capacity and have no overlapping
     * non-cancelled reservation for the requested date range.
     * Implements BR-005 (capacity match) and BR-006 (no overlap with existing
     * reservations) of UC-001.
     */
    public List<AvailableRoom> findAvailable(LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal nightCount = BigDecimal.valueOf(nights);

        return dsl.select(
                        ROOM.ID,
                        ROOM.NUMBER,
                        ROOM_TYPE.NAME,
                        ROOM_TYPE.CAPACITY,
                        ROOM_TYPE.PRICE)
                .from(ROOM)
                .join(ROOM_TYPE).on(ROOM.ROOM_TYPE_ID.eq(ROOM_TYPE.ID))
                .where(ROOM_TYPE.CAPACITY.ge(numberOfGuests))
                .andNotExists(
                        dsl.selectOne()
                                .from(RESERVATION)
                                .where(RESERVATION.ROOM_ID.eq(ROOM.ID))
                                .and(RESERVATION.STATUS.in(BLOCKING_RESERVATION_STATUSES))
                                .and(RESERVATION.CHECK_IN_DATE.lt(checkOutDate))
                                .and(RESERVATION.CHECK_OUT_DATE.gt(checkInDate)))
                .orderBy(ROOM.NUMBER)
                .fetch(record -> new AvailableRoom(
                        record.get(ROOM.ID),
                        record.get(ROOM.NUMBER),
                        record.get(ROOM_TYPE.NAME),
                        record.get(ROOM_TYPE.CAPACITY),
                        record.get(ROOM_TYPE.PRICE),
                        record.get(ROOM_TYPE.PRICE).multiply(nightCount)));
    }
}
