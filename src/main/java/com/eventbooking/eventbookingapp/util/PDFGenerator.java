package com.eventbooking.eventbookingapp.util;

import com.eventbooking.eventbookingapp.model.Ticket;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFGenerator {

    public static String generateTicketPDF(List<Ticket> tickets) throws Exception {
        if (tickets == null || tickets.isEmpty()) {
            throw new IllegalArgumentException("Ticket list cannot be empty.");
        }

        Ticket firstTicket = tickets.get(0);
        Path outputDir = Path.of(System.getProperty("user.home"), "EventTickets");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        String filename = firstTicket.getAttendeeName().replaceAll("\\s+", "_")
                + "_" + firstTicket.getEventName().replaceAll("\\s+", "_")
                + "_Tickets.pdf";
        Path outputFile = outputDir.resolve(filename);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputFile.toFile()));
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
        String titleText = (tickets.size() > 1) ?
                "🎫 Your " + tickets.size() + " Event Tickets" :
                "🎫 Event Ticket";
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        int ticketNum = 1;
        for (Ticket ticket : tickets) {

            Font stubTitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph stubTitle = new Paragraph("Ticket " + ticketNum + " of " + tickets.size() +
                    " (" + ticket.getEventName() + ")", stubTitleFont);
            stubTitle.setSpacingBefore(10f);
            document.add(stubTitle);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addRow(table, "Attendee:", ticket.getAttendeeName());
            addRow(table, "Email:", ticket.getEmail());
            addRow(table, "Ticket ID:", ticket.getTicketId());
            addRow(table, "Issued On:", LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            document.add(table);

            String qrCodePath = QRCodeGenerator.generateQRCode(
                    ticket.getTicketId(),
                    "ticket_qr_" + ticket.getTicketId()
            );

            Image qrImage = Image.getInstance(qrCodePath);
            qrImage.scaleToFit(100, 100);
            qrImage.setAlignment(Element.ALIGN_CENTER);

            document.add(qrImage);

            new File(qrCodePath).delete();

            if (ticketNum < tickets.size()) {
                Paragraph separator = new Paragraph("------------------------------------------------------");
                separator.setAlignment(Element.ALIGN_CENTER);
                document.add(separator);
            }

            ticketNum++;
        }

        document.add(Chunk.NEWLINE);
        Paragraph note = new Paragraph("Please present this ticket upon entry.",
                new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC));
        note.setAlignment(Element.ALIGN_CENTER);
        document.add(note);

        document.close();

        System.out.println("Multi-ticket PDF generated: " + outputFile.toAbsolutePath());
        return outputFile.toString();
    }

    private static void addRow(PdfPTable table, String key, String value) {
        PdfPCell cellKey = new PdfPCell(new Phrase(key));
        PdfPCell cellValue = new PdfPCell(new Phrase(value));
        cellKey.setBorder(Rectangle.NO_BORDER);
        cellValue.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellKey);
        table.addCell(cellValue);
    }
}