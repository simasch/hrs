package ch.martinelli.edu.hrs.booking.ui;

import ch.martinelli.edu.hrs.booking.domain.AvailableRoom;
import ch.martinelli.edu.hrs.core.ui.HrsBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchAvailableRoomsViewTest extends HrsBrowserlessTest {

    @Test
    void main_flow_returns_all_rooms_when_no_reservations_overlap() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        submitSearch(today.plusDays(1), today.plusDays(2), 1);

        Grid<AvailableRoom> grid = grid();
        assertThat(grid.isVisible()).isTrue();
        assertThat(test(grid).size()).isEqualTo(5);
        assertThat($(Notification.class).exists()).isTrue();
    }

    @Test
    void filters_by_capacity_per_BR005() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        submitSearch(today.plusDays(1), today.plusDays(2), 3);

        Grid<AvailableRoom> grid = grid();
        assertThat(test(grid).size()).isEqualTo(1);
        AvailableRoom only = test(grid).getRow(0);
        assertThat(only.number()).isEqualTo("301");
        assertThat(only.capacity()).isEqualTo(4);
    }

    @Test
    void excludes_rooms_with_overlapping_confirmed_reservation_per_BR006() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        // Search inside the CONFIRMED reservation range for room 101 (today+5..today+8)
        submitSearch(today.plusDays(6), today.plusDays(7), 1);

        Grid<AvailableRoom> grid = grid();
        List<String> roomNumbers = roomNumbers(grid);
        assertThat(roomNumbers).doesNotContain("101");
    }

    @Test
    void excludes_rooms_with_overlapping_checked_in_reservation_per_BR006() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        // Search inside the CHECKED_IN reservation range for room 201 (today+10..today+12)
        submitSearch(today.plusDays(10), today.plusDays(11), 2);

        Grid<AvailableRoom> grid = grid();
        assertThat(roomNumbers(grid)).doesNotContain("201");
    }

    @Test
    void cancelled_reservation_does_not_block_room() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        // Room 202 has a CANCELLED reservation today+5..today+8 — it must still appear.
        submitSearch(today.plusDays(6), today.plusDays(7), 2);

        Grid<AvailableRoom> grid = grid();
        assertThat(roomNumbers(grid)).contains("202");
    }

    @Test
    void boundary_search_starting_on_existing_checkout_does_not_overlap() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        // Room 101 ends exactly at today+8; searching today+8..today+9 must NOT exclude it.
        submitSearch(today.plusDays(8), today.plusDays(9), 1);

        Grid<AvailableRoom> grid = grid();
        assertThat(roomNumbers(grid)).contains("101");
    }

    @Test
    void empty_results_show_no_rooms_message_per_alternative_flow_A2() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        // 5 guests exceeds every room type's capacity (max is Suite = 4).
        submitSearch(today.plusDays(1), today.plusDays(2), 5);

        // The grid is hidden in the A2 flow, so $() — which filters invisible components — must not find it.
        assertThat($(Grid.class).exists()).isFalse();
        assertThat($(Paragraph.class).single().getText())
                .containsIgnoringCase("No rooms available");
    }

    @Test
    void rejects_check_in_in_the_past_per_BR001() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        DatePicker checkIn = checkInField();
        DatePicker checkOut = checkOutField();
        IntegerField guests = guestsField();

        // The DatePicker tester rejects values outside the field's configured min/max,
        // so we set the past date through the component API to exercise the server-side guard.
        checkIn.setValue(today.minusDays(1));
        test(checkOut).setValue(today.plusDays(1));
        guests.setValue(1);
        clickSearch();

        assertThat(checkIn.isInvalid()).isTrue();
        assertThat(checkIn.getErrorMessage()).containsIgnoringCase("today or a future date");
        // Form values are retained per alternative flow A1
        assertThat(checkIn.getValue()).isEqualTo(today.minusDays(1));
        assertThat(checkOut.getValue()).isEqualTo(today.plusDays(1));
        assertThat($(Grid.class).exists()).isFalse();
    }

    @Test
    void rejects_check_out_not_after_check_in_per_BR002() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        DatePicker checkOut = checkOutField();

        test(checkInField()).setValue(today.plusDays(3));
        test(checkOut).setValue(today.plusDays(3));
        guestsField().setValue(1);
        clickSearch();

        assertThat(checkOut.isInvalid()).isTrue();
        assertThat(checkOut.getErrorMessage()).containsIgnoringCase("after the check-in date");
    }

    @Test
    void rejects_stay_exceeding_max_length_per_BR004_alternative_flow_A3() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        DatePicker checkOut = checkOutField();

        test(checkInField()).setValue(today.plusDays(1));
        test(checkOut).setValue(today.plusDays(1 + SearchAvailableRoomsView.MAX_STAY_NIGHTS + 1));
        guestsField().setValue(1);
        clickSearch();

        assertThat(checkOut.isInvalid()).isTrue();
        assertThat(checkOut.getErrorMessage())
                .contains(String.valueOf(SearchAvailableRoomsView.MAX_STAY_NIGHTS));
    }

    @Test
    void rejects_check_in_more_than_max_advance_days_per_BR003() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        DatePicker checkIn = checkInField();

        // Bypass the tester's min/max enforcement so the server-side BR-003 guard runs.
        checkIn.setValue(today.plusDays(SearchAvailableRoomsView.MAX_ADVANCE_DAYS + 1));
        checkOutField().setValue(today.plusDays(SearchAvailableRoomsView.MAX_ADVANCE_DAYS + 2));
        guestsField().setValue(1);
        clickSearch();

        assertThat(checkIn.isInvalid()).isTrue();
        assertThat(checkIn.getErrorMessage())
                .contains(String.valueOf(SearchAvailableRoomsView.MAX_ADVANCE_DAYS));
    }

    @Test
    void total_price_equals_nightly_rate_times_nights() {
        navigate(SearchAvailableRoomsView.class);

        LocalDate today = LocalDate.now();
        // 3-night stay; well outside any seeded reservation window
        submitSearch(today.plusDays(20), today.plusDays(23), 1);

        Grid<AvailableRoom> grid = grid();
        AvailableRoom single101 = roomByNumber(grid, "101");
        assertThat(single101.nightlyRate()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(single101.totalPrice()).isEqualByComparingTo(new BigDecimal("300.00"));

        AvailableRoom suite301 = roomByNumber(grid, "301");
        assertThat(suite301.totalPrice()).isEqualByComparingTo(new BigDecimal("900.00"));
    }

    // --- Helpers -----------------------------------------------------------------

    private void submitSearch(LocalDate checkIn, LocalDate checkOut, int guests) {
        test(checkInField()).setValue(checkIn);
        test(checkOutField()).setValue(checkOut);
        guestsField().setValue(guests);
        clickSearch();
    }

    private void clickSearch() {
        test($(Button.class).withText("Search").single()).click();
    }

    private DatePicker checkInField() {
        return $(DatePicker.class).withCaption("Check-in date").single();
    }

    private DatePicker checkOutField() {
        return $(DatePicker.class).withCaption("Check-out date").single();
    }

    private IntegerField guestsField() {
        return $(IntegerField.class).withCaption("Guests").single();
    }

    @SuppressWarnings("unchecked")
    private Grid<AvailableRoom> grid() {
        return (Grid<AvailableRoom>) $(Grid.class).single();
    }

    private List<String> roomNumbers(Grid<AvailableRoom> grid) {
        int size = test(grid).size();
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> test(grid).getRow(i).number())
                .toList();
    }

    private AvailableRoom roomByNumber(Grid<AvailableRoom> grid, String number) {
        int size = test(grid).size();
        for (int i = 0; i < size; i++) {
            AvailableRoom row = test(grid).getRow(i);
            if (number.equals(row.number())) {
                return row;
            }
        }
        throw new AssertionError("Room " + number + " not found in grid");
    }
}
