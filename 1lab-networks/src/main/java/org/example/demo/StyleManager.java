package org.example.demo;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

public class StyleManager {

    private static final String COMBO_BOX_STYLE = "-fx-background-color: #ffffff; " +
            "-fx-border-color: #cccccc; " +
            "-fx-border-radius: 5; " +
            "-fx-padding: 5; " +
            "-fx-font-size: 14px;";

    private static final String COMBO_BOX_HOVER_STYLE = "-fx-background-color: #e0f7fa; " +
            "-fx-border-color: #00838f; " +
            "-fx-border-radius: 5; " +
            "-fx-padding: 5; " +
            "-fx-font-size: 14px;";

    private static final String MAIN_LAYOUT_STYLE = "-fx-padding: 10; -fx-background-color: #f4f4f4;";
    private static final String INPUT_STYLE = COMBO_BOX_STYLE;
    private static final String BUTTON_STYLE = "-fx-background-color: #2e8bff; -fx-text-fill: white; -fx-border-radius: 5;";

    private StyleManager() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void applyStyles(BorderPane mainLayout, TextField inputField, TextArea outputArea, TextFlow infoArea, Button clearButton, VBox controlPane,
                                   ComboBox<String> portSendField, ComboBox<String> portReceiveField, ComboBox<String> parityOptions) {
        controlPane.setStyle("-fx-padding: 10; -fx-spacing: 10;");
        mainLayout.setStyle(MAIN_LAYOUT_STYLE);
        inputField.setStyle(INPUT_STYLE);
        outputArea.setStyle(INPUT_STYLE);
        infoArea.setStyle(INPUT_STYLE);
        clearButton.setStyle(BUTTON_STYLE);
        clearButton.setPrefWidth(150);

        applyComboBoxStyles(portSendField);
        applyComboBoxStyles(portReceiveField);
        applyComboBoxStyles(parityOptions);
    }

    private static void applyComboBoxStyles(ComboBox<String> comboBox) {
        comboBox.setStyle(COMBO_BOX_STYLE);
        comboBox.setOnMouseEntered(e -> comboBox.setStyle(COMBO_BOX_HOVER_STYLE));
        comboBox.setOnMouseExited(e -> comboBox.setStyle(COMBO_BOX_STYLE));
    }
}