package org.example.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    Logger log = Logger.getLogger(getClass().getName());
    private TextArea infoArea;
    private Integer parity = 0;
    private static final ArrayList<String> devicesList = new ArrayList<>();

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

    public void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        File devDir = new File("/dev");

        File[] devices = devDir.listFiles((dir, name) -> name.startsWith("ttys") && name.length() == 7);

        assert devices != null;
        for (File device : devices) {
            devicesList.add(device.getName());
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Networks");

        TextField inputField = new TextField();
        inputField.setPromptText("Enter text and press Enter");

        TextArea outputArea;
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Output");

        Button clearButton = new Button("Clear Output");
        clearButton.setOnAction(e -> {
            outputArea.clear();
            infoArea.clear();
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
        parityOptions.getItems().addAll("Odd", "Even", "No Parity");
        parityOptions.setValue("No Parity");
        parityOptions.setOnAction(event -> {
            String selectedParity = parityOptions.getValue();
            switch (selectedParity) {
                case "Odd":
                    parity = 2;
                    break;
                case "Even":
                    parity = 1;
                    break;
                default:
                    parity = 0;
                    break;
            }
        });

        infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setPromptText("Information");


        inputField.setOnAction(e -> {
            if (Objects.equals(portSendField.getValue(), "N/A") ||
                    Objects.equals(portReceiveField.getValue(), "N/A")) {
                log.info("Error message window.");
                inputField.clear();
                showErrorDialog("Error", "Select the correct port to send data.");
                return;
            }
            SerialPort sendPort = SerialPort.getCommPort("/dev/" + portSendField.getValue());
            SerialPort receivePort = SerialPort.getCommPort("/dev/" + portReceiveField.getValue());

            if (sendPort.openPort() && receivePort.openPort()) {
                sendPort.setComPortParameters(9600, 8, 1, parity);
                receivePort.setComPortParameters(9600, 8, 1, parity);

                String message = inputField.getText();
                byte[] data = message.getBytes();

                int bytesWritten = sendPort.writeBytes(data, data.length);
                if (bytesWritten > 0) {
                    log.info("Data sent successfully: " + message);
                    inputField.clear();
                } else {
                    log.info("Error sending data.");
                    showErrorDialog("Error", "Ports are not connected.");
                }

                byte[] readBuffer = new byte[1024];
                int numRead = receivePort.readBytes(readBuffer, readBuffer.length);

                if (numRead > 0) {
                    String receivedData = new String(readBuffer, 0, numRead);
                    log.info("Data received: " + receivedData);
                    outputArea.appendText(receivedData + "\n");
                    infoArea.appendText("Message: " + receivedData + " | Baud rate: 9600" + " | Bytes sent: " + numRead + "\n");
                } else {
                    log.info("Error read data.");
                    showErrorDialog("Error", "Ports are not connected.");
                }

                sendPort.closePort();
                receivePort.closePort();
            } else {
                log.info("Failed to open port.");
                log.info("Error message window.");
                inputField.clear();
                showErrorDialog("Error", "Ports are not connected.");
            }
        });

        VBox controlPane = new VBox(10, clearButton, portSendLabel, portSendField, portReceiveLabel, portReceiveField, parityLabel, parityOptions);
        controlPane.setStyle("-fx-padding: 10; -fx-spacing: 10;");
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(inputField);
        mainLayout.setCenter(outputArea);
        mainLayout.setRight(controlPane);
        mainLayout.setBottom(infoArea);

        parityOptions.setStyle(COMBO_BOX_STYLE);
        parityOptions.setOnMouseEntered(e -> parityOptions.setStyle(COMBO_BOX_HOVER_STYLE));
        parityOptions.setOnMouseExited(e -> parityOptions.setStyle(COMBO_BOX_STYLE));

        portSendField.setStyle(COMBO_BOX_STYLE);
        portSendField.setOnMouseEntered(e -> portSendField.setStyle(COMBO_BOX_HOVER_STYLE));
        portSendField.setOnMouseExited(e -> portSendField.setStyle(COMBO_BOX_STYLE));

        portReceiveField.setStyle(COMBO_BOX_STYLE);
        portReceiveField.setOnMouseEntered(e -> portReceiveField.setStyle(COMBO_BOX_HOVER_STYLE));
        portReceiveField.setOnMouseExited(e -> portReceiveField.setStyle(COMBO_BOX_STYLE));

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