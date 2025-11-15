package com.eventbooking.eventbookingapp.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ViewSwitcher {

    private static final String STYLESHEET_PATH = "/com/eventbooking/eventbookingapp/styles.css";

    public static void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(ViewSwitcher.class.getResource(fxmlFile));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        String stylesheet = ViewSwitcher.class.getResource(STYLESHEET_PATH).toExternalForm();
        scene.getStylesheets().add(stylesheet);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.isAltDown() && e.getCode() == KeyCode.C) {
                stage.close();
            }
        });
        stage.setScene(scene);
    }
}