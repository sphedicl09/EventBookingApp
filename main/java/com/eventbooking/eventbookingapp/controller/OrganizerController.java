package com.eventbooking.eventbookingapp.controller;

import com.eventbooking.eventbookingapp.model.EventManager;
import com.eventbooking.eventbookingapp.model.Event;
import com.eventbooking.eventbookingapp.util.SupabaseService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

public class OrganizerController {

    @FXML
    private ListView<Event> eventsListView;

    @FXML
    private Label selectedEventDetails;

    @FXML
    public void initialize() {
        EventManager.loadEventsFromSupabase();

        // Bind the ListView to the EventManager's observable events list
        eventsListView.setItems(EventManager.getEvents());

        // Use a custom cell factory to show inline counts and status
        eventsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getBookedCount() + "/" + item.getCapacity() + ")"
                            + (item.isAcceptingBookings() ? "" : " [Closed]")
                            + (item.isWaitlistEnabled() ? " [Waitlist]" : ""));
                }
            }
        });

        // Add context menu to list view items
        eventsListView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                Event selected = eventsListView.getSelectionModel().getSelectedItem();
                if (selected != null) openEventEndingDialog(new ActionEvent(eventsListView, null));
            }
        });

        ContextMenu cm = new ContextMenu();
        MenuItem notifyItem = new MenuItem("Notify Attendees");
        MenuItem toggleWaitlist = new MenuItem("Toggle Waitlist");
        MenuItem toggleBookings = new MenuItem("Toggle Bookings Open/Close");
        MenuItem refreshCounts = new MenuItem("Refresh Counts");

        notifyItem.setOnAction(e -> {
            Event sel = eventsListView.getSelectionModel().getSelectedItem();
            if (sel != null) notifyAttendees(sel);
        });

        toggleWaitlist.setOnAction(e -> {
            Event sel = eventsListView.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            boolean newVal = !sel.isWaitlistEnabled();
            // run update in background
            Task<Boolean> t = new Task<>() {
                @Override
                protected Boolean call() {
                    return SupabaseService.updateEventFlags(sel.getId(), newVal, sel.isAcceptingBookings());
                }
            };
            t.setOnSucceeded(ev -> {
                boolean ok = t.getValue();
                if (ok) {
                    sel.setWaitlistEnabled(newVal);
                    eventsListView.refresh();
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.initOwner(getOwnerWindow(new ActionEvent(eventsListView, null)));
                    info.setTitle("Waitlist Updated");
                    info.setHeaderText(null);
                    info.setContentText("Waitlist " + (newVal ? "enabled" : "disabled") + " for " + sel.getName());
                    styleAlertSafe(info);
                    info.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.initOwner(getOwnerWindow(new ActionEvent(eventsListView, null)));
                    err.setTitle("Update Failed");
                    err.setHeaderText(null);
                    err.setContentText("Failed to update waitlist status for " + sel.getName());
                    styleAlertSafe(err);
                    err.showAndWait();
                }
            });
            new Thread(t).start();
        });

        toggleBookings.setOnAction(e -> {
            Event sel = eventsListView.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            boolean newVal = !sel.isAcceptingBookings();
            Task<Boolean> t = new Task<>() {
                @Override
                protected Boolean call() {
                    return SupabaseService.updateEventFlags(sel.getId(), sel.isWaitlistEnabled(), newVal);
                }
            };
            t.setOnSucceeded(ev -> {
                boolean ok = t.getValue();
                if (ok) {
                    sel.setAcceptingBookings(newVal);
                    eventsListView.refresh();
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.initOwner(getOwnerWindow(new ActionEvent(eventsListView, null)));
                    info.setTitle("Bookings Updated");
                    info.setHeaderText(null);
                    info.setContentText("Bookings " + (newVal ? "opened" : "closed") + " for " + sel.getName());
                    styleAlertSafe(info);
                    info.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.initOwner(getOwnerWindow(new ActionEvent(eventsListView, null)));
                    err.setTitle("Update Failed");
                    err.setHeaderText(null);
                    err.setContentText("Failed to update bookings flag for " + sel.getName());
                    styleAlertSafe(err);
                    err.showAndWait();
                }
            });
            new Thread(t).start();
        });

        refreshCounts.setOnAction(e -> {
            Event sel = eventsListView.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            Task<Void> t = new Task<>() {
                @Override
                protected Void call() {
                    EventManager.refreshEventCount(sel);
                    return null;
                }
            };
            t.setOnSucceeded(ev -> eventsListView.refresh());
            new Thread(t).start();
        });

        cm.getItems().addAll(notifyItem, toggleWaitlist, toggleBookings, refreshCounts);
        eventsListView.setContextMenu(cm);

        // Update the details label when selection changes
        eventsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedEventDetails.setText("Selected: " + newV.getName() + " (Capacity: " + newV.getCapacity() + ")");
            } else {
                selectedEventDetails.setText("Select an event to see details");
            }
        });

        // Select the first event if present
        if (!EventManager.getEvents().isEmpty()) {
            eventsListView.getSelectionModel().selectFirst();
        }

        // Kick off a background refresh of all counts
        Task<Void> refreshAll = new Task<>() {
            @Override
            protected Void call() {
                EventManager.refreshAllCounts();
                return null;
            }
        };
        refreshAll.setOnSucceeded(ev -> eventsListView.refresh());
        new Thread(refreshAll).start();
    }

    @FXML
    public void switchToAttendee(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eventbooking/eventbookingapp/attendee-view.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load(), 800, 600));
    }

    @FXML
    public void openCreateEventDialog(ActionEvent event) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(getOwnerWindow(event));
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
            String capacityText = capacityField.getText();

            try {
                int capacity = Integer.parseInt(capacityText);
                EventManager.addEvent(eventName, capacity);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Event Created");
                alert.setHeaderText(null);
                alert.setContentText("Event '" + eventName + "' created successfully!");
                styleAlertSafe(alert);
                alert.initOwner(dialog);
                alert.showAndWait();
                dialog.close();

            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid number for capacity.");
                styleAlertSafe(alert);
                alert.initOwner(dialog);
                alert.showAndWait();
            }
        });

        VBox layout = new VBox(10, title, nameField, capacityField, createBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 20;");

        Scene scene = new Scene(layout, 300, 220);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    public void openCapacityWarningDialog(ActionEvent event) {
        Event selected = eventsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("No Event Selected");
            a.setHeaderText(null);
            a.setContentText("Please select an event first.");
            styleAlertSafe(a);
            a.initOwner(getOwnerWindow(event));
            a.showAndWait();
            return;
        }

        Window owner = getOwnerWindow(event);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(owner);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Capacity Full: " + selected.getName());
        alert.setHeaderText("Cannot Add More Attendees");
        alert.setContentText("The seat capacity for this event has been reached. Would you like to notify attendees or enable a waitlist?");
        styleAlertSafe(alert);

        ButtonType notify = new ButtonType("Notify Attendees");
        ButtonType waitlist = new ButtonType("Enable Waitlist");
        ButtonType cancel = ButtonType.CANCEL;

        alert.getButtonTypes().setAll(notify, waitlist, cancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == notify) {
            notifyAttendees(selected);
        } else if (result.isPresent() && result.get() == waitlist) {
            // Update the waitlist_enabled flag in Supabase in background
            Task<Boolean> t = new Task<>() {
                @Override
                protected Boolean call() {
                    return SupabaseService.updateEventFlags(selected.getId(), true, selected.isAcceptingBookings());
                }
            };
            t.setOnSucceeded(ev -> {
                boolean ok = t.getValue();
                if (ok) {
                    selected.setWaitlistEnabled(true);
                    eventsListView.refresh();
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.initOwner(owner);
                    info.setTitle("Waitlist Enabled");
                    info.setHeaderText(null);
                    info.setContentText("Waitlist enabled for " + selected.getName());
                    styleAlertSafe(info);
                    info.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.initOwner(owner);
                    err.setTitle("Update Failed");
                    err.setHeaderText(null);
                    err.setContentText("Failed to enable waitlist for " + selected.getName());
                    styleAlertSafe(err);
                    err.showAndWait();
                }
            });
            new Thread(t).start();
        }
    }

    @FXML
    public void openEventEndingDialog(ActionEvent event) {
        Event selected = eventsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("No Event Selected");
            a.setHeaderText(null);
            a.setContentText("Please select an event first.");
            styleAlertSafe(a);
            a.initOwner(getOwnerWindow(event));
            a.showAndWait();
            return;
        }

        Window owner = getOwnerWindow(event);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Event Ending Soon: " + selected.getName());
        alert.setHeaderText("This event ends in 3 days!");
        alert.setContentText("Would you like to continue accepting bookings or notify attendees that it's ending?");
        styleAlertSafe(alert);

        ButtonType continueBookings = new ButtonType("Continue Bookings");
        ButtonType notifyAndClose = new ButtonType("Notify & Close");
        ButtonType cancel = ButtonType.CANCEL;

        alert.getButtonTypes().setAll(continueBookings, notifyAndClose, cancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == continueBookings) {
            // Ensure accepting_bookings = true
            Task<Boolean> t = new Task<>() {
                @Override
                protected Boolean call() {
                    return SupabaseService.updateEventFlags(selected.getId(), selected.isWaitlistEnabled(), true);
                }
            };
            t.setOnSucceeded(ev -> {
                if (t.getValue()) {
                    selected.setAcceptingBookings(true);
                    eventsListView.refresh();
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.initOwner(owner);
                    info.setTitle("Continuing Bookings");
                    info.setHeaderText(null);
                    info.setContentText("This event will continue accepting bookings.");
                    styleAlertSafe(info);
                    info.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.initOwner(owner);
                    err.setTitle("Update Failed");
                    err.setHeaderText(null);
                    err.setContentText("Failed to update booking status for " + selected.getName());
                    styleAlertSafe(err);
                    err.showAndWait();
                }
            });
            new Thread(t).start();

        } else if (result.isPresent() && result.get() == notifyAndClose) {
            // set accepting_bookings = false and notify
            Task<Boolean> t = new Task<>() {
                @Override
                protected Boolean call() {
                    return SupabaseService.updateEventFlags(selected.getId(), selected.isWaitlistEnabled(), false);
                }
            };
            t.setOnSucceeded(ev -> {
                if (t.getValue()) {
                    selected.setAcceptingBookings(false);
                    eventsListView.refresh();
                    notifyAttendees(selected);
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.initOwner(owner);
                    info.setTitle("Event Closed for Bookings");
                    info.setHeaderText(null);
                    info.setContentText("Attendees notified and bookings closed for " + selected.getName());
                    styleAlertSafe(info);
                    info.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.initOwner(owner);
                    err.setTitle("Update Failed");
                    err.setHeaderText(null);
                    err.setContentText("Failed to close bookings for " + selected.getName());
                    styleAlertSafe(err);
                    err.showAndWait();
                }
            });
            new Thread(t).start();
        }
    }

    private void notifyAttendees(Event event) {
        // Placeholder implementation: log and show an info alert. Real implementation would fetch attendee emails
        System.out.println("Notifying attendees for event: " + event.getName());
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Notifying Attendees");
        info.setHeaderText(null);
        info.setContentText("(Simulated) Sending notifications to attendees for " + event.getName());
        styleAlertSafe(info);
        info.showAndWait();

        // Example: if we had attendee emails we could call EmailSender.sendEmailWithAttachment in a background thread
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

        for (ButtonType bt : alert.getButtonTypes()) {
            Button b = (Button) alert.getDialogPane().lookupButton(bt);
            if (b != null) {
                b.setStyle("-fx-background-color: #00bfff; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        }
    }
}
