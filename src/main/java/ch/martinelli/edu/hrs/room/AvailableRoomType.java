package ch.martinelli.edu.hrs.room;

import java.math.BigDecimal;

/**
 * A room type with at least one room free for the requested stay (UC-001, BR-007).
 *
 * @param id            room type id
 * @param name          name of the room type
 * @param description   description shown to guests
 * @param capacity      maximum number of guests
 * @param pricePerNight price per night in CHF
 * @param freeRooms     number of rooms of this type that are free for the whole stay
 */
public record AvailableRoomType(Long id,
                                String name,
                                String description,
                                Integer capacity,
                                BigDecimal pricePerNight,
                                Integer freeRooms) {

    /**
     * BR-006: total price of the stay = price per night multiplied by the number of nights.
     */
    public BigDecimal totalPrice(long nights) {
        return pricePerNight.multiply(BigDecimal.valueOf(nights));
    }
}
