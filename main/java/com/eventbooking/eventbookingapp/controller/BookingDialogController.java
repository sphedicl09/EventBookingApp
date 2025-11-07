package com.eventbooking.eventbookingapp.controller;
import com.eventbooking.eventbookingapp.model.Event;
import com.eventbooking.eventbookingapp.model.Ticket;
import com.eventbooking.eventbookingapp.util.PDFGenerator;
import com.eventbooking.eventbookingapp.util.EmailSender;
import com.eventbooking.eventbookingapp.util.SupabaseService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.UUID;

public class BookingDialogController {

    @FXML private TextField eventNameField;
    @FXML private TextField attendeeNameField;
    @FXML private TextField emailField;

    private Event selectedEvent;

    @FXML
    private void onCancel() {
        ((Stage) eventNameField.getScene().getWindow()).close();
    }

    @FXML
    public void setSelectedEvent(Event event) {
        this.selectedEvent = event;
        eventNameField.setText(event.getName());
        eventNameField.setDisable(true);
    }

    @FXML
    private void onConfirm() {
        String eventName = eventNameField.getText();
        String attendee = attendeeNameField.getText();
        String email = emailField.getText();

        if (eventName.isEmpty() || attendee.isEmpty() || email.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please fill in all fields.").showAndWait();
            return;
        }

        if (selectedEvent == null) {
            new Alert(Alert.AlertType.ERROR, "Error: No event selected. Please re-open from the event list.").showAndWait();
            return;
        }

        Ticket ticket = new Ticket(eventName, attendee, email, UUID.randomUUID().toString());

        try {
            String pdfPath = PDFGenerator.generateTicketPDF(ticket);
            EmailSender.sendEmailWithAttachment(email,
                    "Your Ticket for " + eventName,
                    "Hi " + attendee + ",\n\nHere is your event ticket.",
                    pdfPath);

            new Alert(Alert.AlertType.INFORMATION, "Booking successful! Ticket PDF emailed.").showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error generating ticket/email: " + e.getMessage()).showAndWait();
        }

        try {
            String response = SupabaseService.saveTicket(
                    selectedEvent.getId(),
                    attendee,
                    email,
                    ticket.getTicketId()
            );
            System.out.println("Ticket upload response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }


        ((Stage) eventNameField.getScene().getWindow()).close();
    }


}
