package com.eventbooking.eventbookingapp.model;

public class Ticket {
    private final String eventName;
    private final String attendeeName;
    private final String email;
    private final String ticketId;

    public Ticket(String eventName, String attendeeName, String email, String ticketId) {
        this.eventName = eventName;
        this.attendeeName = attendeeName;
        this.email = email;
        this.ticketId = ticketId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public String getEmail() {
        return email;
    }

    public String getTicketId() {
        return ticketId;
    }

    @Override
    public String toString() {
        return eventName + " - " + attendeeName + " (" + ticketId + ")";
    }
}
