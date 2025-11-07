package com.eventbooking.eventbookingapp.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.eventbooking.eventbookingapp.util.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

public class EventManager {
    private static final ObservableList<Event> events = FXCollections.observableArrayList();

    public static ObservableList<Event> getEvents() {
        return events;
    }

    public static void addEvent(String eventName, int capacity) {
        if (eventName == null || eventName.trim().isEmpty()) {
            return;
        }

        try {
            String response = SupabaseService.saveEvent(eventName, capacity);

            if (!response.startsWith("[")) {
                System.out.println("⚠️ Supabase save event failed: " + response);
                return;
            }

            JSONArray array = new JSONArray(response);
            if (array.length() > 0) {
                JSONObject obj = array.getJSONObject(0);
                String id = obj.getString("events_id");
                String name = obj.getString("name");
                int cap = obj.getInt("capacity");

                Event ev = new Event(id, name, cap);
                events.add(ev);
                // refresh count for new event
                refreshEventCount(ev);
                System.out.println("✅ Event saved to Supabase and added to local list.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("⚠️ Failed to save event to Supabase.");
        }
    }

    public static void loadEventsFromSupabase() {
        try {
            String response = SupabaseService.fetchEvents();
            JSONArray array = new JSONArray(response);

            events.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.getString("id");
                String name = obj.getString("name");
                int capacity = obj.getInt("capacity");

                // Optional flags from DB
                int booked = obj.has("booked_count") ? obj.getInt("booked_count") : 0;
                boolean waitlist = obj.has("waitlist_enabled") ? obj.getBoolean("waitlist_enabled") : false;
                boolean accepting = obj.has("accepting_bookings") ? obj.getBoolean("accepting_bookings") : true;

                Event e = new Event(id, name, capacity, booked, waitlist, accepting);
                events.add(e);
            }
            System.out.println("✅ Events loaded from Supabase: " + events.size());

            // Refresh counts after loading because counts may be stored in a separate table
            refreshAllCounts();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("⚠️ Failed to load events from Supabase.");
        }
    }

    public static void refreshEventCount(Event event) {
        try {
            int count = SupabaseService.fetchEventTicketCount(event.getId());
            event.setBookedCount(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void refreshAllCounts() {
        for (Event e : events) {
            refreshEventCount(e);
        }
    }
}