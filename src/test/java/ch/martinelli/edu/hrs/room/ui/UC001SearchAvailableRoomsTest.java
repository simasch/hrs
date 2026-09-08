package ch.martinelli.edu.hrs.room.ui;

import ch.martinelli.edu.hrs.core.ui.HrsBrowserlessTest;
import ch.martinelli.edu.hrs.core.usecase.UseCase;
import ch.martinelli.edu.hrs.room.AvailableRoomType;
import ch.martinelli.edu.hrs.room.RoomSearchCriteria;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Use case test for UC-001 "Search Available Rooms".
 * <p>
 * Test data: see {@code src/test/resources/db/migration/V900__uc001_search_available_rooms_test_data.sql}.
 * Room types Single (capacity 1, CHF 120), Double (capacity 2, CHF 180) and Family Suite (capacity 4, CHF 320).
 * The Family Suite is booked from today+10 to today+13; one Double room is out of service.
 */
class UC001SearchAvailableRoomsTest extends HrsBrowserlessTest {

    private static final long SINGLE_ID = 9001;
    private static final long DOUBLE_ID = 9002;
    private static final long FAMILY_SUITE_ID = 9003;
    private static final int MAX_CAPACITY = 4;

    private static final int COL_NAME = 0;
    private static final int COL_DESCRIPTION = 1;
    private static final int COL_CAPACITY = 2;
    private static final int COL_PRICE_PER_NIGHT = 3;
    private static final int COL_FREE_ROOMS = 4;
    private static final int COL_TOTAL_PRICE = 5;
    private static final int COL_SELECT = 6;

    private final LocalDate today = LocalDate.now();
    /** Stay overlapping the Family Suite reservation (today+10 .. today+13). */
    private final LocalDate bookedArrival = today.plusDays(10);
    private final LocalDate bookedDeparture = today.plusDays(12);
    /** Stay with no reservations at all. */
    private final LocalDate freeArrival = today.plusDays(20);
    private final LocalDate freeDeparture = today.plusDays(21);

    private DatePicker arrivalDate;
    private DatePicker departureDate;
    private IntegerField numberOfGuests;
    private Button searchButton;
    private Div noRoomsMessage;
    private Grid<AvailableRoomType> resultGrid;

    @BeforeEach
    void openRoomSearchPage() {
        // Step 1: the guest opens the room search page
        RoomSearchView view = navigate(RoomSearchView.class);

        arrivalDate = find(DatePicker.class).id("arrival-date");
        departureDate = find(DatePicker.class).id("departure-date");
        numberOfGuests = find(IntegerField.class).id("number-of-guests");
        searchButton = find(Button.class).id("search-button");
        // Message and result list are hidden until the first search, so a component query would not find them
        noRoomsMessage = childOfView(view, Div.class);
        resultGrid = childOfView(view, Grid.class);
    }

    @AfterEach
    void forgetRetainedCriteria() {
        // The criteria are retained in the session for UC-002; do not leak them into the next test
        VaadinSession.getCurrent().setAttribute(RoomSearchCriteria.class, null);
    }

    // ---------------------------------------------------------------------------------------------
    // Main Success Scenario
    // ---------------------------------------------------------------------------------------------

    @Test
    @UseCase(id = "UC-001")
    void search_form_is_displayed_with_arrival_departure_and_number_of_guests() {
        // Step 2: search form with arrival date, departure date and number of guests
        assertThat(getCurrentView()).isInstanceOf(RoomSearchView.class);
        assertThat(arrivalDate.getLabel()).isEqualTo("Arrival date");
        assertThat(departureDate.getLabel()).isEqualTo("Departure date");
        assertThat(numberOfGuests.getLabel()).isEqualTo("Number of guests");
        assertThat(numberOfGuests.getValue()).isEqualTo(1);
        assertThat(searchButton.isEnabled()).isTrue();

        // No result list and no message before the first search
        assertThat(resultGrid.isVisible()).isFalse();
        assertThat(noRoomsMessage.isVisible()).isFalse();
    }

