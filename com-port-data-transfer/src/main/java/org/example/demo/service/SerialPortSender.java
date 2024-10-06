package org.example.demo.service;

import static org.example.demo.service.SerialPortConstants.*;

import com.fazecast.jSerialComm.SerialPort;
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
        SerialPort sendPort
    ) {
        String message = inputField.getText();
        byte[] messageBytes = message.getBytes();
        int totalChunks = (int) Math.ceil(
            (double) messageBytes.length / CHUNK_SIZE
        );

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

            byte[] data = messagePackage.getBytes();
            byte fcs = (byte) SerialPortManager.calculateCRC8(chunk);
            int bytesWritten = sendPort.writeBytes(data, data.length);
            sendPort.writeBytes(new byte[] { fcs }, 1);
            if (bytesWritten <= 0) {
                errorUI.showErrorDialog("Failed to write data.");
            }
        }

        inputField.clear();
    }

    private String constructMessagePackage(
        byte[] chunk,
        int sourceAddress,
        int destinationAddress
    ) {
        String chunkMessage = new String(chunk);
        String replacedMessage = chunkMessage.replace(
            FLAG,
            ESC + String.valueOf(BYTE_STUFFING)
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
}
