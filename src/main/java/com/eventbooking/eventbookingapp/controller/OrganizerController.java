package com.eventbooking.eventbookingapp.controller;

import com.eventbooking.eventbookingapp.model.Event;
import com.eventbooking.eventbookingapp.model.EventManager;
import com.eventbooking.eventbookingapp.util.SupabaseService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ListCell;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import com.eventbooking.eventbookingapp.util.ViewSwitcher;
import javafx.scene.input.MouseEvent;

public class OrganizerController {

    @FXML
    private ListView<Event> eventsListView;
    @FXML
    private Button createEventBtn;
    @FXML
    private MenuItem editEventMenuItem;
    @FXML
    private MenuItem deleteEventMenuItem;
    @FXML
    private MenuItem toggleWaitlistMenuItem;
    @FXML
    private MenuItem toggleBookingMenuItem;


    @FXML
    public void initialize() {
        Task<Void> loadEventsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                EventManager.loadEventsFromSupabase();
                return null;
            }
        };
        createEventBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PLUS));

        loadEventsTask.setOnSucceeded(e -> {
            eventsListView.setItems(EventManager.getEvents());

            eventsListView.setCellFactory(lv -> new ListCell<Event>() {
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
                                        "Status: " + status + "\n" +
                                        "Waitlist: " + (event.isWaitlistEnabled() ? "Enabled" : "Disabled")
                        );
                        setTooltip(tooltip);
                    }
                }
            });
        });
        new Thread(loadEventsTask).start();

        editEventMenuItem.setOnAction(e -> handleEditEvent());
        deleteEventMenuItem.setOnAction(e -> handleDeleteEvent());
        toggleWaitlistMenuItem.setOnAction(e -> handleToggleWaitlist());
        toggleBookingMenuItem.setOnAction(e -> handleToggleBooking());
        eventsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Event selected = eventsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEventDetailsWindow(selected);
                }
            }
        });
    }

    private void showEventDetailsWindow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eventbooking/eventbookingapp/event-details-view.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Event Details: " + event.getName());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(eventsListView.getScene().getWindow());

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/eventbooking/eventbookingapp/styles.css").toExternalForm());
            dialogStage.setScene(scene);

            EventDetailsController controller = loader.getController();
            controller.loadEventData(event);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Load Error", "Could not load the event details view.");
        }
    }

    private void handleDeleteEvent() {
        Event selectedEvent = eventsListView.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(AlertType.WARNING, "No Event Selected", "Please select an event to delete.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Event: " + selectedEvent.getName());
        confirm.setContentText("Are you sure? This action cannot be undone.");
        styleAlertSafe(confirm);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> deleteTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return SupabaseService.deleteEvent(selectedEvent.getId());
                }
            };

            deleteTask.setOnSucceeded(e -> {
                if (deleteTask.getValue()) {
                    EventManager.removeEvent(selectedEvent);
                    showAlert(AlertType.INFORMATION, "Success", "Event deleted successfully.");
                } else {
                    showAlert(AlertType.ERROR, "Delete Failed", "Could not delete the event from the database.");
                }
            });

            deleteTask.setOnFailed(e -> {
                showAlert(AlertType.ERROR, "Delete Failed", "An error occurred: " + deleteTask.getException().getMessage());
            });

            new Thread(deleteTask).start();
        }
    }

    private void handleEditEvent() {
        Event selectedEvent = eventsListView.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(AlertType.WARNING, "No Event Selected", "Please select an event to edit.");
            return;
        }
        openCreateEventDialog(null, selectedEvent);
    }

    @FXML
    public void openCreateEventDialog(ActionEvent event) {
        openCreateEventDialog(event, null);
    }

    private void openCreateEventDialog(ActionEvent event, Event eventToEdit) {
        boolean isEditMode = (eventToEdit != null);
        String dialogTitle = isEditMode ? "Edit Event" : "Create New Event";
        String buttonText = isEditMode ? "Update" : "Create";

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        Window owner = (event != null) ? getOwnerWindow(event) : eventsListView.getScene().getWindow();
        dialog.initOwner(owner);
        dialog.setTitle(dialogTitle);

        Label titleLabel = new Label(dialogTitle);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00bfff;");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter Event Name");

        TextField capacityField = new TextField();
        capacityField.setPromptText("Enter Seat Capacity");

        DatePicker eventDatePicker = new DatePicker();
        eventDatePicker.setPromptText("Select Event Date");
        eventDatePicker.setPrefWidth(150);

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 12);
        hourSpinner.setPrefWidth(70);

        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
        minuteSpinner.setPrefWidth(70);

        HBox timeBox = new HBox(10, new Label("Time (HH:mm):"), hourSpinner, minuteSpinner);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        TextField posterUrlField = new TextField();
        posterUrlField.setPromptText("Enter Poster Image URL (e.g., https://...)");

        TextArea synopsisArea = new TextArea();
        synopsisArea.setPromptText("Enter event synopsis/description...");
        synopsisArea.setWrapText(true);
        synopsisArea.setPrefHeight(100);

        if (isEditMode) {
            nameField.setText(eventToEdit.getName());
            capacityField.setText(String.valueOf(eventToEdit.getCapacity()));

            if (eventToEdit.getEventDate() != null) {
                eventDatePicker.setValue(eventToEdit.getEventDate().toLocalDate());
                hourSpinner.getValueFactory().setValue(eventToEdit.getEventDate().getHour());
                minuteSpinner.getValueFactory().setValue(eventToEdit.getEventDate().getMinute());
            }
            if (eventToEdit.getPosterUrl() != null) {
                posterUrlField.setText(eventToEdit.getPosterUrl());
            }
            if (eventToEdit.getSynopsis() != null) {
                synopsisArea.setText(eventToEdit.getSynopsis());
            }
        }

        Button confirmBtn = new Button(buttonText);
        confirmBtn.setStyle("-fx-background-color: #00bfff; -fx-text-fill: white; -fx-font-weight: bold;");

        confirmBtn.setOnAction(e -> {
            String eventName = nameField.getText();
            String capacityText = capacityField.getText();
            LocalDate date = eventDatePicker.getValue();
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            String posterUrl = posterUrlField.getText();
            String synopsis = synopsisArea.getText();

            if (date == null) {
                showAlert(AlertType.ERROR, "Invalid Input", "Please select a date for the event.", dialog);
                return;
            }

            try {
                int capacity = Integer.parseInt(capacityText);
                LocalDateTime eventDateTime = date.atTime(hour, minute);

                Task<Boolean> dbTask = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        if (isEditMode) {
                            return SupabaseService.updateEvent(eventToEdit.getId(), eventName, capacity, eventDateTime, posterUrl, synopsis);
                        } else {
                            return EventManager.addEvent(eventName, capacity, eventDateTime, posterUrl, synopsis);
                        }
                    }
                };

                dbTask.setOnSucceeded(taskResult -> {
                    if (dbTask.getValue()) {
                        if (isEditMode) {
                            EventManager.updateEvent(eventToEdit, eventName, capacity, eventDateTime, posterUrl, synopsis);
                            eventsListView.refresh();
                        }
                        dialog.close();
                    } else {
                        showAlert(AlertType.ERROR, "Database Error", "Could not save the event.");
                    }
                });

                dbTask.setOnFailed(taskFailure -> {
                    showAlert(AlertType.ERROR, "Database Error", "An error occurred: " + dbTask.getException().getMessage());
                });

                new Thread(dbTask).start();

            } catch (NumberFormatException ex) {
                showAlert(AlertType.ERROR, "Invalid Input", "Please enter a valid number for capacity.", dialog);
            }
        });

        VBox layout = new VBox(10, titleLabel, nameField, capacityField, eventDatePicker, timeBox, posterUrlField, synopsisArea, confirmBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 20;");

        Scene scene = new Scene(layout, 400, 450);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlertSafe(alert);
        alert.showAndWait();
    }

    private void showAlert(AlertType type, String title, String content, Window owner) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlertSafe(alert);
        alert.showAndWait();
    }

    @FXML
    public void openAttendeeListDialog(ActionEvent event) {
        Event selectedEvent = eventsListView.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(AlertType.WARNING, "No Event Selected", "Please select an event from the list to see its attendees.", getOwnerWindow(event));
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eventbooking/eventbookingapp/attendee-list-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Attendee List");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getOwnerWindow(event));
            dialogStage.setScene(new Scene(loader.load()));
            AttendeeListDialogController controller = loader.getController();
            controller.loadAttendees(selectedEvent.getId(), selectedEvent.getName());
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Load Error", "Could not load the attendee list view.", getOwnerWindow(event));
        }
    }

    public void switchToAttendee(ActionEvent event) throws IOException {
        ViewSwitcher.switchScene(event, "/com/eventbooking/eventbookingapp/attendee-view.fxml");
    }

    public void openCapacityWarningDialog(ActionEvent event) {
        showAlert(AlertType.WARNING, "Capacity Full", "Cannot Add More Attendees. The seat capacity for this event has been reached.", getOwnerWindow(event));
    }

    public void openEventEndingDialog(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initOwner(getOwnerWindow(event));
        alert.setTitle("Event Ending Soon");
        alert.setHeaderText("This event ends in 3 days!");
        alert.setContentText("Would you like to continue accepting bookings?");
        styleAlertSafe(alert);
        alert.showAndWait();
    }

    private Window getOwnerWindow(ActionEvent event) {
        return ((javafx.scene.Node) event.getSource()).getScene().getWindow();
    }

    private void styleAlertSafe(Alert alert) {
        alert.getDialogPane().setStyle(
                "-fx-background-color: #2b2b2b; " +
                        "-fx-font-size: 14px; " +
                        "-fx-text-fill: white;"
        );
        try {
            for (ButtonType bt : alert.getButtonTypes()) {
                Button b = (Button) alert.getDialogPane().lookupButton(bt);
                if (b != null) {
                    b.setStyle("-fx-background-color: #00bfff; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            }
        } catch (Exception e) {

        }
    }

    private void handleToggleWaitlist() {
        Event selectedEvent = eventsListView.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) return;

        boolean newWaitlistStatus = !selectedEvent.isWaitlistEnabled();

        Task<Boolean> updateTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return SupabaseService.updateEventFlags(
                        selectedEvent.getId(),
                        newWaitlistStatus,
                        selectedEvent.isAcceptingBookings()
                );
            }
        };

        updateTask.setOnSucceeded(e -> {
            if (updateTask.getValue()) {
                selectedEvent.setWaitlistEnabled(newWaitlistStatus);
                eventsListView.refresh();
                showAlert(AlertType.INFORMATION, "Success", "Waitlist " + (newWaitlistStatus ? "enabled." : "disabled."));
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "Could not update waitlist status.");
            }
        });

        new Thread(updateTask).start();
    }

    private void handleToggleBooking() {
        Event selectedEvent = eventsListView.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) return;

        boolean newBookingStatus = !selectedEvent.isAcceptingBookings();

        Task<Boolean> updateTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return SupabaseService.updateEventFlags(
                        selectedEvent.getId(),
                        selectedEvent.isWaitlistEnabled(),
                        newBookingStatus
                );
            }
        };

        updateTask.setOnSucceeded(e -> {
            if (updateTask.getValue()) {
                selectedEvent.setAcceptingBookings(newBookingStatus);
                eventsListView.refresh();
                showAlert(AlertType.INFORMATION, "Success", "Bookings " + (newBookingStatus ? "opened." : "closed."));
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "Could not update booking status.");
            }
        });

        new Thread(updateTask).start();
    }
}