package ch.martinelli.edu.hrs.room;

import org.jooq.DSLContext;
import org.jooq.Records;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static ch.martinelli.edu.hrs.db.Tables.*;

/**
 * Data access for UC-001 Search Available Rooms.
 */
@Repository
public class RoomAvailabilityRepository {

    static final String CLEANING_STATUS_OUT_OF_SERVICE = "OUT_OF_SERVICE";
    static final String RESERVATION_STATUS_CANCELLED = "CANCELLED";

    private final DSLContext ctx;

    public RoomAvailabilityRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Finds the room types that have at least one room free for every night of the stay and can accommodate
     * the number of guests, together with the number of free rooms per type (BR-003, BR-004, BR-005, BR-007).
     *
     * @param arrivalDate    first night of the stay
     * @param departureDate  day the guest leaves (exclusive)
     * @param numberOfGuests number of persons staying
     * @return matching room types ordered by price per night
     */
    public List<AvailableRoomType> findAvailableRoomTypes(LocalDate arrivalDate, LocalDate departureDate,
                                                          int numberOfGuests) {
        return ctx
                .select(ROOM_TYPE.ID,
                        ROOM_TYPE.NAME,
                        ROOM_TYPE.DESCRIPTION,
                        ROOM_TYPE.CAPACITY,
                        ROOM_TYPE.PRICE,
                        DSL.count(ROOM.ID))
                .from(ROOM_TYPE)
                .join(ROOM).on(ROOM.ROOM_TYPE_ID.eq(ROOM_TYPE.ID))
                .where(ROOM_TYPE.CAPACITY.ge(numberOfGuests))
                // BR-004: out-of-service rooms are never offered
                .and(ROOM.CLEANING_STATUS.ne(CLEANING_STATUS_OUT_OF_SERVICE))
                // BR-003: no non-cancelled reservation overlaps [arrival, departure)
                .andNotExists(DSL.selectOne()
                        .from(RESERVATION)
                        .where(RESERVATION.ROOM_ID.eq(ROOM.ID))
                        .and(RESERVATION.STATUS.ne(RESERVATION_STATUS_CANCELLED))
                        .and(RESERVATION.ARRIVAL_DATE.lt(departureDate))
                        .and(RESERVATION.DEPARTURE_DATE.gt(arrivalDate)))
                .groupBy(ROOM_TYPE.ID, ROOM_TYPE.NAME, ROOM_TYPE.DESCRIPTION, ROOM_TYPE.CAPACITY, ROOM_TYPE.PRICE)
                .orderBy(ROOM_TYPE.PRICE, ROOM_TYPE.NAME)
                .fetch(Records.mapping(AvailableRoomType::new));
    }

    /**
     * Largest capacity among all room types; the upper bound for the number of guests (BR-005).
     *
     * @return the largest capacity, or empty when no room type is defined
     */
    public Optional<Integer> findMaxCapacity() {
        return ctx
                .select(DSL.max(ROOM_TYPE.CAPACITY))
                .from(ROOM_TYPE)
                .fetchOptional(DSL.max(ROOM_TYPE.CAPACITY));
    }
}