    @Test
    @UseCase(id = "UC-001", businessRules = {"BR-003", "BR-004", "BR-006", "BR-007"})
    void search_shows_matching_room_types_with_free_rooms_and_total_price() {
        // Steps 3-4: enter criteria and start the search (2 nights, 1 guest)
        search(bookedArrival, bookedDeparture, 1);

        // Step 7: matching room types with name, description, capacity, price per night, free rooms, total price
        assertThat(noRoomsMessage.isVisible()).isFalse();
        assertThat(resultGrid.isVisible()).isTrue();
        assertThat(test(resultGrid).size()).isEqualTo(2);

        assertThat(resultGrid.getColumns().get(COL_NAME).getHeaderText()).isEqualTo("Room type");
        assertThat(resultGrid.getColumns().get(COL_DESCRIPTION).getHeaderText()).isEqualTo("Description");
        assertThat(resultGrid.getColumns().get(COL_CAPACITY).getHeaderText()).isEqualTo("Capacity");
        assertThat(resultGrid.getColumns().get(COL_PRICE_PER_NIGHT).getHeaderText()).isEqualTo("Price per night");
        assertThat(resultGrid.getColumns().get(COL_FREE_ROOMS).getHeaderText()).isEqualTo("Free rooms");
        assertThat(resultGrid.getColumns().get(COL_TOTAL_PRICE).getHeaderText()).isEqualTo("Total price");

        // BR-007: one row per room type, ordered by price
        assertThat(test(resultGrid).getCellText(0, COL_NAME)).isEqualTo("Single");
        assertThat(test(resultGrid).getCellText(0, COL_DESCRIPTION)).isEqualTo("Cosy room with a single bed");
        assertThat(test(resultGrid).getCellText(0, COL_CAPACITY)).isEqualTo("1");
        assertThat(test(resultGrid).getCellText(0, COL_PRICE_PER_NIGHT)).isEqualTo("CHF 120.00");
        // Both Single rooms are free: a dirty room is still offered, only out-of-service rooms are excluded
        assertThat(test(resultGrid).getCellText(0, COL_FREE_ROOMS)).isEqualTo("2");
        // BR-006: 2 nights x CHF 120.00
        assertThat(test(resultGrid).getCellText(0, COL_TOTAL_PRICE)).isEqualTo("CHF 240.00");

        assertThat(test(resultGrid).getCellText(1, COL_NAME)).isEqualTo("Double");
        assertThat(test(resultGrid).getCellText(1, COL_CAPACITY)).isEqualTo("2");
        assertThat(test(resultGrid).getCellText(1, COL_PRICE_PER_NIGHT)).isEqualTo("CHF 180.00");
        // BR-003: the cancelled reservation on room 201 does not block it, and room 202's previous guest
        // departs on the arrival date; BR-004: room 203 is out of service and not counted
        assertThat(test(resultGrid).getCellText(1, COL_FREE_ROOMS)).isEqualTo("2");
        assertThat(test(resultGrid).getCellText(1, COL_TOTAL_PRICE)).isEqualTo("CHF 360.00");

        // The Family Suite is booked for these nights and therefore not listed (BR-003)
        assertThat(test(resultGrid).getRow(0).id()).isEqualTo(SINGLE_ID);
        assertThat(test(resultGrid).getRow(1).id()).isEqualTo(DOUBLE_ID);
    }

    @Test
    @UseCase(id = "UC-001", businessRules = {"BR-003"})
    void room_type_is_listed_again_once_the_previous_guest_has_departed() {
        // The Family Suite reservation ends on today+13: arriving that day is allowed (BR-003)
        search(today.plusDays(13), today.plusDays(14), MAX_CAPACITY);

        assertThat(resultGrid.isVisible()).isTrue();
        assertThat(test(resultGrid).size()).isEqualTo(1);
        assertThat(test(resultGrid).getRow(0).id()).isEqualTo(FAMILY_SUITE_ID);
        assertThat(test(resultGrid).getCellText(0, COL_FREE_ROOMS)).isEqualTo("1");
        // BR-006: 1 night x CHF 320.00
        assertThat(test(resultGrid).getCellText(0, COL_TOTAL_PRICE)).isEqualTo("CHF 320.00");
    }

