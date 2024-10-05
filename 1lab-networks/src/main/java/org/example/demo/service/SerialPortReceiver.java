package org.example.demo.service;

import static org.example.demo.service.SerialPortConstants.*;

import com.fazecast.jSerialComm.SerialPort;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.example.demo.ui.ErrorUI;

public class SerialPortReceiver {

    private final ErrorUI errorUI = new ErrorUI();
    private final SerialPortManager serialPortManager = new SerialPortManager();
    private final ByteArrayOutputStream receivedMassege =
        new ByteArrayOutputStream();

    public void processReceivingData(
        SerialPort receivePort,
        TextArea outputArea,
        TextFlow infoArea,
        ComboBox<String> portReceiveField
    ) {
        ByteArrayOutputStream packageStructure = new ByteArrayOutputStream();
        ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
        byte[] readByte = new byte[1];
        byte[] flagBytes = new byte[2];
        byte[] addressBytes = new byte[2];
        boolean dataStartFlag = false;
        int destinationAddress = serialPortManager.getPortAddress(
            portReceiveField
        );

        while (receivePort.readBytes(readByte, readByte.length) > 0) {
            byte currentByte = readByte[0];
            shiftArray(flagBytes, currentByte);

            if (Arrays.equals(flagBytes, FLAG.getBytes())) {
                dataStartFlag = handleNewFlag(
                    receivePort,
                    packageStructure,
                    dataBytes,
                    addressBytes,
                    infoArea,
                    destinationAddress
                );
                continue;
            }

            if (dataStartFlag) {
                handleDataByte(
                    currentByte,
                    readByte,
                    receivePort,
                    packageStructure,
                    dataBytes
                );
            }
        }
        removeLastBytes(dataBytes, 1);
        displayPackageData(infoArea, packageStructure, dataBytes);
        outputArea.appendText(receivedMassege.toString() + '\n');
        receivedMassege.reset();
    }

    private void handleDataByte(
        byte currentByte,
        byte[] readByte,
        SerialPort receivePort,
        ByteArrayOutputStream packageStructure,
        ByteArrayOutputStream dataBytes
    ) {
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

    private void shiftArray(byte[] array, byte newValue) {
        array[0] = array[1];
        array[1] = newValue;
    }

    private boolean handleNewFlag(
        SerialPort receivePort,
        ByteArrayOutputStream packageStructure,
        ByteArrayOutputStream dataBytes,
        byte[] addressBytes,
        TextFlow infoArea,
        int destinationAddress
    ) {
        receivePort.readBytes(addressBytes, addressBytes.length);
        int addressReceivePort = (addressBytes[1] & 0xFF) - 48;
        if (addressReceivePort != destinationAddress) {
            return false;
        }

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
            resetStreams(
                packageStructure,
                dataBytes,
                FLAG.getBytes(),
                addressBytes
            );
        }
        return true;
    }

    private void removeLastBytes(ByteArrayOutputStream stream, int n) {
        if (stream.size() >= n) {
            byte[] currentData = stream.toByteArray();
            stream.reset();
            stream.write(currentData, 0, currentData.length - n);
        }
    }

    private void displayPackageData(
        TextFlow infoArea,
        ByteArrayOutputStream packageStructure,
        ByteArrayOutputStream dataBytes
    ) {
        if (dataBytes.size() != 0) {
            receivedMassege.writeBytes(dataBytes.toByteArray());
        } else {
            errorUI.showErrorDialog("Error received data.");
            return;
        }
        Text info = new Text(
            "Message: " +
            dataBytes +
            " | Baud rate: 9600" +
            " | Bytes in package: " +
            dataBytes.size() +
            " | Package structure: "
        );
        infoArea.getChildren().add(info);

        addPackageStructureToInfo(packageStructure, infoArea);
        infoArea.getChildren().addAll(new Text("\n"));
    }

    private void addPackageStructureToInfo(
        ByteArrayOutputStream packageStructure,
        TextFlow infoArea
    ) {
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

    private void resetStreams(
        ByteArrayOutputStream packageStructure,
        ByteArrayOutputStream dataBytes,
        byte[] flagBytes,
        byte[] addressBytes
    ) {
        dataBytes.reset();
        packageStructure.reset();
        packageStructure.writeBytes(flagBytes);
        packageStructure.writeBytes(addressBytes);
    }
}
