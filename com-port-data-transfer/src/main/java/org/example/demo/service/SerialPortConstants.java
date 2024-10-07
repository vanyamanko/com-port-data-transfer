package org.example.demo.service;

public final class SerialPortConstants {

    private SerialPortConstants() {}

    public static final String FLAG = "$p";
    public static final int POLYNOMIAL = 0x107;
    public static final int CHUNK_SIZE = 16;
    public static final char ESC = '\u001B';
    public static final char BYTE_STUFFING = 0x01;
    public static final String ESC_BYTE_STUFFING = "ESC0x01";
}
