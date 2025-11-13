package com.eventbooking.eventbookingapp.controller;

import com.eventbooking.eventbookingapp.model.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.format.DateTimeFormatter;

public class EventDetailsController {

    @FXML private ImageView posterView;
    @FXML private Label eventNameLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventCapacityLabel;
    @FXML private Label eventStatusLabel;
    @FXML private Label eventSynopsisLabel;

    public void loadEventData(Event event) {
        eventNameLabel.setText(event.getName());

        String date = "No date set";
        if (event.getEventDate() != null) {
            date = "Date: " + event.getEventDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
        }
        eventDateLabel.setText(date);

        eventCapacityLabel.setText("Booked: " + event.getBookedCount() + " / " + event.getCapacity());

        String status = event.isAcceptingBookings() ? "Open" : "Closed";
        if (event.isWaitlistEnabled()) {
            status += " (Waitlist Enabled)";
        }
        eventStatusLabel.setText("Status: " + status);

        String synopsis = event.getSynopsis();
        if (synopsis == null || synopsis.isEmpty()) {
            eventSynopsisLabel.setText("No synopsis provided for this event.");
        } else {
            eventSynopsisLabel.setText(synopsis);
        }

        String imageUrl = event.getPosterUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image poster = new Image(imageUrl, true);
                posterView.setImage(poster);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
            }
        }
    }
}