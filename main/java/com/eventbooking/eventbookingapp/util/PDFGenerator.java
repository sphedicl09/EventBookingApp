package com.eventbooking.eventbookingapp.util;

import com.eventbooking.eventbookingapp.model.Ticket;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {

    public static String generateTicketPDF(Ticket ticket) throws Exception {
        Path outputDir = Path.of(System.getProperty("user.home"), "EventTickets");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        String filename = ticket.getAttendeeName().replaceAll("\\s+", "_")
                + "_" + ticket.getEventName().replaceAll("\\s+", "_")
                + "_Ticket.pdf";
        Path outputFile = outputDir.resolve(filename);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputFile.toFile()));
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
        Paragraph title = new Paragraph("🎫 Event Ticket", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        addRow(table, "Event Name:", ticket.getEventName());
        addRow(table, "Attendee Name:", ticket.getAttendeeName());
        addRow(table, "Email:", ticket.getEmail());
        addRow(table, "Ticket ID:", ticket.getTicketId());
        addRow(table, "Issued On:", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        document.add(table);
        document.add(Chunk.NEWLINE);

        Paragraph note = new Paragraph("Please present this ticket upon entry.", new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC));
        note.setAlignment(Element.ALIGN_CENTER);
        document.add(note);

        document.close();

        System.out.println("Ticket PDF generated: " + outputFile.toAbsolutePath());

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
