package ch.martinelli.edu.hrs.room;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Search criteria entered by the guest in UC-001. The criteria are retained in the Vaadin session so that
 * Reserve Room Online (UC-002) can reuse them.
 */
public class RoomSearchCriteria {

    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private Integer numberOfGuests;

    public RoomSearchCriteria() {
    }

    public RoomSearchCriteria(LocalDate arrivalDate, LocalDate departureDate, Integer numberOfGuests) {
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.numberOfGuests = numberOfGuests;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    /**
     * Number of nights of the stay (BR-001 guarantees at least one).
     */
    public long getNights() {
        return ChronoUnit.DAYS.between(arrivalDate, departureDate);
    }
}
