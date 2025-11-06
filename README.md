# Cluster-Buster EventBookingApp

A JavaFX-based event booking application for organizers and attendees.  
This app allows organizers to create events, and attendees to book tickets.

---

## Prerequisites

Before running the app, make sure you have:

1. **Java Development Kit (JDK) 21**  
   * [Download JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)  
   * Set the `JAVA_HOME` environment variable.

2. **JavaFX 21 SDK**  
   * [Download JavaFX SDK](https://gluonhq.com/products/javafx/)  
   * Unzip and note the path (needed for IDE configuration or VM options).

3. **Maven** (optional if managing dependencies)  
   ```bash
   mvn -v

4. **Supabase Account**
   * Free-tier is enough

5. **Gmail Account (for sending tickets)**
   * You will need to generate a 16-character **App Password**

---

## Cloning the Project

Clone the repository to your local machine:

```bash
git clone https://github.com/sphedicl09/EventBookingApp.git
cd Cluster-Buster-EventBookingApp
```

---

## Backend & Environment Setup

This app **will not run** without setting up the backend database and environment variables.

1. **Supabase Database Setup**
   
   1. **Create a Project:** Go to [Supabase](https://supabase.com/) and create a new project.
      
   3. **Get API Keys:** Go to `Project Settings → API`. You will need two values:
      
      * The **Project URL**
      * The `anon` **public API Key**
        
   5. **Create Tables:** Go to the `SQL Editor` in your Supabase project and run the following script to create the `events` and `tickets` tables:
      ```bash
      -- Create the 'events' table
      create table public.events (
        events_id uuid not null default gen_random_uuid (),
        name text null,
        capacity numeric null,
        created_at timestamp with time zone not null default now(), -- Auto-fills the creation time
        constraint events_pkey primary key (events_id)
      );
      
      -- Create the 'tickets' table
      create table public.tickets (
        tickets_id uuid not null default gen_random_uuid (),
        events_id uuid null, -- This is the foreign key
        attendee_name text null,
        email text null,
        ticket_code text null,
        created_at timestamp with time zone not null default now(), -- Auto-fills the creation time
        constraint tickets_pkey primary key (tickets_id),
        constraint tickets_events_id_fkey foreign key (events_id) references events (events_id) on delete cascade -- Deletes tickets if event is deleted
      );
      ```
   6. **Set Security Policies (RLS):** By default, your tables are read-only. You must enable `INSERT` for the app to work.
      
      * Go to `Authentication → Policies`.  
      * On the `events table`, click **New Policy → "Enable INSERT access for all users" → Review → Save**.
      * Do the ***exact same thing*** for the `tickets` table: **New Policy → "Enable INSERT access for all users"**.
     
2. **Email Service (Gmail) Setup**

   This app uses a Gmail account to send ticket confirmation emails.

     1. **Enable 2-Step Verification:** Go to your Google Account settings → **Security** and turn on 2-Step Verification.
  
     2. **Generate App Password:** On the same **Security** page, go to **App Passwords**.

        * Select app: **"Other (Custom name...)"**
        * Name it: `JavaFX Event App`
        * Google will give you a **16-character password**. Copy this.

      3. **Update Code:** Open the file `src/main/java/com/eventbooking/eventbookingapp/util/EmailSender.java`.

         * Change `APP_PASSWORD` to the 16-character password you just generated.
         * Change `SENDER_EMAIL` to your Gmail address. 
         ```bash
         // In EmailSender.java
         private static final String SENDER_EMAIL = "your-email@gmail.com";
         private static final String APP_PASSWORD = "your-16-character-app-password";
         ```
---

## Setting Up in IntelliJ IDEA

1. **Open the Project**  
   * Go to `File → Open → Select the EventBookingApp` folder.  
   * If prompted, import as a Maven project.

2. **Add JavaFX SDK**  
   * Go to `File → Project Structure → Libraries`.  
   * Click `+ → Java`.  
   * Select the `lib` folder inside your downloaded JavaFX SDK.

3. **Configure Run Configuration**  
   * Go to `Run → Edit Configurations`.  
   * Click `+ → Application`.  
   * Main class: `com.eventbooking.eventbookingapp.Main`  
   * VM options (replace `/path/to/javafx-sdk-21/lib` with your actual JavaFX path):  
   ```bash
   --module-path /path/to/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml
   ```
   * **Environment Variables:** This is essential for Supabase. Click the `[...]` icon next to "Environment variables" and add two:
     * `SUPABASE_URL`: Paste your Project URL here.
     * `SUPABASE_KEY`: Paste your `anon` public API Key here.

4. **Install Maven Dependencies**
    ```bash
    mvn clean install

---

## Running the App

You can now run the Main.java file in IntelliJ.

If you run with Maven, you must set the environment variables in your terminal first:
```bash
# Windows (Command Prompt)
set SUPABASE_URL=your-url
set SUPABASE_KEY=your-key
mvn clean javafx:run

# Windows (PowerShell)
$env:SUPABASE_URL="your-url"
$env:SUPABASE_KEY="your-key"
mvn clean javafx:run

# macOS/Linux
export SUPABASE_URL=your-url
export SUPABASE_KEY=your-key
mvn clean javafx:run
```

---

## App Overview

### Organizer View
* Create events with names and seat capacities.  
* Switch to attendee view using the “Switch to Attendee” button.  
* Receives alerts when seat capacity is full or events are ending soon.

### Attendee View
* View all events created by organizers.  
* Book tickets using the booking dialog.  
* Manage your bookings.  
* Receive alerts for booking confirmation or warnings.

### Booking Dialog
* Enter attendee name and email.  
* Generates ticket PDF (locally, future plan: email ticket).  
* Sends a notification after successful booking.

---

## Development Guidelines

* All of you guys must use **JDK 21** and **JavaFX 21**.  
* Always pull the latest updates before making changes:
* To add new features or bug fixes, create a branch:
  ```bash
  git pull origin main

* Commit with clear messages:
  ```bash
  git add .
  git commit -m "Add feature description"
  git push origin feature/branch-name
  ```
