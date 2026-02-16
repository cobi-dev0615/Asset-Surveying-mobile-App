package com.gg.reader.api.utils;

/**
 * Stub for vendor RFID library class.
 */
public class HexUtils {
    public static String bytes2HexString(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString().toUpperCase();
    }
}
