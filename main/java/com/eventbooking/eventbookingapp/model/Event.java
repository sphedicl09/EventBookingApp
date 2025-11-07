package com.eventbooking.eventbookingapp.model;

public class Event {
    private final String id;
    private final String name;
    private final int capacity;

    // New mutable state kept locally for UI convenience
    private int bookedCount = 0;
    private boolean waitlistEnabled = false;
    private boolean acceptingBookings = true;

    public Event(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    // New constructor used when loading flags from backend
    public Event(String id, String name, int capacity, int bookedCount, boolean waitlistEnabled, boolean acceptingBookings) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.bookedCount = bookedCount;
        this.waitlistEnabled = waitlistEnabled;
        this.acceptingBookings = acceptingBookings;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }

    public int getBookedCount() { return bookedCount; }
    public void setBookedCount(int bookedCount) { this.bookedCount = bookedCount; }

    public boolean isWaitlistEnabled() { return waitlistEnabled; }
    public void setWaitlistEnabled(boolean waitlistEnabled) { this.waitlistEnabled = waitlistEnabled; }

    public boolean isAcceptingBookings() { return acceptingBookings; }
    public void setAcceptingBookings(boolean acceptingBookings) { this.acceptingBookings = acceptingBookings; }

    @Override
    public String toString() {
        return name + " (" + bookedCount + "/" + capacity + ")" + (acceptingBookings ? "" : " [Closed]") + (waitlistEnabled ? " [Waitlist]" : "");
    }
}