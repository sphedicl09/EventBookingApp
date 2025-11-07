package com.eventbooking.eventbookingapp.controller;
import com.eventbooking.eventbookingapp.model.Event;
import com.eventbooking.eventbookingapp.model.EventManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class AttendeeController {

    @FXML
    private ListView<Event> eventList;

    public void initialize() {
        eventList.setItems(EventManager.getEvents());
    }

    public void switchToOrganizer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eventbooking/eventbookingapp/organizer-view.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load(), 800, 600));
    }

    @FXML
    private void openBookingDialog() {
        Event selectedEvent = eventList.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an event from the list.").showAndWait();
            return; // Stop the method
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eventbooking/eventbookingapp/booking-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Book Event");
            dialogStage.setScene(new Scene(loader.load(), 400, 300));

            BookingDialogController controller = loader.getController();
            controller.setSelectedEvent(selectedEvent);

            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}