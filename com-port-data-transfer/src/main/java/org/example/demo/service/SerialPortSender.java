package org.example.demo.service;

import static org.example.demo.service.SerialPortConstants.*;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.example.demo.ui.ErrorUI;

public class SerialPortSender {

    private final SerialPortManager serialPortManager = new SerialPortManager();

    private final ErrorUI errorUI = new ErrorUI();

    public void processSendingData(
        TextField inputField,
        ComboBox<String> portSendField,
        ComboBox<String> portReceiveField,
        SerialPort sendPort,
        SerialPort receivePort
    ) {
        String message = inputField.getText();
        byte[] messageBytes = message.getBytes();
        int totalChunks = (int) Math.ceil(
            (double) messageBytes.length / CHUNK_SIZE
        );

        AtomicInteger bytesAvailablePrev = new AtomicInteger(0);
        int collisionCount = 0;
        for (int i = 0; i < totalChunks; i++) {
            int destinationAddress = serialPortManager.getPortAddress(
                portSendField
            );
            int sourceAddress = serialPortManager.getPortAddress(
                portReceiveField
            );

            byte[] chunk = getChunk(messageBytes, i);
            String messagePackage = constructMessagePackage(
                chunk,
                sourceAddress,
                destinationAddress
            );

            if (sendDataAndFCS(messagePackage, chunk, sendPort) <= 0) {
                errorUI.showErrorDialog("Failed to write data.");
            }
            randomCollisionEmulation(sendPort);
            if (
                collisionСheck(
                    receivePort,
                    sendPort,
                    messagePackage,
                    bytesAvailablePrev
                )
            ) {
                i--;
                collisionCount++;
                if (collisionCount > MAX_NUMBER_OF_COLLISION) {
                    errorUI.showErrorDialog(
                        "The maximum number of collisions has been reached. Please restart the program and try again."
                    );
                    throw new RuntimeException(
                        "The maximum number of collisions has been reached."
                    );
                }
                delayBeforeResending(collisionCount);
                continue;
            }
            collisionCount = 0;
        }

        inputField.clear();
    }

    private String constructMessagePackage(
        byte[] chunk,
        int sourceAddress,
        int destinationAddress
    ) {
        String chunkMessage = new String(chunk);
        String replacedMessage = chunkMessage
            .replace(FLAG, ESC + String.valueOf(BYTE_STUFFING_FLAG))
            .replace(
                String.valueOf(JAM_SIGNAL),
                ESC + String.valueOf(BYTE_STUFFING_JAM_SIGNAL)
            );

        return (FLAG + destinationAddress + sourceAddress + replacedMessage);
    }

    private byte[] getChunk(byte[] messageBytes, int chunkIndex) {
        int start = chunkIndex * CHUNK_SIZE;
        int length = Math.min(CHUNK_SIZE, messageBytes.length - start);

        byte[] chunk = new byte[length];
        System.arraycopy(messageBytes, start, chunk, 0, length);
        return chunk;
    }

    private int sendDataAndFCS(
        String messagePackage,
        byte[] chunk,
        SerialPort sendPort
    ) {
        byte[] data = messagePackage.getBytes();
        byte fcs = (byte) SerialPortManager.calculateCRC8(chunk);
        int bytesWritten = sendPort.writeBytes(data, data.length);
        sendPort.writeBytes(new byte[] { fcs }, 1);
        return bytesWritten;
    }

    private boolean collisionСheck(
        SerialPort receivePort,
        SerialPort sendPort,
        String messagePackage,
        AtomicInteger bytesAvailablePrev
    ) {
        serialPortManager.delay(100);

        if (
            (receivePort.bytesAvailable() - bytesAvailablePrev.get()) ==
            messagePackage.getBytes().length + 1
        ) {
            bytesAvailablePrev.set(receivePort.bytesAvailable());
        } else {
            sendJamSignal(sendPort);
            serialPortManager.delay(50);
            bytesAvailablePrev.set(receivePort.bytesAvailable());
            return true;
        }
        return false;
    }

    private void sendJamSignal(SerialPort sendPort) {
        sendPort.writeBytes(new byte[] { (byte) JAM_SIGNAL }, 1);
    }

    private void randomCollisionEmulation(SerialPort sendPort) {
        Random random = new Random();
        if (random.nextDouble() < PROBABILITY_OF_COLLISION) {
            byte[] collisionEmulation = "data".getBytes();
            sendPort.writeBytes(collisionEmulation, collisionEmulation.length);
        }
    }

    private void delayBeforeResending(int collisionCount) {
        Random random = new Random();
        int timeSlotAmount = random.nextInt((int) Math.pow(2, collisionCount));
        for (int i = 0; i < timeSlotAmount; i++) {
            serialPortManager.delay(TIME_SLOT_MS);
        }
    }
}
