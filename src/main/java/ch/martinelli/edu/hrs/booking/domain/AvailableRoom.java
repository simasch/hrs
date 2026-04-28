package ch.martinelli.edu.hrs.booking.domain;

import java.math.BigDecimal;

public record AvailableRoom(
        Long roomId,
        String number,
        String roomTypeName,
        Integer capacity,
        BigDecimal nightlyRate,
        BigDecimal totalPrice
) {
}
