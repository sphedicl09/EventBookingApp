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

---

## Cloning the Project

Clone the repository to your local machine:

```bash
git clone https://github.com/sphedicl09/Cluster-Buster-EventBookingApp.git
cd Cluster-Buster-EventBookingApp
```

---

## Setting Up in IntelliJ IDEA

1. **Open the Project**  
   * Go to `File → Open → Select the Cluster-Buster-EventBookingApp` folder.  
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

4. **Install Maven Dependencies**
    ```bash
    mvn clean install

---

## Running the App

Run `Main.java` in IntelliJ, or use Maven:
  ```bash
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
