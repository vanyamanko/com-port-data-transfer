package org.example.demo.service;

import static org.example.demo.service.SerialPortConstants.*;

import com.fazecast.jSerialComm.SerialPort;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
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
        AtomicInteger collisionAmount = new AtomicInteger(0);
        int destinationAddress = serialPortManager.getPortAddress(
            portReceiveField
        );

        while (receivePort.readBytes(readByte, readByte.length) > 0) {
            byte currentByte = readByte[0];
            if (currentByte == (byte) JAM_SIGNAL) {
                dataStartFlag = false;
                collisionAmount.incrementAndGet();
                dataBytes.reset();
                packageStructure.reset();
            }
            shiftArray(flagBytes, currentByte);

            if (Arrays.equals(flagBytes, FLAG.getBytes())) {
                dataStartFlag = handleNewFlag(
                    receivePort,
                    packageStructure,
                    dataBytes,
                    addressBytes,
                    infoArea,
                    destinationAddress,
                    collisionAmount
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
        if (dataBytes.size() != 0 && packageStructure.size() != 0) {
            fcsCheckAndDisplay(
                infoArea,
                packageStructure,
                dataBytes,
                collisionAmount
            );
            outputArea.appendText(receivedMassege.toString() + '\n');
        } else {
            errorUI.showErrorDialog(
                "Ports are not connected or message is empty."
            );
        }
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
            if (readByte[0] == BYTE_STUFFING_FLAG) {
                packageStructure.writeBytes("0x01".getBytes());
                dataBytes.writeBytes(FLAG.getBytes());
            }
            if (readByte[0] == BYTE_STUFFING_JAM_SIGNAL) {
                packageStructure.writeBytes("0x02".getBytes());
                dataBytes.writeBytes(String.valueOf(JAM_SIGNAL).getBytes());
            }
        } else {
            dataBytes.write(currentByte);
            packageStructure.write(currentByte);
        }
    }

    private void shiftArray(byte[] array, byte newValue) {
        if (array == null || array.length < 2) {
            throw new IllegalArgumentException(
                "Array must have at least 2 elements."
            );
        }
        array[0] = array[1];
        array[1] = newValue;
    }

    private boolean handleNewFlag(
        SerialPort receivePort,
        ByteArrayOutputStream packageStructure,
        ByteArrayOutputStream dataBytes,
        byte[] addressBytes,
        TextFlow infoArea,
        int destinationAddress,
        AtomicInteger collisionAmount
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
            getAndRemoveLastByte(dataBytes);
            fcsCheckAndDisplay(
                infoArea,
                packageStructure,
                dataBytes,
                collisionAmount
            );
            resetStreams(
                packageStructure,
                dataBytes,
                FLAG.getBytes(),
                addressBytes
            );
        }
        return true;
    }

    private byte getAndRemoveLastByte(ByteArrayOutputStream stream)
        throws ArrayIndexOutOfBoundsException {
        if (stream.size() == 0) {
            throw new ArrayIndexOutOfBoundsException(
                "Error: Stream is empty, last byte cannot be removed."
            );
        }
        byte[] currentData = stream.toByteArray();

        byte removedByte = currentData[currentData.length - 1];

        stream.reset();
        stream.write(currentData, 0, currentData.length - 1);

        return removedByte;
    }

    private void displayPackageData(
        TextFlow infoArea,
        ByteArrayOutputStream packageStructure,
        ByteArrayOutputStream dataBytes,
        int unsignedFcsCalculated,
        AtomicInteger collisionAmount
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
            " | Number of collisions during transmission: " +
            collisionAmount +
            " | Package structure: "
        );
        infoArea.getChildren().add(info);

        getAndRemoveLastByte(packageStructure);
        addPackageStructureToInfo(
            packageStructure,
            unsignedFcsCalculated,
            infoArea
        );
        infoArea.getChildren().addAll(new Text("\n"));
        collisionAmount.set(0);
    }

    private void addPackageStructureToInfo(
        ByteArrayOutputStream packageStructure,
        int unsignedFcsCalculated,
        TextFlow infoArea
    ) {
        String[] parts = packageStructure
            .toString()
            .split(ESC_BYTE_STUFFING_FLAG);
        for (int i = 0; i < parts.length; i++) {
            Text normalText = new Text(parts[i]);
            infoArea.getChildren().add(normalText);

            if (i < parts.length - 1) {
                Text blueText = new Text(ESC_BYTE_STUFFING_FLAG);
                blueText.setFill(Color.BLUE);
                infoArea.getChildren().add(blueText);
            }
        }
        Text unsignedFcsCalculatedText = new Text(
            "0x" + Integer.toHexString(unsignedFcsCalculated).toUpperCase()
        );
        unsignedFcsCalculatedText.setFill(Color.GREEN);
        infoArea.getChildren().add(unsignedFcsCalculatedText);
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

    private void fcsCheckAndDisplay(
        TextFlow infoArea,
        ByteArrayOutputStream packageStructure,
        ByteArrayOutputStream dataBytes,
        AtomicInteger collisionAmount
    ) {
        byte fcsReceived = getAndRemoveLastByte(dataBytes);
        byte fcsCalculated = (byte) SerialPortManager.calculateCRC8(
            dataBytes.toByteArray()
        );
        int unsignedFcsReceived = fcsReceived & 0xFF;
        int unsignedFcsCalculated = fcsCalculated & 0xFF;

        if (unsignedFcsReceived == unsignedFcsCalculated) {
            displayPackageData(
                infoArea,
                packageStructure,
                dataBytes,
                unsignedFcsCalculated,
                collisionAmount
            );
        } else {
            throw new IllegalArgumentException(
                "FCS check failed. Received: " +
                unsignedFcsReceived +
                ", Calculated: " +
                unsignedFcsCalculated
            );
        }
    }
}
