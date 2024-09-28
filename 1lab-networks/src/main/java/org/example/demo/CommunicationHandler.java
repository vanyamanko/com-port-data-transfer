package org.example.demo;

import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Objects;

public class CommunicationHandler {

    private static final String ERROR_MESSAGE = "Ports are not connected.";
    private static final String FLAG = "$p";
    private static final int DESTINATION_ADDRESS = 0;
    private static final int FCS = 0;
    private static final int CHUNK_SIZE = 16;

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
            byte[] messageBytes = message.getBytes();
            int totalChunks = (int) Math.ceil((double) messageBytes.length / CHUNK_SIZE);

            for (int i = 0; i < totalChunks; i++) {
                int sourceAddress = Integer.parseInt(String.valueOf(portSendField.getValue()
                        .charAt(portSendField.getValue().length() - 1)));

                int start = i * CHUNK_SIZE;
                int length = Math.min(CHUNK_SIZE, messageBytes.length - start);

                byte[] chunk = new byte[length];
                System.arraycopy(messageBytes, start, chunk, 0, length);

                String chunkMessage = new String(chunk);

                String messagePackage = FLAG + DESTINATION_ADDRESS + sourceAddress + chunkMessage + FCS;

                byte[] data = messagePackage.getBytes();
                int bytesWritten = sendPort.writeBytes(data, data.length);
                if (bytesWritten > 0) {
                    inputField.clear();
                } else {
                    errorUI.showErrorDialog("Failed to write data.");
                }
            }

            byte[] readByte = new byte[1];
            byte[] flagBytes = new byte[2];
            byte[] addressBytes = new byte[2];
            boolean dataStartFlag = false;
            ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
            while (receivePort.readBytes(readByte, readByte.length) > 0) {
                byte currentByte = readByte[0];
                flagBytes[0] = flagBytes[1];
                flagBytes[1] = currentByte;

                if (Arrays.equals(flagBytes, FLAG.getBytes())) {
                    receivePort.readBytes(addressBytes, addressBytes.length);
                    dataStartFlag = true;

                    if (dataBytes.size() != 0) {
                        byte[] currentData = dataBytes.toByteArray();
                        dataBytes.reset();
                        dataBytes.write(currentData, 0, currentData.length - 2);
                        String receivedBytes = dataBytes.toString();
                        outputArea.appendText(receivedBytes);
                        infoArea.appendText("Message: " + receivedBytes + " | Baud rate: 9600" + " | Bytes in package: " + dataBytes.size() + " | Package structure: \n");
                        dataBytes.reset();
                    }
                    continue;
                }
                if (dataStartFlag) {
                    dataBytes.write(currentByte);
                }

            }
            byte[] currentData = dataBytes.toByteArray();
            dataBytes.reset();
            dataBytes.write(currentData, 0, currentData.length - 1);
            String receivedBytes = dataBytes.toString();
            outputArea.appendText(receivedBytes + "\n");
            infoArea.appendText("Message: " + receivedBytes + " | Baud rate: 9600" + " | Bytes in package: " + dataBytes.size() + " | Package structure: \n");

            sendPort.closePort();
            receivePort.closePort();
        } else {
            inputField.clear();
            errorUI.showErrorDialog(ERROR_MESSAGE);
        }
    }

    public int getParityValue(String selectedParity) {
        return switch (selectedParity) {
            case "Even" -> 1;
            case "Odd" -> 2;
            case "Mark" -> 3;
            case "Space" -> 4;
            default -> 0;
        };
    }
}