package org.example.demo;

import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Objects;

public class CommunicationHandler {

    private static final String ERROR_MESSAGE = "Ports are not connected.";
    private static final String FLAG = "$p";
    private static final int DESTINATION_ADDRESS = 0;
    private static final int FCS = 0;
    private static final int CHUNK_SIZE = 16;
    private static final char ESC = '\u001B';
    private static final char BYTE_STUFFING = 0x01;
    private static final String ESC_BYTE_STUFFING = "ESC0x01";
    private final ByteArrayOutputStream receivedMassege = new ByteArrayOutputStream();

    private final ErrorUI errorUI = new ErrorUI();

    public void handleEnter(TextField inputField, ComboBox<String> portSendField, ComboBox<String> portReceiveField, TextArea outputArea, TextFlow infoArea, int parity) {
        if (arePortsInvalid(portSendField, portReceiveField)) {
            inputField.clear();
            errorUI.showErrorDialog("Select the correct port to send data.");
            return;
        }

        SerialPort sendPort = openPort("/dev/" + portSendField.getValue(), parity);
        SerialPort receivePort = openPort("/dev/" + portReceiveField.getValue(), parity);

        if (sendPort != null && receivePort != null) {
            processSendingData(inputField, portSendField, sendPort);
            processReceivingData(receivePort, outputArea, infoArea);

            closePorts(sendPort, receivePort);
        } else {
            inputField.clear();
            errorUI.showErrorDialog(ERROR_MESSAGE);
        }
    }

    private boolean arePortsInvalid(ComboBox<String> portSendField, ComboBox<String> portReceiveField) {
        return Objects.equals(portSendField.getValue(), "N/A") || Objects.equals(portReceiveField.getValue(), "N/A");
    }

    private SerialPort openPort(String portName, int parity) {
        SerialPort port = SerialPort.getCommPort(portName);
        if (port.openPort()) {
            port.setComPortParameters(9600, 8, 1, parity);
            return port;
        }
        return null;
    }

    private void processSendingData(TextField inputField, ComboBox<String> portSendField, SerialPort sendPort) {
        String message = inputField.getText();
        byte[] messageBytes = message.getBytes();
        int totalChunks = (int) Math.ceil((double) messageBytes.length / CHUNK_SIZE);

        for (int i = 0; i < totalChunks; i++) {
            int sourceAddress = Integer.parseInt(String.valueOf(portSendField.getValue()
                    .charAt(portSendField.getValue().length() - 1)));

            byte[] chunk = getChunk(messageBytes, i);
            String messagePackage = constructMessagePackage(chunk, sourceAddress);

            byte[] data = messagePackage.getBytes();
            int bytesWritten = sendPort.writeBytes(data, data.length);
            if (bytesWritten <= 0) {
                errorUI.showErrorDialog("Failed to write data.");
            }
        }

        inputField.clear();
    }

    private byte[] getChunk(byte[] messageBytes, int chunkIndex) {
        int start = chunkIndex * CHUNK_SIZE;
        int length = Math.min(CHUNK_SIZE, messageBytes.length - start);

        byte[] chunk = new byte[length];
        System.arraycopy(messageBytes, start, chunk, 0, length);
        return chunk;
    }

    private String constructMessagePackage(byte[] chunk, int sourceAddress) {
        String chunkMessage = new String(chunk);
        String replacedMessage = chunkMessage.replace(FLAG, ESC + String.valueOf(BYTE_STUFFING));

        return FLAG + DESTINATION_ADDRESS + sourceAddress + replacedMessage + FCS;
    }

