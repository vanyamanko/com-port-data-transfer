package org.example.demo.handler;

import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;
import org.example.demo.service.SerialPortManager;
import org.example.demo.service.SerialPortReceiver;
import org.example.demo.service.SerialPortSender;
import org.example.demo.ui.ErrorUI;

public class CommunicationHandler {

    private final ErrorUI errorUI = new ErrorUI();
    private final SerialPortManager serialPortManager = new SerialPortManager();
    private final SerialPortSender serialPortSender = new SerialPortSender();
    private final SerialPortReceiver serialPortReceiver =
        new SerialPortReceiver();

    public void handleEnter(
        TextField inputField,
        ComboBox<String> portSendField,
        ComboBox<String> portReceiveField,
        TextArea outputArea,
        TextFlow infoArea,
        int parity
    ) {
        if (
            serialPortManager.arePortsInvalid(portSendField, portReceiveField)
        ) {
            inputField.clear();
            errorUI.showErrorDialog("Select the correct ports.");
            return;
        }

        SerialPort sendPort = serialPortManager.openPort(
            "/dev/" + portSendField.getValue(),
            parity
        );
        SerialPort receivePort = serialPortManager.openPort(
            "/dev/" + portReceiveField.getValue(),
            parity
        );

        if (sendPort != null || receivePort != null) {
            serialPortSender.processSendingData(
                inputField,
                portSendField,
                portReceiveField,
                sendPort,
                receivePort
            );
            serialPortReceiver.processReceivingData(
                receivePort,
                outputArea,
                infoArea,
                portReceiveField
            );

            serialPortManager.closePorts(sendPort, receivePort);
        } else {
            inputField.clear();
            errorUI.showErrorDialog("Runtime error.");
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
