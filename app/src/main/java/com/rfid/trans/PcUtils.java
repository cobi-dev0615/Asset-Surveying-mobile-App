package com.rfid.trans;

import com.gg.reader.api.utils.BitBuffer;
import com.gg.reader.api.utils.HexUtils;
import kotlin.UByte;

/* loaded from: classes3.dex */
public class PcUtils {
    public static String getPc(int i) {
        BitBuffer allocateDynamic = BitBuffer.allocateDynamic();
        allocateDynamic.put(i << 11);
        allocateDynamic.position(16);
        byte[] bArr = new byte[2];
        allocateDynamic.get(bArr);
        return HexUtils.bytes2HexString(bArr);
    }

    public static String getGbPc(int i) {
        BitBuffer allocateDynamic = BitBuffer.allocateDynamic();
        allocateDynamic.put(i << 8);
        allocateDynamic.position(16);
        byte[] bArr = new byte[2];
        allocateDynamic.get(bArr);
        return HexUtils.bytes2HexString(bArr);
    }

    public static String padLeft(String str, int i, char c) {
        if (i - str.length() <= 0) {
            return str;
        }
        char[] cArr = new char[i];
        System.arraycopy(str.toCharArray(), 0, cArr, 0, str.length());
        for (int length = str.length(); length < i; length++) {
            cArr[length] = c;
        }
        return new String(cArr);
    }

    public static int getValueLen(String str) {
        String trim = str.trim();
        return trim.length() % 4 == 0 ? trim.length() / 4 : (trim.length() / 4) + 1;
    }

    public static int getTwoValueLen(String str) {
        String trim = str.trim();
        return trim.length() % 2 == 0 ? trim.length() / 2 : (trim.length() / 2) + 1;
    }

    public static int byteArrayToInt(byte[] bArr) {
        return ((bArr[0] & UByte.MAX_VALUE) << 24) | (bArr[3] & UByte.MAX_VALUE) | ((bArr[2] & UByte.MAX_VALUE) << 8) | ((bArr[1] & UByte.MAX_VALUE) << 16);
    }

    public static byte[] intToByteArray(int i) {
        return new byte[]{(byte) ((i >> 24) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 8) & 255), (byte) (i & 255)};
    }
}