    private void processReceivingData(SerialPort receivePort, TextArea outputArea, TextFlow infoArea) {
        ByteArrayOutputStream packageStructure = new ByteArrayOutputStream();
        ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
        byte[] readByte = new byte[1];
        byte[] flagBytes = new byte[2];
        byte[] addressBytes = new byte[2];
        boolean dataStartFlag = false;

        while (receivePort.readBytes(readByte, readByte.length) > 0) {
            byte currentByte = readByte[0];
            shiftArray(flagBytes, currentByte);

            if (Arrays.equals(flagBytes, FLAG.getBytes())) {
                handleNewFlag(receivePort, packageStructure, dataBytes, addressBytes, infoArea);
                dataStartFlag = true;
                continue;
            }

            if (dataStartFlag) {
                handleDataByte(currentByte, readByte, receivePort, packageStructure, dataBytes);
            }
        }
        removeLastBytes(dataBytes, 1);
        displayPackageData(infoArea, packageStructure, dataBytes);
        outputArea.appendText(receivedMassege.toString() + '\n');
        receivedMassege.reset();
    }

    private void shiftArray(byte[] array, byte newValue) {
        array[0] = array[1];
        array[1] = newValue;
    }

    private void handleNewFlag(SerialPort receivePort, ByteArrayOutputStream packageStructure, ByteArrayOutputStream dataBytes,
                               byte[] addressBytes, TextFlow infoArea) {
        receivePort.readBytes(addressBytes, addressBytes.length);

        if (packageStructure.size() != 0) {
            byte[] tempData = packageStructure.toByteArray();
            packageStructure.reset();
            packageStructure.write(tempData, 0, tempData.length - 1);
        }
        if (packageStructure.size() <= 3) {
            packageStructure.writeBytes(FLAG.getBytes());
            packageStructure.writeBytes(addressBytes);
        }

        if (dataBytes.size() != 0) {
            removeLastBytes(dataBytes, 2);
            displayPackageData(infoArea, packageStructure, dataBytes);
            resetStreams(packageStructure, dataBytes, FLAG.getBytes(), addressBytes);
        }
    }

    private void removeLastBytes(ByteArrayOutputStream stream, int n) {
        if (stream.size() >= n) {
            byte[] currentData = stream.toByteArray();
            stream.reset();
            stream.write(currentData, 0, currentData.length - n);
        }
    }

    private void handleDataByte(byte currentByte, byte[] readByte, SerialPort receivePort,
                                ByteArrayOutputStream packageStructure, ByteArrayOutputStream dataBytes) {
        if (currentByte == ESC) {
            packageStructure.writeBytes("ESC".getBytes());
            receivePort.readBytes(readByte, readByte.length);
            if (readByte[0] == BYTE_STUFFING) {
                packageStructure.writeBytes("0x01".getBytes());
                dataBytes.writeBytes(FLAG.getBytes());
            }
        } else {
            dataBytes.write(currentByte);
            packageStructure.write(currentByte);
        }
    }

    private void displayPackageData(TextFlow infoArea, ByteArrayOutputStream packageStructure,
                                    ByteArrayOutputStream dataBytes) {

        receivedMassege.writeBytes(dataBytes.toByteArray());

        Text info = new Text("Message: " + dataBytes + " | Baud rate: 9600" + " | Bytes in package: " + dataBytes.size()
                + " | Package structure: ");
        infoArea.getChildren().add(info);

        addPackageStructureToInfo(packageStructure, infoArea);
        infoArea.getChildren().addAll(new Text("\n"));
    }

    private void addPackageStructureToInfo(ByteArrayOutputStream packageStructure, TextFlow infoArea) {
        String[] parts = packageStructure.toString().split(ESC_BYTE_STUFFING);
        for (int i = 0; i < parts.length; i++) {
            Text normalText = new Text(parts[i]);
            infoArea.getChildren().add(normalText);

            if (i < parts.length - 1) {
                Text blueText = new Text(ESC_BYTE_STUFFING);
                blueText.setFill(Color.BLUE);
                infoArea.getChildren().add(blueText);
            }
        }
    }

    private void resetStreams(ByteArrayOutputStream packageStructure, ByteArrayOutputStream dataBytes,
                              byte[] flagBytes, byte[] addressBytes) {
        dataBytes.reset();
        packageStructure.reset();
        packageStructure.writeBytes(flagBytes);
        packageStructure.writeBytes(addressBytes);
    }

    private void closePorts(SerialPort sendPort, SerialPort receivePort) {
        sendPort.closePort();
        receivePort.closePort();
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
