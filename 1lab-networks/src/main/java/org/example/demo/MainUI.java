package org.example.demo;

import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class MainUI {

    private final Stage primaryStage;
    private final List<String> devicesList;
    private final TextFlow infoArea = new TextFlow();
    private Integer parity = 0;
    private final CommunicationHandler communicationHandler;

    public MainUI(Stage primaryStage, List<String> devicesList) {
        this.primaryStage = primaryStage;
        this.devicesList = devicesList;
        this.communicationHandler = new CommunicationHandler();
    }

    public void initializeUI() {
        TextField inputField = new TextField();
        inputField.setPromptText("Enter text and press Enter");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Output");

        Button clearButton = new Button("Clear Output");
        clearButton.setOnAction(e -> {
            outputArea.clear();
            infoArea.getChildren().clear();
        });

        Label portSendLabel = new Label("Choice send port number:");
        ComboBox<String> portSendField = new ComboBox<>();
        portSendField.getItems().addAll(devicesList);
        portSendField.setValue("N/A");

        Label portReceiveLabel = new Label("Choice receive port number:");
        ComboBox<String> portReceiveField = new ComboBox<>();
        portReceiveField.getItems().addAll(devicesList);
        portReceiveField.setValue("N/A");

        Label parityLabel = new Label("Select parity check option:");
        ComboBox<String> parityOptions = new ComboBox<>();
        parityOptions
            .getItems()
            .addAll("Even", "Odd", "Mark", "Space", "No Parity");
        parityOptions.setValue("No Parity");
        parityOptions.setOnAction(event -> {
            String selectedParity = parityOptions.getValue();
            parity = communicationHandler.getParityValue(selectedParity);
        });

        infoArea.setAccessibleHelp("Information");
        infoArea.setMinWidth(400);
        infoArea.setMinHeight(200);

        inputField.setOnAction(e ->
            communicationHandler.handleEnter(
                inputField,
                portSendField,
                portReceiveField,
                outputArea,
                infoArea,
                parity
            )
        );

        VBox controlPane = new VBox(
            10,
            clearButton,
            portSendLabel,
            portSendField,
            portReceiveLabel,
            portReceiveField,
            parityLabel,
            parityOptions
        );
        controlPane.setStyle("-fx-padding: 10; -fx-spacing: 10;");
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(inputField);
        mainLayout.setCenter(outputArea);
        mainLayout.setRight(controlPane);
        mainLayout.setBottom(infoArea);
        StyleManager.applyStyles(
            mainLayout,
            inputField,
            outputArea,
            infoArea,
            clearButton,
            controlPane,
            portSendField,
            portReceiveField,
            parityOptions
        );

        Scene scene = new Scene(mainLayout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
