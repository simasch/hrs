package ch.martinelli.edu.hrs.booking.ui;

import ch.martinelli.edu.hrs.booking.domain.AvailableRoom;
import ch.martinelli.edu.hrs.booking.domain.RoomAvailabilityRepository;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@PageTitle("Find a Room")
@Route("")
public class SearchAvailableRoomsView extends Composite<VerticalLayout> {

    static final int MAX_ADVANCE_DAYS = 365;
    static final int MAX_STAY_NIGHTS = 30;
    static final int MIN_GUESTS = 1;
    static final int MAX_GUESTS = 10;

    private final RoomAvailabilityRepository roomAvailabilityRepository;

    private final DatePicker checkInDate = new DatePicker("Check-in date");
    private final DatePicker checkOutDate = new DatePicker("Check-out date");
    private final IntegerField numberOfGuests = new IntegerField("Guests");
    private final Button searchButton = new Button("Search");

    private final Grid<AvailableRoom> resultsGrid = new Grid<>(AvailableRoom.class, false);
    private final Paragraph statusMessage = new Paragraph();

    public SearchAvailableRoomsView(RoomAvailabilityRepository roomAvailabilityRepository) {
        this.roomAvailabilityRepository = roomAvailabilityRepository;

        VerticalLayout root = getContent();
        root.setSizeFull();
        root.setPadding(true);
        root.setSpacing(true);

        root.add(new H1("Find a Room"), buildSearchForm(), statusMessage, resultsGrid);

        configureResultsGrid();
    }

    private VerticalLayout buildSearchForm() {
        LocalDate today = LocalDate.now();
        checkInDate.setMin(today);
        checkInDate.setMax(today.plusDays(MAX_ADVANCE_DAYS));
        checkInDate.setValue(today);

        checkOutDate.setMin(today.plusDays(1));
        checkOutDate.setValue(today.plusDays(1));

        numberOfGuests.setMin(MIN_GUESTS);
        numberOfGuests.setMax(MAX_GUESTS);
        numberOfGuests.setStepButtonsVisible(true);
        numberOfGuests.setValue(1);

        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickShortcut(Key.ENTER);
        searchButton.addClickListener(e -> performSearch());

        FormLayout form = new FormLayout(checkInDate, checkOutDate, numberOfGuests);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3));

        VerticalLayout container = new VerticalLayout(form, new HorizontalLayout(searchButton));
        container.setPadding(false);
        container.setSpacing(true);
        return container;
    }

    private void configureResultsGrid() {
        resultsGrid.addColumn(AvailableRoom::number).setHeader("Room").setAutoWidth(true);
        resultsGrid.addColumn(AvailableRoom::roomTypeName).setHeader("Room type").setAutoWidth(true);
        resultsGrid.addColumn(AvailableRoom::capacity).setHeader("Capacity").setAutoWidth(true);
        resultsGrid.addColumn(room -> formatChf(room.nightlyRate())).setHeader("Nightly rate").setAutoWidth(true);
        resultsGrid.addColumn(room -> formatChf(room.totalPrice())).setHeader("Total for stay").setAutoWidth(true);
        resultsGrid.addColumn(room -> "Available").setHeader("Availability").setAutoWidth(true);
        resultsGrid.setVisible(false);
    }

    private void performSearch() {
        clearFieldErrors();
        statusMessage.setText("");

        LocalDate checkIn = checkInDate.getValue();
        LocalDate checkOut = checkOutDate.getValue();
        Integer guests = numberOfGuests.getValue();

        if (checkIn == null) {
            setError(checkInDate, "Check-in date is required.");
            return;
        }
        if (checkOut == null) {
            setError(checkOutDate, "Check-out date is required.");
            return;
        }
        if (guests == null) {
            setError(numberOfGuests, "Number of guests is required.");
            return;
        }

        // BR-001: Check-in date not in the past
        LocalDate today = LocalDate.now();
        if (checkIn.isBefore(today)) {
            setError(checkInDate, "Check-in date must be today or a future date.");
            return;
        }

        // BR-002: Check-out strictly after check-in (minimum one night)
        if (!checkOut.isAfter(checkIn)) {
            setError(checkOutDate, "Check-out date must be after the check-in date.");
            return;
        }

        // BR-003: Maximum advance booking 365 days
        if (checkIn.isAfter(today.plusDays(MAX_ADVANCE_DAYS))) {
            setError(checkInDate, "Bookings can be made at most " + MAX_ADVANCE_DAYS + " days in advance.");
            return;
        }

        // BR-004: Maximum stay 30 nights — alternative flow A3
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > MAX_STAY_NIGHTS) {
            setError(checkOutDate, "The maximum stay is " + MAX_STAY_NIGHTS + " nights.");
            return;
        }

        if (guests < MIN_GUESTS || guests > MAX_GUESTS) {
            setError(numberOfGuests, "Number of guests must be between " + MIN_GUESTS + " and " + MAX_GUESTS + ".");
            return;
        }

        List<AvailableRoom> rooms = roomAvailabilityRepository.findAvailable(checkIn, checkOut, guests);
        resultsGrid.setItems(rooms);

        if (rooms.isEmpty()) {
            // Alternative flow A2
            resultsGrid.setVisible(false);
            statusMessage.setText("No rooms available for the selected dates. Try adjusting the dates or guest count.");
        } else {
            resultsGrid.setVisible(true);
            statusMessage.setText(rooms.size() + " room(s) available for " + nights + " night(s).");
            Notification.show(rooms.size() + " room(s) found", 2000, Notification.Position.TOP_CENTER);
        }
    }

    private void setError(HasValidation field, String message) {
        field.setInvalid(true);
        field.setErrorMessage(message);
    }

    private void clearFieldErrors() {
        checkInDate.setInvalid(false);
        checkInDate.setErrorMessage(null);
        checkOutDate.setInvalid(false);
        checkOutDate.setErrorMessage(null);
        numberOfGuests.setInvalid(false);
        numberOfGuests.setErrorMessage(null);
    }

    private static String formatChf(BigDecimal amount) {
        return "CHF " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
