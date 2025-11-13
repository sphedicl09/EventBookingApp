package com.eventbooking.eventbookingapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/eventbooking/eventbookingapp/organizer-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isAltDown() && event.getCode() == KeyCode.C) {
                stage.close();
            }
        });
        scene.getStylesheets().add(getClass().getResource("/com/eventbooking/eventbookingapp/styles.css").toExternalForm());
        stage.setTitle("Event Booking System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
