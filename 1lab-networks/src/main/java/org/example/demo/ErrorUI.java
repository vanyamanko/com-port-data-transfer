package org.example.demo;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ErrorUI {

    public void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