    @Test
    @UseCase(id = "UC-001", businessRules = {"BR-005"})
    void only_room_types_with_sufficient_capacity_are_listed() {
        // 3 guests: Single (1) and Double (2) are too small, only the Family Suite (4) fits
        search(freeArrival, freeDeparture, 3);

        assertThat(resultGrid.isVisible()).isTrue();
        assertThat(test(resultGrid).size()).isEqualTo(1);
        assertThat(test(resultGrid).getCellText(0, COL_NAME)).isEqualTo("Family Suite");
        assertThat(test(resultGrid).getCellText(0, COL_CAPACITY)).isEqualTo("4");
    }

    @Test
    @UseCase(id = "UC-001")
    void search_can_be_started_with_enter() {
        test(arrivalDate).setValue(freeArrival);
        test(departureDate).setValue(freeDeparture);
        test(numberOfGuests).setValue(1);

        // Step 4: the guest starts the search with the keyboard
        fireShortcut(Key.ENTER);

        assertThat(resultGrid.isVisible()).isTrue();
        assertThat(test(resultGrid).size()).isEqualTo(3);
    }

    @Test
    @UseCase(id = "UC-001")
    void selecting_a_room_type_continues_with_the_reservation() {
        search(bookedArrival, bookedDeparture, 2);
        assertThat(test(resultGrid).size()).isEqualTo(1);

        // Step 8: the guest selects a room type
        Button select = (Button) test(resultGrid).getCellComponent(0, COL_SELECT);
        assertThat(select.getText()).isEqualTo("Select");
        assertThat(select.getId()).contains("select-room-type-" + DOUBLE_ID);
        test(select).click();

        // The guest is taken to Reserve Room Online (UC-002) for the selected room type
        assertThat(UI.getCurrent().getInternals().getActiveViewLocation().getPath())
                .isEqualTo(RoomSearchView.RESERVE_ROUTE + "/" + DOUBLE_ID);

        // Postcondition: the search criteria are retained for UC-002
        RoomSearchCriteria retained = VaadinSession.getCurrent().getAttribute(RoomSearchCriteria.class);
        assertThat(retained).isNotNull();
        assertThat(retained.getArrivalDate()).isEqualTo(bookedArrival);
        assertThat(retained.getDepartureDate()).isEqualTo(bookedDeparture);
        assertThat(retained.getNumberOfGuests()).isEqualTo(2);
    }

    @Test
    @UseCase(id = "UC-001")
    void search_criteria_are_retained_and_shown_again_when_the_guest_returns() {
        search(freeArrival, freeDeparture, 2);

        // Postcondition: the criteria are retained so that UC-002 can use them
        RoomSearchCriteria retained = VaadinSession.getCurrent().getAttribute(RoomSearchCriteria.class);
        assertThat(retained).isNotNull();
        assertThat(retained.getArrivalDate()).isEqualTo(freeArrival);
        assertThat(retained.getDepartureDate()).isEqualTo(freeDeparture);
        assertThat(retained.getNumberOfGuests()).isEqualTo(2);
        assertThat(retained.getNights()).isEqualTo(1);

        // Coming back to the page shows the retained criteria in the form
        navigate(RoomSearchView.class);
        assertThat(find(DatePicker.class).id("arrival-date").getValue()).isEqualTo(freeArrival);
        assertThat(find(DatePicker.class).id("departure-date").getValue()).isEqualTo(freeDeparture);
        assertThat(find(IntegerField.class).id("number-of-guests").getValue()).isEqualTo(2);
    }

    @Test
    @UseCase(id = "UC-001")
    void searching_does_not_change_room_availability() {
        // Postcondition: no reservation is created and no room is held by a search
        search(freeArrival, freeDeparture, 1);
        assertThat(test(resultGrid).getCellText(1, COL_FREE_ROOMS)).isEqualTo("2");

        test(searchButton).click();
        assertThat(test(resultGrid).size()).isEqualTo(3);
        assertThat(test(resultGrid).getCellText(1, COL_FREE_ROOMS)).isEqualTo("2");
    }

