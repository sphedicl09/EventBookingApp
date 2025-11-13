package com.eventbooking.eventbookingapp.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

public class SupabaseService {
    private static final String SUPABASE_URL = System.getenv("SUPABASE_URL");
    private static final String API_KEY = System.getenv("SUPABASE_KEY");
    private static final HttpClient client = HttpClient.newHttpClient();

    private static HttpRequest.Builder createRequestBuilder(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + endpoint))
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    public static String fetchEvents(String endpoint) {
        try {
            HttpRequest request = createRequestBuilder(endpoint)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public static String saveEvent(String name, int capacity, LocalDateTime eventDate) {
        String json = "{\"name\":\"" + name + "\", \"capacity\":" + capacity +
                ", \"event_date\":\"" + eventDate.toString() + "\"}";

        try {
            HttpRequest request = createRequestBuilder("events?select=*")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) { // 201 Created
                return response.body();
            } else {
                return "Error: " + response.statusCode();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public static String saveTicket(String eventId, String name, String email, String ticketCode) {
        String json = String.format(
                "{\"events_id\":\"%s\", \"attendee_name\":\"%s\", \"email\":\"%s\", \"ticket_code\":\"%s\"}",
                eventId, name, email, ticketCode
        );

        try {
            HttpRequest request = createRequestBuilder("tickets")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return response.body();
            } else {
                return "Error: " + response.statusCode();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public static String fetchAttendeesForEvent(String eventId) {
        try {
            String endpoint = "tickets?select=attendee_name,email&events_id=eq." + eventId;
            HttpRequest request = createRequestBuilder(endpoint)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.out.println("Error fetching attendees: " + response.statusCode());
                return "[]";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public static boolean updateEvent(String eventId, String name, int capacity, LocalDateTime eventDate) {
        String json = "{\"name\":\"" + name + "\", \"capacity\":" + capacity +
                ", \"event_date\":\"" + eventDate.toString() + "\"}";

        try {
            HttpRequest request = createRequestBuilder("events?events_id=eq." + eventId)
                    .header("Prefer", "return=minimal")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 204 || response.statusCode() == 200;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteEvent(String eventId) {
        try {
            HttpRequest request = createRequestBuilder("events?events_id=eq." + eventId)
                    .header("Prefer", "return=minimal")
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204 || response.statusCode() == 200;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateEventFlags(String eventId, boolean waitlistEnabled, boolean acceptingBookings) {
        String json = "{\"waitlist_enabled\":" + waitlistEnabled +
                ",\"accepting_bookings\":" + acceptingBookings + "}";

        try {
            HttpRequest request = createRequestBuilder("events?events_id=eq." + eventId)
                    .header("Prefer", "return=minimal")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204 || response.statusCode() == 200;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}