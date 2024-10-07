package org.example.demo.service;

import static org.example.demo.service.SerialPortConstants.*;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Objects;
import javafx.scene.control.ComboBox;

public class SerialPortManager {

    public boolean arePortsInvalid(
        ComboBox<String> portSendField,
        ComboBox<String> portReceiveField
    ) {
        return (
            Objects.equals(portSendField.getValue(), "N/A") ||
            Objects.equals(portReceiveField.getValue(), "N/A") ||
            Objects.equals(
                portSendField.getValue(),
                portReceiveField.getValue()
            )
        );
    }

    public SerialPort openPort(String portName, int parity) {
        SerialPort port = SerialPort.getCommPort(portName);
        if (port.openPort()) {
            port.setComPortParameters(9600, 8, 1, parity);
            return port;
        }
        return null;
    }

    public void closePorts(SerialPort sendPort, SerialPort receivePort) {
        sendPort.closePort();
        receivePort.closePort();
    }

    public int getPortAddress(ComboBox<String> portField) {
        return Integer.parseInt(
            String.valueOf(
                portField.getValue().charAt(portField.getValue().length() - 1)
            )
        );
    }

    public static int calculateCRC8(byte[] data) {
        int crc = 0x00;
        for (byte b : data) {
            crc ^= (b & 0xFF);

            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc & 0xFF;
    }
}
