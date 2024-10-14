package org.example.demo.service;

public final class SerialPortConstants {

    private SerialPortConstants() {}

    public static final String FLAG = "$p";
    public static final char JAM_SIGNAL = 0xAA;
    public static final int POLYNOMIAL = 0x107;
    public static final int CHUNK_SIZE = 16;
    public static final char ESC = '\u001B';
    public static final char BYTE_STUFFING_FLAG = 0x01;
    public static final char BYTE_STUFFING_JAM_SIGNAL = 0x02;
    public static final String ESC_BYTE_STUFFING_FLAG = "ESC0x01";
    public static final double PROBABILITY_OF_COLLISION = 0.01;
    public static final double TIME_SLOT_MS = 5;
    public static final int MAX_NUMBER_OF_COLLISION = 16;
    public static final int MAX_QUANTITY_TO_INCREASE_INTERVAL = 10;
}
