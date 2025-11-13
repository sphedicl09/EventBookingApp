package com.eventbooking.eventbookingapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event {
    private final String id;
    private String name;
    private int capacity;
    private LocalDateTime eventDate;
    private int bookedCount = 0;
    private boolean waitlistEnabled = false;
    private boolean acceptingBookings = true;

    public Event(String id, String name, int capacity, int bookedCount, boolean waitlistEnabled, boolean acceptingBookings, LocalDateTime eventDate) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.bookedCount = bookedCount;
        this.waitlistEnabled = waitlistEnabled;
        this.acceptingBookings = acceptingBookings;
        this.eventDate = eventDate;
    }


    //Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public int getBookedCount() { return bookedCount; }
    public boolean isWaitlistEnabled() { return waitlistEnabled; }
    public boolean isAcceptingBookings() { return acceptingBookings; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }

    //Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setBookedCount(int bookedCount) {
        this.bookedCount = bookedCount;
    }

    public void setWaitlistEnabled(boolean waitlistEnabled) {
        this.waitlistEnabled = waitlistEnabled;
    }

    public void setAcceptingBookings(boolean acceptingBookings) {
        this.acceptingBookings = acceptingBookings;
    }


    @Override
    public String toString() {
        String dateStr = "";
        if (eventDate != null) {
            dateStr = " on " + eventDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
        return name + " (" + bookedCount + "/" + capacity + ")" + dateStr
                + (acceptingBookings ? "" : " [Closed]")
                + (waitlistEnabled ? " [Waitlist]" : "");
    }
}