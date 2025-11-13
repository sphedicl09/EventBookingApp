package com.eventbooking.eventbookingapp.controller;

import com.eventbooking.eventbookingapp.model.Event;
import com.eventbooking.eventbookingapp.model.Ticket;
import com.eventbooking.eventbookingapp.util.PDFGenerator;
import com.eventbooking.eventbookingapp.util.EmailSender;
import com.eventbooking.eventbookingapp.util.SupabaseService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingDialogController {

    @FXML
    private TextField eventNameField;

    @FXML
    private TextField attendeeNameField;

    @FXML
    private TextField emailField;

    @FXML
    private Spinner<Integer> ticketSpinner;

    private Event selectedEvent;

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        ticketSpinner.setValueFactory(valueFactory);
        confirmBookingButton.setDefaultButton(true);
    }

    @FXML
    private void onCancel() {
        ((Stage) eventNameField.getScene().getWindow()).close();
    }

    @FXML private Button confirmBookingButton;

    public void setSelectedEvent(Event event) {
        this.selectedEvent = event;
        eventNameField.setText(event.getName());
        eventNameField.setDisable(true);
        int remainingTickets = selectedEvent.getCapacity() - selectedEvent.getBookedCount();
        if (remainingTickets < 1) {
            remainingTickets = 1;
        }

        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, remainingTickets, 1);
        ticketSpinner.setValueFactory(valueFactory);
    }

    @FXML
    private void onConfirm() {
        String eventName = eventNameField.getText();
        String attendee = attendeeNameField.getText();
        String email = emailField.getText();
        int ticketsToBook = ticketSpinner.getValue();

        if (eventName.isEmpty() || attendee.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all fields.");
            return;
        }
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.ERROR, "Event Error", "Error: No event selected.");
            return;
        }

        int remainingCapacity = selectedEvent.getCapacity() - selectedEvent.getBookedCount();
        if (ticketsToBook > remainingCapacity) {
            showAlert(Alert.AlertType.ERROR, "Not Enough Tickets",
                    "You requested " + ticketsToBook + " tickets, but only " +
                            remainingCapacity + " are available.");
            return;
        }

        List<Ticket> newTickets = new ArrayList<>();
        for (int i = 0; i < ticketsToBook; i++) {
            String ticketId = UUID.randomUUID().toString();
            newTickets.add(new Ticket(eventName, attendee, email, ticketId));
        }

        try {
            for (Ticket ticket : newTickets) {
                String response = SupabaseService.saveTicket(
                        selectedEvent.getId(),
                        ticket.getAttendeeName(),
                        ticket.getEmail(),
                        ticket.getTicketId()
                );
                System.out.println("Saved ticket: " + ticket.getTicketId() + " | Response: " + response);
            }

            String pdfPath = PDFGenerator.generateTicketPDF(newTickets);

            String subject = (ticketsToBook > 1) ?
                    "Your " + ticketsToBook + " Tickets for " + eventName :
                    "Your Ticket for " + eventName;

            EmailSender.sendEmailWithAttachment(email, subject,
                    "Hi " + attendee + ",\n\nHere are your event ticket(s).",
                    pdfPath);

            showAlert(Alert.AlertType.INFORMATION, "Booking Successful",
                    "Booking complete! Your " + ticketsToBook + " ticket(s) have been emailed.");

            int newBookedCount = selectedEvent.getBookedCount() + ticketsToBook;
            selectedEvent.setBookedCount(newBookedCount);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Booking Failed", "Error: " + e.getMessage());
        }

        ((Stage) eventNameField.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}