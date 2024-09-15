package org.example.demo;

import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Objects;

public class CommunicationHandler {

    private static final String ERROR_MESSAGE = "Ports are not connected.";

    private final ErrorUI errorUI = new ErrorUI();

    public void handleEnter(TextField inputField, ComboBox<String> portSendField, ComboBox<String> portReceiveField, TextArea outputArea, TextArea infoArea, int parity) {
        if (Objects.equals(portSendField.getValue(), "N/A") || Objects.equals(portReceiveField.getValue(), "N/A")) {
            inputField.clear();
            errorUI.showErrorDialog("Select the correct port to send data.");
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
                inputField.clear();
            } else {
                errorUI.showErrorDialog(ERROR_MESSAGE);
            }

            byte[] readBuffer = new byte[1024];
            int numRead = receivePort.readBytes(readBuffer, readBuffer.length);

            if (numRead > 0) {
                String receivedData = new String(readBuffer, 0, numRead);
                outputArea.appendText(receivedData + "\n");
                infoArea.appendText("Message: " + receivedData + " | Baud rate: 9600" + " | Bytes sent: " + numRead + "\n");
            } else {
                errorUI.showErrorDialog(ERROR_MESSAGE);
            }

            sendPort.closePort();
            receivePort.closePort();
        } else {
            inputField.clear();
            errorUI.showErrorDialog(ERROR_MESSAGE);
        }
    }

    public int getParityValue(String selectedParity) {
        return switch (selectedParity) {
            case "Odd" -> 2;
            case "Even" -> 1;
            default -> 0;
        };
    }
}