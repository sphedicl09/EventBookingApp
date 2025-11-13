package com.eventbooking.eventbookingapp.controller;

import com.eventbooking.eventbookingapp.util.SupabaseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import org.json.JSONArray;
import org.json.JSONObject;

public class AttendeeListDialogController {

    @FXML
    private Label eventNameLabel;

    @FXML
    private ListView<String> attendeeListView;

    private final ObservableList<String> attendees = FXCollections.observableArrayList();

    public void loadAttendees(String eventId, String eventName) {
        eventNameLabel.setText("Attendees for: " + eventName);

        ProgressIndicator p = new ProgressIndicator();
        p.setMaxSize(40, 40);
        attendeeListView.setPlaceholder(p);
        attendeeListView.setItems(attendees);

        Task<Void> fetchAttendeesTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String response = SupabaseService.fetchAttendeesForEvent(eventId);
                JSONArray jsonArray = new JSONArray(response);
                attendees.clear();

                if (jsonArray.length() == 0) {
                    javafx.application.Platform.runLater(() -> {
                        attendeeListView.setPlaceholder(new Label("No attendees have booked this event yet."));
                    });
                    return null;
                }

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String name = obj.getString("attendee_name");
                    String email = obj.getString("email");
                    attendees.add(name + " (" + email + ")");
                }
                return null;
            }
        };

        fetchAttendeesTask.setOnFailed(e -> {
            attendeeListView.setPlaceholder(new Label("Error: Could not load attendees."));
            fetchAttendeesTask.getException().printStackTrace();
        });

        new Thread(fetchAttendeesTask).start();
    }
}