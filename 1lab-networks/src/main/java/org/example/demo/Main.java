package org.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private TextArea infoArea;

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

    public static void main(String[] args) {
        launch(args);
    }
    //

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Interface Example");

        // Input field
        TextField inputField = new TextField();
        inputField.setPromptText("Enter text and press Enter");

        // Output area
        TextArea outputArea;
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Output");

        // Control panel
        Button clearButton = new Button("Clear Output");
        clearButton.setOnAction(e -> {
            outputArea.clear();
            infoArea.clear(); // Clear debug information
        });

        // Port number
        Label portLabel = new Label("Enter port number:");
        TextField portField = new TextField();
        portField.setPromptText("For example, 8080");

        // Parity options
        Label parityLabel = new Label("Select parity check option:");
        ComboBox<String> parityOptions = new ComboBox<>();
        parityOptions.getItems().addAll("Odd", "Even", "No Parity");
        parityOptions.setValue("No Parity");

        // Debug area
        infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setPromptText("Information");

        // Input handler
        inputField.setOnAction(e -> {
            String input = inputField.getText();
            outputArea.appendText(input + "\n");
            infoArea.appendText("Message: " + input + " | Baud rate: " + " | Send bytes: " + "\n");
            inputField.clear();
        });

        //
        VBox controlPane = new VBox(10, clearButton, portLabel, portField, parityLabel, parityOptions);
        controlPane.setStyle("-fx-padding: 10; -fx-spacing: 10;");
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(inputField);
        mainLayout.setCenter(outputArea);
        mainLayout.setRight(controlPane);
        mainLayout.setBottom(infoArea);

        parityOptions.setStyle(COMBO_BOX_STYLE);
        parityOptions.setOnMouseEntered(e -> parityOptions.setStyle(COMBO_BOX_HOVER_STYLE));
        parityOptions.setOnMouseExited(e -> parityOptions.setStyle(COMBO_BOX_STYLE));

        mainLayout.setStyle(MAIN_LAYOUT_STYLE);
        inputField.setStyle(INPUT_STYLE);
        outputArea.setStyle(INPUT_STYLE);
        infoArea.setStyle(INPUT_STYLE);
        clearButton.setStyle(BUTTON_STYLE);
        clearButton.setPrefWidth(150);

        Scene scene = new Scene(mainLayout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}