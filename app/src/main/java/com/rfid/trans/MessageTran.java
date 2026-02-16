package com.rfid.trans;

import android.util.Log;
import com.rfid.serialport.SerialPort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import kotlin.UByte;

/* loaded from: classes3.dex */
public class MessageTran {
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;
    private SerialPort mSerialPort = null;
    private boolean connected = false;

    public boolean isOpen() {
        return this.connected;
    }

    public int open(String str, int i) {
        try {
            this.mSerialPort = new SerialPort(new File(str), i, 0);
        } catch (IOException | SecurityException | InvalidParameterException unused) {
        }
        SerialPort serialPort = this.mSerialPort;
        if (serialPort == null) {
            return -1;
        }
        this.mInStream = serialPort.getInputStream();
        this.mOutStream = this.mSerialPort.getOutputStream();
        this.connected = true;
        return 0;
    }

    public int close() {
        if (this.mInStream != null) {
            try {
                SerialPort serialPort = this.mSerialPort;
                if (serialPort != null) {
                    serialPort.close();
                    this.mSerialPort = null;
                }
                this.mInStream.close();
                this.mOutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.connected = false;
        return 0;
    }

    public byte[] Read() {
        if (!this.connected) {
            return null;
        }
        try {
            byte[] bArr = new byte[2560];
            int read = this.mInStream.read(bArr);
            if (read > 0) {
                byte[] bArr2 = new byte[read];
                System.arraycopy(bArr, 0, bArr2, 0, read);
                Log.d("Recv", bytesToHexString(bArr2, 0, read));
                return bArr2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int Write(byte[] bArr) {
        if (!this.connected) {
            return -1;
        }
        int length = bArr.length;
        int i = bArr[0];
        if (length != (i & 255) + 1) {
            return -1;
        }
        int i2 = (i & 255) + 1;
        try {
            byte[] bArr2 = new byte[i2];
            System.arraycopy(bArr, 0, bArr2, 0, i2);
            this.mOutStream.flush();
            this.mOutStream.write(bArr2);
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String bytesToHexString(byte[] bArr, int i, int i2) {
        StringBuilder sb = new StringBuilder("");
        if (bArr != null) {
            try {
                if (bArr.length > 0) {
                    while (i < i2) {
                        String hexString = Integer.toHexString(bArr[i] & UByte.MAX_VALUE);
                        if (hexString.length() == 1) {
                            sb.append(0);
                        }
                        sb.append(hexString);
                        i++;
                    }
                    return sb.toString().toUpperCase();
                }
            } catch (Exception unused) {
            }
        }
        return null;
    }

    public byte[] hexStringToBytes(String str) {
        if (str != null) {
            try {
                if (!str.equals("")) {
                    String upperCase = str.toUpperCase();
                    int length = upperCase.length() / 2;
                    char[] charArray = upperCase.toCharArray();
                    byte[] bArr = new byte[length];
                    for (int i = 0; i < length; i++) {
                        int i2 = i * 2;
                        bArr[i] = (byte) (charToByte(charArray[i2 + 1]) | (charToByte(charArray[i2]) << 4));
                    }
                    return bArr;
                }
            } catch (Exception unused) {
            }
        }
        return null;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
