package com.eventbooking.eventbookingapp.controller;

import com.eventbooking.eventbookingapp.model.EventManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class OrganizerController {

    public void switchToAttendee(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eventbooking/eventbookingapp/attendee-view.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load(), 800, 600));
    }

    public void openCreateEventDialog(ActionEvent event) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create New Event");

        Label title = new Label("Create a New Event");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00bfff;");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter Event Name");

        TextField capacityField = new TextField();
        capacityField.setPromptText("Enter Seat Capacity");

        Button createBtn = new Button("Create");
        createBtn.setStyle("-fx-background-color: #00bfff; -fx-text-fill: white; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> {
            String eventName = nameField.getText();
            EventManager.addEvent(eventName);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Event Created");
            alert.setHeaderText(null);
            alert.setContentText("Event '" + eventName + "' created successfully!");
            styleAlertSafe(alert);
            alert.showAndWait();
            dialog.close();
        });

        VBox layout = new VBox(10, title, nameField, capacityField, createBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 20;");

        Scene scene = new Scene(layout, 300, 220);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public void openCapacityWarningDialog(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Capacity Full");
        alert.setHeaderText("Cannot Add More Attendees");
        alert.setContentText("The seat capacity for this event has been reached.");
        styleAlertSafe(alert);
        alert.showAndWait();
    }

    public void openEventEndingDialog(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Event Ending Soon");
        alert.setHeaderText("This event ends in 3 days!");
        alert.setContentText("Would you like to continue accepting bookings?");
        styleAlertSafe(alert);
        alert.showAndWait();
    }

    private void styleAlertSafe(Alert alert) {
        alert.getDialogPane().setStyle(
                "-fx-background-color: #2b2b2b; " +
                        "-fx-font-size: 14px; " +
                        "-fx-text-fill: white;"
        );

        for (ButtonType bt : alert.getButtonTypes()) {
            Button b = (Button) alert.getDialogPane().lookupButton(bt);
            b.setStyle("-fx-background-color: #00bfff; -fx-text-fill: white; -fx-font-weight: bold;");
        }
    }
}
