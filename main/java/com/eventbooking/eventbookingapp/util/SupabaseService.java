package com.eventbooking.eventbookingapp.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SupabaseService {
    private static final String SUPABASE_URL = System.getenv("SUPABASE_URL");
    private static final String API_KEY = System.getenv("SUPABASE_KEY");
    private static String format;

    private static HttpURLConnection setupConnection(String endpoint, String method) throws IOException {
        URL url = new URL(SUPABASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("apikey", API_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoInput(true);
        if (method.equals("POST") || method.equals("PATCH"))
            conn.setDoOutput(true);
        return conn;
    }

    public static String fetchEvents() {
        try {
            HttpURLConnection conn = setupConnection("events?select=*", "GET");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                return br.lines().reduce("", (acc, line) -> acc + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public static String saveEvent(String name, int capacity) {
        String json = String.format(
                "{\"name\":\"%s\", \"capacity\":%d}",
                name, capacity
        );

        try {
            HttpURLConnection conn = setupConnection("events?select=*", "POST");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 201) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    return br.lines().reduce("", (acc, line) -> acc + line);
                }
            } else {
                return "Error: " + code;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public static String saveTicket(String eventId, String name, String email, String ticketCode) {
        String json = format;

        try {
            HttpURLConnection conn = setupConnection("tickets", "POST");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 201 || code == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    return br.lines().reduce("", (acc, line) -> acc + line);
                }
            } else {
                return "Error: " + code;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // New: fetch the ticket count for a given event id
    public static int fetchEventTicketCount(String eventId) {
        try {
            // Use PostgREST filter to count related tickets: tickets?select=*&event_id=eq.<id>
            HttpURLConnection conn = setupConnection("tickets?select=id&event_id=eq." + eventId, "GET");
            int code = conn.getResponseCode();
            if (code != 200) return 0;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String resp = br.lines().reduce("", (a, b) -> a + b);
                if (resp == null || resp.trim().isEmpty() || resp.equals("[]")) return 0;
                // Count occurrences of '{' as a cheap way since response is an array of objects; better to parse JSON but avoid new deps
                long count = resp.chars().filter(ch -> ch == '{').count();
                return (int) count;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // New: update event flags via PATCH to events?id=eq.<id>
    public static boolean updateEventFlags(String eventId, boolean waitlistEnabled, boolean acceptingBookings) {
        String json = String.format("{\"waitlist_enabled\":%s,\"accepting_bookings\":%s}", waitlistEnabled ? "true" : "false", acceptingBookings ? "true" : "false");
        try {
            HttpURLConnection conn = setupConnection("events?id=eq." + eventId, "PATCH");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            return code == 200 || code == 204;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
