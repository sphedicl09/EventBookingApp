package com.eventbooking.eventbookingapp.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EventManager {
    private static final ObservableList<String> events = FXCollections.observableArrayList();

    public static ObservableList<String> getEvents() {
        return events;
    }

    public static void addEvent(String eventName) {
        if (eventName != null && !eventName.trim().isEmpty()) {
            events.add(eventName.trim());
        }
    }
}
