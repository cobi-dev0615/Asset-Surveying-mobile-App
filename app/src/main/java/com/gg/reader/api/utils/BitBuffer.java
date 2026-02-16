package com.gg.reader.api.utils;

/**
 * Stub for vendor RFID library class.
 */
public class BitBuffer {
    private byte[] data = new byte[256];
    private int pos = 0;

    public static BitBuffer allocateDynamic() {
        return new BitBuffer();
    }

    public void put(int value) {
        if (pos + 4 <= data.length) {
            data[pos]     = (byte) ((value >> 24) & 0xFF);
            data[pos + 1] = (byte) ((value >> 16) & 0xFF);
            data[pos + 2] = (byte) ((value >> 8) & 0xFF);
            data[pos + 3] = (byte) (value & 0xFF);
            pos += 4;
        }
    }

    public void position(int newPos) {
        this.pos = newPos / 8;
    }

    public void get(byte[] dst) {
        int len = Math.min(dst.length, data.length - pos);
        if (len > 0) {
            System.arraycopy(data, pos, dst, 0, len);
        }
    }
}
