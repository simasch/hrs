package ch.martinelli.edu.hrs.room.ui;

import ch.martinelli.edu.hrs.room.AvailableRoomType;
import ch.martinelli.edu.hrs.room.RoomAvailabilityRepository;
import ch.martinelli.edu.hrs.room.RoomSearchCriteria;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * UC-001 Search Available Rooms.
 * <p>
 * The guest enters arrival date, departure date and number of guests, starts the search and sees every room
 * type with at least one room free for the whole stay, including the total price (BR-006). Selecting a room
 * type keeps the criteria in the session and continues with Reserve Room Online (UC-002).
 */
@PageTitle("Search Available Rooms")
@Route("")
public class RoomSearchView extends VerticalLayout {

    /** Route of Reserve Room Online (UC-002); the selected room type id is appended as route parameter. */
    public static final String RESERVE_ROUTE = "reserve";

    public static final String MSG_ARRIVAL_REQUIRED = "Arrival date is required";
    public static final String MSG_ARRIVAL_IN_PAST = "Arrival date must not be in the past";
    public static final String MSG_DEPARTURE_REQUIRED = "Departure date is required";
    public static final String MSG_DEPARTURE_NOT_AFTER_ARRIVAL = "Departure date must be after the arrival date";
    public static final String MSG_GUESTS_RANGE = "Number of guests must be between 1 and %d";
    public static final String MSG_NO_ROOMS_AVAILABLE =
            "No rooms are available for the requested criteria. Please adjust the dates or the number of guests.";
    public static final String MSG_NO_ROOM_TYPES = "No room types are defined yet. Room search is not possible.";

    private final transient RoomAvailabilityRepository roomAvailabilityRepository;

    private final DatePicker arrivalDate = new DatePicker("Arrival date");
    private final DatePicker departureDate = new DatePicker("Departure date");
    private final IntegerField numberOfGuests = new IntegerField("Number of guests");
    private final Button searchButton = new Button("Search");

    private final Div noRoomsMessage = new Div();
    private final Grid<AvailableRoomType> resultGrid = new Grid<>();

    private final Binder<RoomSearchCriteria> binder = new Binder<>(RoomSearchCriteria.class);
    private final int maxCapacity;

    private RoomSearchCriteria currentCriteria;

    public RoomSearchView(RoomAvailabilityRepository roomAvailabilityRepository) {
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.maxCapacity = roomAvailabilityRepository.findMaxCapacity().orElse(0);

        setSizeFull();
        add(new H1("Find your room"));
        add(createSearchForm());
        add(noRoomsMessage);
        add(createResultGrid());

        binder.setBean(restoreCriteria());
        noRoomsMessage.setVisible(false);
        resultGrid.setVisible(false);

        if (maxCapacity == 0) {
            // Precondition not met: no room type defined
            noRoomsMessage.setText(MSG_NO_ROOM_TYPES);
            noRoomsMessage.setVisible(true);
            searchButton.setEnabled(false);
        }
    }