    // ---------------------------------------------------------------------------------------------
    // Alternative Flows
    // ---------------------------------------------------------------------------------------------

    @Test
    @UseCase(id = "UC-001", scenario = "A1: Departure Date Not After Arrival Date", businessRules = {"BR-001"})
    void departure_on_arrival_date_shows_message_and_keeps_entered_values() {
        test(arrivalDate).setValue(freeArrival);
        test(departureDate).setValue(freeArrival);
        test(numberOfGuests).setValue(2);

        test(searchButton).click();

        // A1.1: message that the departure date must be after the arrival date
        assertThat(departureDate.isInvalid()).isTrue();
        assertThat(departureDate.getErrorMessage()).isEqualTo(RoomSearchView.MSG_DEPARTURE_NOT_AFTER_ARRIVAL);
        assertThat(arrivalDate.isInvalid()).isFalse();

        // A1.2: the entered values stay in the form; no result list is shown
        assertThat(arrivalDate.getValue()).isEqualTo(freeArrival);
        assertThat(departureDate.getValue()).isEqualTo(freeArrival);
        assertThat(numberOfGuests.getValue()).isEqualTo(2);
        assertThat(resultGrid.isVisible()).isFalse();
        assertThat(noRoomsMessage.isVisible()).isFalse();
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A1: Departure Date Not After Arrival Date", businessRules = {"BR-001"})
    void departure_before_arrival_date_shows_message() {
        test(arrivalDate).setValue(freeDeparture);
        test(departureDate).setValue(freeArrival);
        test(numberOfGuests).setValue(1);

        test(searchButton).click();

        assertThat(departureDate.isInvalid()).isTrue();
        assertThat(departureDate.getErrorMessage()).isEqualTo(RoomSearchView.MSG_DEPARTURE_NOT_AFTER_ARRIVAL);
        assertThat(resultGrid.isVisible()).isFalse();
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A1: Departure Date Not After Arrival Date", businessRules = {"BR-001"})
    void correcting_the_departure_date_continues_the_search() {
        test(arrivalDate).setValue(freeArrival);
        test(departureDate).setValue(freeArrival);
        test(numberOfGuests).setValue(1);
        test(searchButton).click();
        assertThat(departureDate.isInvalid()).isTrue();

        // A1.3: use case continues at step 3 with a corrected departure date
        test(departureDate).setValue(freeDeparture);
        test(searchButton).click();

        assertThat(departureDate.isInvalid()).isFalse();
        assertThat(resultGrid.isVisible()).isTrue();
        assertThat(test(resultGrid).size()).isEqualTo(3);
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A2: Arrival Date in the Past", businessRules = {"BR-002"})
    void arrival_date_in_the_past_shows_message_and_keeps_entered_values() {
        LocalDate yesterday = today.minusDays(1);
        // The date picker's minimum is today; a manually typed past date reaches the server as a value
        arrivalDate.setValue(yesterday);
        test(departureDate).setValue(today.plusDays(1));
        test(numberOfGuests).setValue(1);

        test(searchButton).click();

        // A2.1: message that the arrival date must not be in the past
        assertThat(arrivalDate.isInvalid()).isTrue();
        assertThat(arrivalDate.getErrorMessage()).isEqualTo(RoomSearchView.MSG_ARRIVAL_IN_PAST);

        // A2.2: the entered values stay in the form; no result list is shown
        assertThat(arrivalDate.getValue()).isEqualTo(yesterday);
        assertThat(departureDate.getValue()).isEqualTo(today.plusDays(1));
        assertThat(numberOfGuests.getValue()).isEqualTo(1);
        assertThat(resultGrid.isVisible()).isFalse();
        assertThat(noRoomsMessage.isVisible()).isFalse();
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A2: Arrival Date in the Past", businessRules = {"BR-002"})
    void arrival_today_is_allowed() {
        search(today, today.plusDays(1), 1);

        assertThat(arrivalDate.isInvalid()).isFalse();
        assertThat(resultGrid.isVisible()).isTrue();
        assertThat(test(resultGrid).size()).isEqualTo(3);
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A3: Invalid Number of Guests", businessRules = {"BR-005"})
    void missing_number_of_guests_shows_allowed_range() {
        test(arrivalDate).setValue(freeArrival);
        test(departureDate).setValue(freeDeparture);
        numberOfGuests.clear();

        test(searchButton).click();

        assertThat(numberOfGuests.isInvalid()).isTrue();
        assertThat(numberOfGuests.getErrorMessage())
                .isEqualTo(RoomSearchView.MSG_GUESTS_RANGE.formatted(MAX_CAPACITY));
        assertThat(resultGrid.isVisible()).isFalse();
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A3: Invalid Number of Guests", businessRules = {"BR-005"})
    void zero_guests_shows_allowed_range_and_keeps_entered_values() {
        test(arrivalDate).setValue(freeArrival);
        test(departureDate).setValue(freeDeparture);
        // The field's minimum is 1; a manually typed 0 reaches the server as a value
        numberOfGuests.setValue(0);

        test(searchButton).click();

        // A3.1: message stating the allowed range
        assertThat(numberOfGuests.isInvalid()).isTrue();
        assertThat(numberOfGuests.getErrorMessage()).isEqualTo("Number of guests must be between 1 and 4");

        // A3.2: the entered values stay in the form; no result list is shown
        assertThat(arrivalDate.getValue()).isEqualTo(freeArrival);
        assertThat(departureDate.getValue()).isEqualTo(freeDeparture);
        assertThat(numberOfGuests.getValue()).isEqualTo(0);
        assertThat(resultGrid.isVisible()).isFalse();
        assertThat(noRoomsMessage.isVisible()).isFalse();
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A3: Invalid Number of Guests", businessRules = {"BR-005"})
    void more_guests_than_the_largest_capacity_shows_allowed_range() {
        test(arrivalDate).setValue(freeArrival);
        test(departureDate).setValue(freeDeparture);
        // Largest room capacity is 4; a manually typed 5 reaches the server as a value
        numberOfGuests.setValue(MAX_CAPACITY + 1);

        test(searchButton).click();

        assertThat(numberOfGuests.isInvalid()).isTrue();
        assertThat(numberOfGuests.getErrorMessage()).isEqualTo("Number of guests must be between 1 and 4");
        assertThat(numberOfGuests.getValue()).isEqualTo(MAX_CAPACITY + 1);
        assertThat(resultGrid.isVisible()).isFalse();
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A3: Invalid Number of Guests", businessRules = {"BR-005"})
    void largest_capacity_is_a_valid_number_of_guests() {
        search(freeArrival, freeDeparture, MAX_CAPACITY);

        assertThat(numberOfGuests.isInvalid()).isFalse();
        assertThat(resultGrid.isVisible()).isTrue();
        assertThat(test(resultGrid).size()).isEqualTo(1);
        assertThat(test(resultGrid).getCellText(0, COL_NAME)).isEqualTo("Family Suite");
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A4: No Rooms Available", businessRules = {"BR-003", "BR-005"})
    void no_rooms_available_shows_message_suggesting_other_criteria() {
        // 4 guests fit only the Family Suite, which is booked for these nights
        search(bookedArrival, bookedDeparture, MAX_CAPACITY);

        // A4.1 + A4.2: message that no rooms are available, suggesting other dates or party size
        assertThat(resultGrid.isVisible()).isFalse();
        assertThat(noRoomsMessage.isVisible()).isTrue();
        assertThat(noRoomsMessage.getText()).isEqualTo(RoomSearchView.MSG_NO_ROOMS_AVAILABLE);
        assertThat(noRoomsMessage.getText())
                .contains("No rooms are available")
                .contains("adjust the dates or the number of guests");

        // The entered values stay in the form so the guest can change them
        assertThat(arrivalDate.getValue()).isEqualTo(bookedArrival);
        assertThat(departureDate.getValue()).isEqualTo(bookedDeparture);
        assertThat(numberOfGuests.getValue()).isEqualTo(MAX_CAPACITY);
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A4: No Rooms Available", businessRules = {"BR-003"})
    void changing_the_dates_after_no_rooms_available_shows_results() {
        search(bookedArrival, bookedDeparture, MAX_CAPACITY);
        assertThat(noRoomsMessage.isVisible()).isTrue();

        // A4.3 / A4.4: the guest changes the dates and the use case continues at step 3
        test(arrivalDate).setValue(freeArrival);
        test(departureDate).setValue(freeDeparture);
        test(searchButton).click();

        assertThat(noRoomsMessage.isVisible()).isFalse();
        assertThat(resultGrid.isVisible()).isTrue();
        assertThat(test(resultGrid).size()).isEqualTo(1);
        assertThat(test(resultGrid).getCellText(0, COL_NAME)).isEqualTo("Family Suite");
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A5: Guest Refines the Search", businessRules = {"BR-005", "BR-006"})
    void refining_the_search_replaces_the_result_list() {
        search(freeArrival, freeDeparture, 1);
        assertThat(test(resultGrid).size()).isEqualTo(3);
        assertThat(test(resultGrid).getCellText(0, COL_TOTAL_PRICE)).isEqualTo("CHF 120.00");

        // A5.1: the guest changes the number of guests and the departure date
        test(numberOfGuests).setValue(2);
        test(departureDate).setValue(freeArrival.plusDays(3));
        // A5.2: use case continues at step 4
        test(searchButton).click();

        assertThat(resultGrid.isVisible()).isTrue();
        assertThat(test(resultGrid).size()).isEqualTo(2);
        assertThat(test(resultGrid).getCellText(0, COL_NAME)).isEqualTo("Double");
        // BR-006: 3 nights x CHF 180.00
        assertThat(test(resultGrid).getCellText(0, COL_TOTAL_PRICE)).isEqualTo("CHF 540.00");
        assertThat(test(resultGrid).getCellText(1, COL_NAME)).isEqualTo("Family Suite");
        assertThat(test(resultGrid).getCellText(1, COL_TOTAL_PRICE)).isEqualTo("CHF 960.00");

        // The retained criteria follow the refined search
        RoomSearchCriteria retained = VaadinSession.getCurrent().getAttribute(RoomSearchCriteria.class);
        assertThat(retained.getNumberOfGuests()).isEqualTo(2);
        assertThat(retained.getDepartureDate()).isEqualTo(freeArrival.plusDays(3));
    }

    @Test
    @UseCase(id = "UC-001", scenario = "A5: Guest Refines the Search", businessRules = {"BR-003"})
    void refining_to_booked_dates_hides_the_result_list() {
        search(freeArrival, freeDeparture, MAX_CAPACITY);
        assertThat(resultGrid.isVisible()).isTrue();

        // A5.1: the guest moves the stay onto the nights the Family Suite is booked
        test(arrivalDate).setValue(bookedArrival);
        test(departureDate).setValue(bookedDeparture);
        test(searchButton).click();

        assertThat(resultGrid.isVisible()).isFalse();
        assertThat(noRoomsMessage.isVisible()).isTrue();
        assertThat(noRoomsMessage.getText()).isEqualTo(RoomSearchView.MSG_NO_ROOMS_AVAILABLE);
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static <T> T childOfView(RoomSearchView view, Class<?> type) {
        return (T) view.getChildren().filter(type::isInstance).findFirst().orElseThrow();
    }

    /**
     * Steps 3-4: enter the criteria and start the search.
     */
    private void search(LocalDate arrival, LocalDate departure, int guests) {
        test(arrivalDate).setValue(arrival);
        test(departureDate).setValue(departure);
        test(numberOfGuests).setValue(guests);
        test(searchButton).click();
    }
}
