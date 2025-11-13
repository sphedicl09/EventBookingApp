package com.eventbooking.eventbookingapp.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.eventbooking.eventbookingapp.util.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class EventManager {
    private static final ObservableList<Event> events = FXCollections.observableArrayList();

    public static ObservableList<Event> getEvents() {
        return events;
    }

    public static boolean addEvent(String eventName, int capacity, LocalDateTime eventDate) {
        if (eventName == null || eventName.trim().isEmpty()) {
            return false;
        }

        try {
            String response = SupabaseService.saveEvent(eventName, capacity, eventDate);

            if (!response.startsWith("[")) {
                System.out.println("⚠️ Supabase save event failed: " + response);
                return false; // Return false
            }

            JSONArray array = new JSONArray(response);
            if (array.length() > 0) {
                JSONObject obj = array.getJSONObject(0);
                String id = obj.getString("events_id");
                String name = obj.getString("name");
                int cap = obj.getInt("capacity");

                events.add(new Event(id, name, cap, 0, false, true, eventDate));
                System.out.println("✅ Event saved to Supabase and added to local list.");
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("⚠️ Failed to save event to Supabase.");
            return false;
        }
    }

    public static void loadEventsFromSupabase() {
        try {
            String response = SupabaseService.fetchEvents("events?select=*,tickets(count)");
            JSONArray array = new JSONArray(response);
            events.clear();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.getString("events_id");
                String name = obj.optString("name", "Unnamed Event");
                int capacity = obj.optInt("capacity", 0);
                boolean waitlist = obj.optBoolean("waitlist_enabled", false);
                boolean accepting = obj.optBoolean("accepting_bookings", true);
                String dateStr = obj.optString("event_date", null);
                LocalDateTime eventDate = null;

                if (dateStr != null) {
                    eventDate = ZonedDateTime.parse(dateStr).toLocalDateTime();
                }

                int booked = 0;
                JSONArray ticketsArray = obj.optJSONArray("tickets");
                if (ticketsArray != null && ticketsArray.length() > 0) {
                    booked = ticketsArray.getJSONObject(0).getInt("count");
                }

                events.add(new Event(id, name, capacity, booked, waitlist, accepting, eventDate));
            }
            System.out.println("✅ Events loaded from Supabase: " + events.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("⚠️ Failed to load events from Supabase.");
        }
    }

    public static void updateEvent(Event event, String newName, int newCapacity, LocalDateTime eventDate) {
        event.setName(newName);
        event.setCapacity(newCapacity);
        event.setEventDate(eventDate);
    }

    public static void removeEvent(Event event) {
        events.remove(event);
    }
}