    private FormLayout createSearchForm() {
        arrivalDate.setId("arrival-date");
        arrivalDate.setMin(LocalDate.now());
        arrivalDate.setPlaceholder("Arrival");
        // The field's own min validation runs before the Binder validators; give it the A2 message
        arrivalDate.setI18n(new DatePicker.DatePickerI18n().setMinErrorMessage(MSG_ARRIVAL_IN_PAST));

        departureDate.setId("departure-date");
        departureDate.setMin(LocalDate.now().plusDays(1));
        departureDate.setPlaceholder("Departure");

        numberOfGuests.setId("number-of-guests");
        numberOfGuests.setMin(1);
        numberOfGuests.setMax(Math.max(maxCapacity, 1));
        numberOfGuests.setStepButtonsVisible(true);
        // The field's own min/max validation runs before the Binder validators; give it the A3 message
        String guestsMessage = MSG_GUESTS_RANGE.formatted(maxCapacity);
        numberOfGuests.setI18n(new IntegerField.IntegerFieldI18n()
                .setMinErrorMessage(guestsMessage)
                .setMaxErrorMessage(guestsMessage));

        searchButton.setId("search-button");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickShortcut(Key.ENTER);
        searchButton.addClickListener(e -> search());

        // A2 / BR-002: arrival date must be today or later
        binder.forField(arrivalDate)
                .asRequired(MSG_ARRIVAL_REQUIRED)
                .withValidator(date -> !date.isBefore(LocalDate.now()), MSG_ARRIVAL_IN_PAST)
                .bind(RoomSearchCriteria::getArrivalDate, RoomSearchCriteria::setArrivalDate);

        // A1 / BR-001: departure date must be at least one day after the arrival date
        binder.forField(departureDate)
                .asRequired(MSG_DEPARTURE_REQUIRED)
                .withValidator(departureAfterArrival())
                .bind(RoomSearchCriteria::getDepartureDate, RoomSearchCriteria::setDepartureDate);

        // A3 / BR-005: number of guests between 1 and the largest room capacity
        binder.forField(numberOfGuests)
                .asRequired(guestsMessage)
                .withValidator(guests -> guests >= 1 && guests <= maxCapacity, guestsMessage)
                .bind(RoomSearchCriteria::getNumberOfGuests, RoomSearchCriteria::setNumberOfGuests);

        // Re-validate the departure date when the arrival date changes so a stale message disappears
        arrivalDate.addValueChangeListener(e -> {
            if (departureDate.getValue() != null) {
                binder.validate();
            }
        });

        FormLayout form = new FormLayout();
        form.add(arrivalDate, departureDate, numberOfGuests, searchButton);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 4));
        return form;
    }

    private Validator<LocalDate> departureAfterArrival() {
        return (departure, context) -> {
            LocalDate arrival = arrivalDate.getValue();
            if (arrival != null && !departure.isAfter(arrival)) {
                return ValidationResult.error(MSG_DEPARTURE_NOT_AFTER_ARRIVAL);
            }
            return ValidationResult.ok();
        };
    }

    private Grid<AvailableRoomType> createResultGrid() {
        NumberFormat chf = new DecimalFormat("CHF #,##0.00");

        resultGrid.setId("result-grid");
        resultGrid.addColumn(AvailableRoomType::name).setHeader("Room type").setAutoWidth(true);
        resultGrid.addColumn(AvailableRoomType::description).setHeader("Description").setFlexGrow(2);
        resultGrid.addColumn(AvailableRoomType::capacity).setHeader("Capacity").setAutoWidth(true);
        resultGrid.addColumn(new NumberRenderer<>(AvailableRoomType::pricePerNight, chf))
                .setHeader("Price per night").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        resultGrid.addColumn(AvailableRoomType::freeRooms).setHeader("Free rooms").setAutoWidth(true);
        resultGrid.addColumn(new NumberRenderer<>(roomType -> roomType.totalPrice(currentCriteria.getNights()), chf))
                .setHeader("Total price").setAutoWidth(true).setTextAlign(ColumnTextAlign.END)
                .setKey("totalPrice");
        resultGrid.addComponentColumn(roomType -> {
            Button select = new Button("Select", e -> selectRoomType(roomType));
            select.setId("select-room-type-" + roomType.id());
            select.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            return select;
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);
        resultGrid.setAllRowsVisible(true);
        return resultGrid;
    }

    /**
     * Steps 4-7: validate the criteria, determine the free rooms and show the result list.
     */
    private void search() {
        // Steps 5 / A1-A3: validation errors are shown on the fields; the entered values stay in the form
        if (!binder.validate().isOk()) {
            noRoomsMessage.setVisible(false);
            resultGrid.setVisible(false);
            return;
        }

        RoomSearchCriteria criteria = binder.getBean();
        currentCriteria = criteria;
        // Postcondition: retain the criteria for Reserve Room Online (UC-002)
        VaadinSession.getCurrent().setAttribute(RoomSearchCriteria.class, criteria);

        List<AvailableRoomType> roomTypes = roomAvailabilityRepository.findAvailableRoomTypes(
                criteria.getArrivalDate(), criteria.getDepartureDate(), criteria.getNumberOfGuests());

        if (roomTypes.isEmpty()) {
            // A4: no rooms available
            noRoomsMessage.setText(MSG_NO_ROOMS_AVAILABLE);
            noRoomsMessage.setVisible(true);
            resultGrid.setVisible(false);
        } else {
            noRoomsMessage.setVisible(false);
            resultGrid.setItems(roomTypes);
            resultGrid.setVisible(true);
        }
    }

    /**
     * Step 8: the guest selects a room type and continues with the reservation (UC-002).
     */
    private void selectRoomType(AvailableRoomType roomType) {
        VaadinSession.getCurrent().setAttribute(RoomSearchCriteria.class, currentCriteria);
        getUI().ifPresent(ui -> ui.navigate(RESERVE_ROUTE + "/" + roomType.id()));
    }

    /**
     * A5 / postcondition: criteria of a previous search are shown again when the guest returns to the page.
     */
    private RoomSearchCriteria restoreCriteria() {
        RoomSearchCriteria retained = VaadinSession.getCurrent().getAttribute(RoomSearchCriteria.class);
        if (retained != null) {
            return new RoomSearchCriteria(retained.getArrivalDate(), retained.getDepartureDate(),
                    retained.getNumberOfGuests());
        }
        return new RoomSearchCriteria(null, null, 1);
    }
}
