# Use Case: Search Available Rooms

## Overview

**Use Case ID:** UC-001  
**Use Case Name:** Search Available Rooms  
**Primary Actor:** Guest  
**Goal:** Find the rooms that are free for the desired stay and suitable for the travelling party, so the guest can decide which room to reserve  
**Status:** Draft

## Preconditions

- Guest has opened the hotel's online booking site; no login is required
- At least one room type with at least one room is defined in the system

## Main Success Scenario

1. Guest opens the room search page.
2. System displays the search form with arrival date, departure date, and number of guests.
3. Guest enters the arrival date, the departure date, and the number of guests.
4. Guest starts the search.
5. System validates the search criteria.
6. System determines which rooms are free for every night of the requested stay and can accommodate the number of guests.
7. System displays the matching room types with name, description, capacity, price per night, the number of free rooms, and the total price for the stay.
8. Guest reviews the list and selects a room type to continue with the reservation. The guest has found an available room for the desired stay.

## Alternative Flows

### A1: Departure Date Not After Arrival Date

**Trigger:** The departure date is on or before the arrival date (step 5)  
**Flow:**

1. System displays a message that the departure date must be after the arrival date.
2. System keeps the entered values in the form.
3. Use case continues at step 3.

### A2: Arrival Date in the Past

**Trigger:** The arrival date lies before today's date (step 5)  
**Flow:**

1. System displays a message that the arrival date must not be in the past.
2. System keeps the entered values in the form.
3. Use case continues at step 3.

### A3: Invalid Number of Guests

**Trigger:** The number of guests is missing, less than one, or greater than the largest room capacity of the hotel (step 5)  
**Flow:**

1. System displays a message stating the allowed range for the number of guests.
2. System keeps the entered values in the form.
3. Use case continues at step 3.

### A4: No Rooms Available

**Trigger:** No room is free for the entire stay and suitable for the number of guests (step 6)  
**Flow:**

1. System displays a message that no rooms are available for the requested criteria.
2. System suggests adjusting the dates or the number of guests.
3. Guest changes the search criteria or leaves the page.
4. Use case continues at step 3 or ends.

### A5: Guest Refines the Search

**Trigger:** Guest wants to look at other dates or a different party size after seeing the results (step 7)  
**Flow:**

1. Guest changes the arrival date, departure date, or number of guests.
2. Use case continues at step 4.

## Postconditions

### Success Postconditions

- Guest sees every room type with at least one room free for the whole stay and sufficient capacity, together with the total price
- The search criteria are retained so that Reserve Room Online (UC-002) can use them
- No reservation is created and no room is held; room availability remains unchanged

### Failure Postconditions

- No result list is shown; the guest sees a message explaining why the search could not be performed
- No reservation is created and room availability remains unchanged

## Business Rules

### BR-001: Minimum Stay of One Night

The departure date must be at least one day after the arrival date, so every stay covers at least one night.

### BR-002: No Searches for Past Dates

The arrival date must be today or later. Stays starting in the past cannot be searched or reserved.

### BR-003: Room Availability

A room is available for a stay when no reservation with a status other than Cancelled occupies it on any night from the arrival date up to, but not including, the departure date. A room whose previous guest departs on the arrival date counts as available.

### BR-004: Out-of-Service Rooms Are Not Offered

Rooms with the housekeeping state Out Of Service are excluded from search results regardless of the requested dates.

### BR-005: Capacity Must Fit the Party

Only room types whose capacity is greater than or equal to the number of guests are shown. The number of guests must be between 1 and the largest capacity among all room types.

### BR-006: Total Price Calculation

The total price of a stay is the room type's price per night multiplied by the number of nights and is shown in CHF.

### BR-007: Results Grouped by Room Type

Available rooms are presented per room type, not per individual room. The specific room is assigned when the reservation is made.
