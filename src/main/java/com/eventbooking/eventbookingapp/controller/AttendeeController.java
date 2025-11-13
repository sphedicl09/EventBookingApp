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
import javafx.scene.control.Tooltip;
import javafx.scene.control.ListCell;
import com.eventbooking.eventbookingapp.model.Event;

public class AttendeeController {

    @FXML
    private ListView<Event> eventList;

    public void initialize() {
        eventList.setItems(EventManager.getEvents());
        eventList.setCellFactory(lv -> new ListCell<Event>() {
            private Tooltip tooltip = new Tooltip();

            @Override
            public void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(event.toString());

                    String status = event.isAcceptingBookings() ? "Open" : "Closed";
                    String date = (event.getEventDate() != null) ?
                            event.getEventDate().toLocalDate().toString() : "No date set";

                    tooltip.setText(
                            "Event: " + event.getName() + "\n" +
                                    "Date: " + date + "\n" +
                                    "Booked: " + event.getBookedCount() + " / " + event.getCapacity() + "\n" +
                                    "Status: " + status
                    );
                    setTooltip(tooltip);
                }
            }
        });
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Event Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an event from the list.");
            alert.showAndWait();
            return;
        }

        if (!selectedEvent.isAcceptingBookings()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Booking Closed");
            alert.setHeaderText(null);
            alert.setContentText("Bookings for '" + selectedEvent.getName() + "' are currently closed.");
            alert.showAndWait();
            return;
        }

        if (selectedEvent.getBookedCount() >= selectedEvent.getCapacity()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Event Full");
            alert.setHeaderText(null);
            alert.setContentText("Sorry, '" + selectedEvent.getName() + "' is fully booked.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eventbooking/eventbookingapp/booking-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Book Event");
            dialogStage.setScene(new Scene(loader.load(), 400, 370));
            BookingDialogController controller = loader.getController();
            controller.setSelectedEvent(selectedEvent);
            dialogStage.showAndWait();
            eventList.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}