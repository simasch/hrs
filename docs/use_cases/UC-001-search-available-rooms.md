# Use Case: Search Available Rooms

## Overview

**Use Case ID:** UC-001
**Use Case Name:** Search Available Rooms
**Primary Actor:** Guest
**Goal:** Find rooms that are available for a desired stay so the guest can decide whether to book.
**Status:** Draft

## Preconditions

- The hotel's public booking website is reachable.
- The room catalog and availability data are loaded in the system.

## Main Success Scenario

1. Guest opens the public booking page.
2. System displays the search form with check-in date, check-out date, and number of guests.
3. Guest enters the check-in date, check-out date, and number of guests.
4. Guest submits the search.
5. System validates that the check-in date is today or later and that the check-out date is after the check-in date.
6. System retrieves all rooms whose capacity is greater than or equal to the requested number of guests and that have no overlapping reservation for the requested date range.
7. System displays the matching rooms with room type, capacity, nightly rate, total price for the stay, and an availability indicator.
8. Guest reviews the results.

## Alternative Flows

### A1: Invalid Date Range

**Trigger:** Check-in date is in the past or check-out date is not after check-in date (step 5).
**Flow:**

1. System displays a validation error explaining the date rule.
2. System keeps the entered values in the form.
3. Use case continues at step 3.

### A2: No Rooms Available

**Trigger:** No rooms match the requested capacity and date range (step 6).
**Flow:**

1. System displays a "No rooms available for the selected dates" message.
2. System suggests adjusting the dates or guest count.
3. Use case continues at step 3 or ends.

### A3: Stay Exceeds Maximum Length

**Trigger:** Requested stay is longer than the maximum allowed (step 5).
**Flow:**

1. System displays an error stating the maximum stay length.
2. Use case continues at step 3.

### A4: Refine Search

**Trigger:** Guest changes search criteria after viewing results (step 8).
**Flow:**

1. Guest updates dates or guest count and submits again.
2. Use case continues at step 5.

## Postconditions

### Success Postconditions

- The system has displayed the list of available rooms for the requested dates.
- No data is changed; no reservation is created.
- The search criteria are retained so the guest can proceed to UC-002 Reserve Room.

### Failure Postconditions

- No search results are displayed.
- An error or empty-result message is visible to the guest.
- No data is changed.

## Business Rules

### BR-001: Check-in Date Not in Past

The check-in date must be today or a future date.

### BR-002: Check-out After Check-in

The check-out date must be strictly after the check-in date. The minimum stay is one night.

### BR-003: Maximum Advance Booking

Searches for stays starting more than 365 days in the future are not allowed.

### BR-004: Maximum Stay Length

A single reservation may not exceed 30 nights.

### BR-005: Capacity Match

A room is considered available only if its capacity is greater than or equal to the requested number of guests.

### BR-006: No Overlap with Existing Reservations

A room is considered available for a date range only if it has no confirmed or checked-in reservation overlapping that range.
