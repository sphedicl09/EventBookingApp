package com.eventbooking.eventbookingapp.controller;

import com.eventbooking.eventbookingapp.model.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class EventCardController {

    @FXML private VBox eventCard;
    @FXML private ImageView posterImageView;
    @FXML private Label eventNameLabel;
    @FXML private Button bookButton;

    private Event event;

    public void setData(Event event) {
        this.event = event;
        eventNameLabel.setText(event.getName());

        if (event.getPosterUrl() != null && !event.getPosterUrl().isBlank()) {
            try {
                Image poster = new Image(event.getPosterUrl(), true);
                posterImageView.setImage(poster);
            } catch (Exception e) {
                System.out.println("Failed to load image from URL: " + event.getPosterUrl());
            }
        } else {

        }

        if (event.getBookedCount() >= event.getCapacity()) {
            bookButton.setText("Sold Out");
            bookButton.setDisable(true);
        } else if (!event.isAcceptingBookings()) {
            bookButton.setText("Closed");
            bookButton.setDisable(true);
        }
    }

    @FXML
    private void handleBookNow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eventbooking/eventbookingapp/booking-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Book Event");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(bookButton.getScene().getWindow());

            Pane root = loader.load();
            Scene scene = new Scene(root, 400, 370);
            scene.getStylesheets().add(getClass().getResource("/com/eventbooking/eventbookingapp/styles.css").toExternalForm());
            dialogStage.setScene(scene);

            BookingDialogController controller = loader.getController();
            controller.setSelectedEvent(this.event);

            dialogStage.showAndWait();

            setData(this.event);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}