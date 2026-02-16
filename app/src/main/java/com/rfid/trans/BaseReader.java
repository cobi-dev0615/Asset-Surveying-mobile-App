package com.rfid.trans;

import android.util.Log;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.vectordrawable.graphics.drawable.PathInterpolatorCompat;
import kotlin.UByte;

/* loaded from: classes3.dex */
public class BaseReader {
    private TagCallback callback;
    private MessageTran msg = new MessageTran();
    private long maxScanTime = 2000;
    private int[] recvLength = new int[1];
    private byte[] recvBuff = new byte[AccessibilityNodeInfoCompat.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_MAX_LENGTH];
    private int logswitch = 0;
    private String resultError = "";

    private void getCRC(byte[] bArr, int i) {
        int i2 = 65535;
        int i3 = 0;
        while (i3 < i) {
            try {
                i2 ^= bArr[i3] & UByte.MAX_VALUE;
                for (int i4 = 0; i4 < 8; i4++) {
                    i2 = (i2 & 1) != 0 ? (i2 >> 1) ^ 33800 : i2 >> 1;
                }
                i3++;
            } catch (Exception unused) {
                return;
            }
        }
        bArr[i3] = (byte) (i2 & 255);
        bArr[i3 + 1] = (byte) ((i2 >> 8) & 255);
    }

    private boolean CheckCRC(byte[] bArr, int i) {
        try {
            byte[] bArr2 = new byte[256];
            System.arraycopy(bArr, 0, bArr2, 0, i);
            getCRC(bArr2, i);
            if (bArr2[i + 1] == 0) {
                if (bArr2[i] == 0) {
                    return true;
                }
            }
        } catch (Exception unused) {
        }
        return false;
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

    public void SetCallBack(TagCallback tagCallback) {
        this.callback = tagCallback;
    }

    public int Connect(String str, int i, int i2) {
        this.logswitch = i2;
        return this.msg.open(str, i);
    }

    public int DisConnect() {
        return this.msg.close();
    }

    private int SendCMD(byte[] bArr) {
        if (this.logswitch == 1) {
            Log.d("Send", bytesToHexString(bArr, 0, (bArr[0] & UByte.MAX_VALUE) + 1));
        }
        return this.msg.Write(bArr);
    }

    private int GetCMDData(byte[] bArr, int[] iArr, int i, int i2) {
        int length;
        int i3;
        byte[] bArr2 = new byte[2000];
        long currentTimeMillis = System.currentTimeMillis();
        while (true) {
            int i4 = 0;
            while (System.currentTimeMillis() - currentTimeMillis < i2) {
                try {
                    byte[] Read = this.msg.Read();
                    if (Read != null && (length = Read.length) != 0) {
                        int i5 = length + i4;
                        byte[] bArr3 = new byte[i5];
                        System.arraycopy(bArr2, 0, bArr3, 0, i4);
                        System.arraycopy(Read, 0, bArr3, i4, length);
                        int i6 = 0;
                        while (true) {
                            i3 = i5 - i6;
                            if (i3 <= 4) {
                                break;
                            }
                            int i7 = bArr3[i6];
                            if ((i7 & 255) >= 4) {
                                byte b = bArr3[i6 + 2];
                                if ((b & UByte.MAX_VALUE) == i || ((bArr3[i6 + 3] & UByte.MAX_VALUE) == 254 && (b & UByte.MAX_VALUE) == 0)) {
                                    int i8 = i7 & 255;
                                    if (i5 < i6 + i8 + 1) {
                                        break;
                                    }
                                    int i9 = i8 + 1;
                                    byte[] bArr4 = new byte[i9];
                                    System.arraycopy(bArr3, i6, bArr4, 0, i9);
                                    if (CheckCRC(bArr4, i9)) {
                                        System.arraycopy(bArr4, 0, bArr, 0, i9);
                                        iArr[0] = i8 + 2;
                                        return 0;
                                    }
                                }
                            }
                            i6++;
                        }
                        if (i5 > i6) {
                            System.arraycopy(bArr3, i6, bArr2, 0, i3);
                            i4 = i3;
                        }
                    }
                } catch (Exception e) {
                    e.toString();
                    return 48;
                }
            }
            return 48;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:73:0x010e, code lost:
    
        if (r25[0] <= 0) goto L57;
     */
    /* JADX WARN: Code restructure failed: missing block: B:74:0x0110, code lost:
    
        return 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:75:0x0111, code lost:
    
        return r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:86:0x0153, code lost:
    
        if (r10 <= r6) goto L75;
     */
    /* JADX WARN: Code restructure failed: missing block: B:87:0x0155, code lost:
    
        r0 = 0;
        java.lang.System.arraycopy(r11, r6, r2, 0, r8);
        r6 = r8;
     */
    /* JADX WARN: Code restructure failed: missing block: B:97:0x015b, code lost:
    
        r0 = 0;
        r6 = 0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private int GetInventoryData(byte r22, int r23, byte[] r24, int[] r25, int[] r26, int r27) {
        /*
            Method dump skipped, instructions count: 389
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.rfid.trans.BaseReader.GetInventoryData(byte, int, byte[], int[], int[], int):int");
    }

    public int GetReaderInformation(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, byte[] bArr6, byte[] bArr7, byte[] bArr8, byte[] bArr9, byte[] bArr10, byte[] bArr11, byte[] bArr12, byte[] bArr13) {
        byte[] bArr14 = {4, bArr[0], 33, 0, 0};
        getCRC(bArr14, 4 - 1);
        SendCMD(bArr14);
        if (GetCMDData(this.recvBuff, this.recvLength, 33, 1000) != 0) {
            return 48;
        }
        byte[] bArr15 = this.recvBuff;
        bArr[0] = bArr15[1];
        bArr2[0] = bArr15[4];
        bArr2[1] = bArr15[5];
        bArr3[0] = bArr15[6];
        bArr4[0] = bArr15[7];
        byte b = bArr15[8];
        bArr6[0] = (byte) (b & 63);
        byte b2 = bArr15[9];
        bArr7[0] = (byte) (b2 & 63);
        bArr5[0] = (byte) (((b2 & 192) >> 6) | ((b & 192) >> 4));
        bArr8[0] = bArr15[10];
        bArr9[0] = bArr15[11];
        this.maxScanTime = (r1 & UByte.MAX_VALUE) * 100;
        bArr10[0] = bArr15[12];
        bArr11[0] = bArr15[13];
        bArr12[0] = bArr15[14];
        bArr13[0] = bArr15[15];
        return 0;
    }

    public int SetInventoryScanTime(byte b, byte b2) {
        byte[] bArr = {5, b, 37, b2, 0, 0};
        getCRC(bArr, 5 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 37, 500) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int SetPowerMode(byte b, byte[] bArr) {
        byte[] bArr2 = {5, b, 107, bArr[0], 0, 0};
        getCRC(bArr2, 5 - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 107, 500) != 0) {
            return 48;
        }
        if (bArr[0] == 0) {
            bArr[0] = this.recvBuff[4];
        }
        return this.recvBuff[3] & UByte.MAX_VALUE;
    }

    public int Inventory_G2(byte b, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, int i, byte[] bArr, int[] iArr, int[] iArr2) {
        byte[] bArr2;
        if (b5 == 0) {
            bArr2 = new byte[]{9, b, 1, b2, b3, b6, b7, (byte) i, 0, 0};
        } else {
            bArr2 = new byte[]{11, b, 1, b2, b3, b4, b5, b6, b7, (byte) i, 0, 0};
        }
        getCRC(bArr2, bArr2[0] - 1);
        SendCMD(bArr2);
        return GetInventoryData(b, 1, bArr, iArr, iArr2, i);
    }

    public int SetRfPower(byte b, byte b2) {
        byte[] bArr = {5, b, 47, b2, 0, 0};
        getCRC(bArr, 5 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 47, 500) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int RfOutput(byte b, byte b2) {
        byte[] bArr = {5, b, 48, b2, 0, 0};
        getCRC(bArr, 5 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 48, 1000) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int SetPowerMode(byte b, byte b2) {
        byte[] bArr = {5, b, 107, b2, 0, 0};
        getCRC(bArr, 5 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 107, 1000) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int SetAddress(byte b, byte b2) {
        byte[] bArr = {5, b, 36, b2, 0, 0};
        getCRC(bArr, 5 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 36, 500) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int SetRegion(byte b, int i, int i2, int i3) {
        byte[] bArr = {6, b, 34, (byte) (((i & 12) << 4) | (i2 & 63)), (byte) (((i & 3) << 6) | (i3 & 63)), 0, 0};
        getCRC(bArr, 6 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 34, 500) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int SetAntennaMultiplexing(byte b, byte b2, byte b3, byte b4) {
        byte[] bArr = {7, b, 63, b2, b3, b4, 0, 0};
        getCRC(bArr, 7 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 63, 500) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int ConfigDRM(byte b, byte[] bArr) {
        byte[] bArr2 = {5, b, -112, bArr[0], 0, 0};
        getCRC(bArr2, 5 - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 144, 400) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        byte b2 = bArr3[3];
        if (b2 == 0) {
            bArr[0] = bArr3[4];
        }
        return b2 & UByte.MAX_VALUE;
    }

    public int SetBeepNotification(byte b, byte b2) {
        byte[] bArr = {5, b, 64, b2, 0, 0};
        getCRC(bArr, 5 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 64, 400) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int ReadData_G2(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte b5, byte[] bArr2, byte b6, byte[] bArr3, byte b7, byte[] bArr4, byte b8, byte[] bArr5, byte[] bArr6) {
        int i;
        byte b9;
        byte b10 = b2;
        if (b8 == 0) {
            int i2 = b10 * 2;
            byte[] bArr7 = new byte[i2 + 13];
            bArr7[0] = (byte) (i2 + 12);
            bArr7[1] = b;
            bArr7[2] = 2;
            bArr7[3] = b10;
            System.arraycopy(bArr, 0, bArr7, 4, i2);
            bArr7[i2 + 4] = b3;
            bArr7[i2 + 5] = b4;
            bArr7[i2 + 6] = b5;
            System.arraycopy(bArr2, 0, bArr7, i2 + 7, 4);
            getCRC(bArr7, bArr7[0] - 1);
            SendCMD(bArr7);
            if (GetCMDData(this.recvBuff, this.recvLength, 2, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
                return 48;
            }
            byte[] bArr8 = this.recvBuff;
            byte b11 = bArr8[3];
            if (b11 == 0) {
                bArr6[0] = 0;
                System.arraycopy(bArr8, 4, bArr5, 0, b5 * 2);
            } else if ((b11 & UByte.MAX_VALUE) == 252) {
                bArr6[0] = bArr8[4];
            }
            b9 = this.recvBuff[3];
        } else {
            if (b7 == 0) {
                return 255;
            }
            int i3 = b7 & UByte.MAX_VALUE;
            if (i3 % 8 == 0) {
                i = i3 / 8;
            } else {
                i = (i3 / 8) + 1;
            }
            byte[] bArr9 = new byte[i + 17];
            bArr9[0] = (byte) (i + 16);
            bArr9[1] = b;
            bArr9[2] = 2;
            bArr9[3] = b10;
            if ((b10 & UByte.MAX_VALUE) == 255) {
                b10 = 0;
            }
            int i4 = b10 * 2;
            System.arraycopy(bArr, 0, bArr9, 4, i4);
            bArr9[i4 + 4] = b3;
            bArr9[i4 + 5] = b4;
            bArr9[i4 + 6] = b5;
            System.arraycopy(bArr2, 0, bArr9, i4 + 7, 4);
            bArr9[i4 + 11] = b6;
            bArr9[i4 + 12] = bArr3[0];
            bArr9[i4 + 13] = bArr3[1];
            bArr9[i4 + 14] = b7;
            System.arraycopy(bArr4, 0, bArr9, i4 + 15, i);
            getCRC(bArr9, bArr9[0] - 1);
            SendCMD(bArr9);
            if (GetCMDData(this.recvBuff, this.recvLength, 2, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
                return 48;
            }
            byte[] bArr10 = this.recvBuff;
            byte b12 = bArr10[3];
            if (b12 == 0) {
                bArr6[0] = 0;
                System.arraycopy(bArr10, 4, bArr5, 0, b5 * 2);
            } else if ((b12 & UByte.MAX_VALUE) == 252) {
                bArr6[0] = bArr10[4];
            }
            b9 = this.recvBuff[3];
        }
        return b9 & UByte.MAX_VALUE;
    }

    public int ExtReadData_G2(byte b, byte b2, byte[] bArr, byte b3, byte[] bArr2, byte b4, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte b5 = b2;
        int i = b5 * 2;
        byte[] bArr6 = new byte[i + 14];
        bArr6[0] = (byte) (i + 13);
        bArr6[1] = b;
        bArr6[2] = 21;
        bArr6[3] = b5;
        if ((b5 & UByte.MAX_VALUE) == 255) {
            b5 = 0;
        }
        int i2 = b5 * 2;
        System.arraycopy(bArr, 0, bArr6, 4, i2);
        bArr6[i2 + 4] = b3;
        bArr6[i2 + 5] = bArr2[0];
        bArr6[i2 + 6] = bArr2[1];
        bArr6[i2 + 7] = b4;
        System.arraycopy(bArr3, 0, bArr6, i2 + 8, 4);
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 21, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr7 = this.recvBuff;
        byte b6 = bArr7[3];
        if (b6 == 0) {
            bArr5[0] = 0;
            System.arraycopy(bArr7, 4, bArr4, 0, b4 * 2);
        } else if ((b6 & UByte.MAX_VALUE) == 252) {
            bArr5[0] = bArr7[4];
        }
        return this.recvBuff[3] & UByte.MAX_VALUE;
    }

    public int WriteData_G2(byte b, byte b2, byte b3, byte[] bArr, byte b4, byte b5, byte[] bArr2, byte[] bArr3, byte b6, byte[] bArr4, byte b7, byte[] bArr5, byte b8, byte[] bArr6) {
        int i;
        byte b9 = b3;
        if (b8 == 0) {
            int i2 = (b9 + b2) * 2;
            byte[] bArr7 = new byte[i2 + 13];
            bArr7[0] = (byte) (i2 + 12);
            bArr7[1] = b;
            bArr7[2] = 3;
            bArr7[3] = b2;
            bArr7[4] = b9;
            int i3 = b9 * 2;
            System.arraycopy(bArr, 0, bArr7, 5, i3);
            bArr7[i3 + 5] = b4;
            bArr7[i3 + 6] = b5;
            int i4 = b2 * 2;
            System.arraycopy(bArr2, 0, bArr7, i3 + 7, i4);
            System.arraycopy(bArr3, 0, bArr7, i3 + i4 + 7, 4);
            getCRC(bArr7, bArr7[0] - 1);
            SendCMD(bArr7);
            if (GetCMDData(this.recvBuff, this.recvLength, 3, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
                return 48;
            }
            byte[] bArr8 = this.recvBuff;
            byte b10 = bArr8[3];
            if (b10 == 0) {
                bArr6[0] = 0;
            } else if ((b10 & UByte.MAX_VALUE) == 252) {
                bArr6[0] = bArr8[4];
            }
            return b10 & UByte.MAX_VALUE;
        }
        if (b7 == 0) {
            return 255;
        }
        int i5 = b7 & UByte.MAX_VALUE;
        if (i5 % 8 == 0) {
            i = i5 / 8;
        } else {
            i = (i5 / 8) + 1;
        }
        int i6 = b2 * 2;
        byte[] bArr9 = new byte[i6 + 17 + i];
        bArr9[0] = (byte) (i6 + 16 + i);
        bArr9[1] = b;
        bArr9[2] = 3;
        bArr9[3] = b2;
        bArr9[4] = b9;
        if ((b9 & UByte.MAX_VALUE) == 255) {
            b9 = 0;
        }
        int i7 = b9 * 2;
        System.arraycopy(bArr, 0, bArr9, 5, i7);
        bArr9[i7 + 5] = b4;
        bArr9[i7 + 6] = b5;
        System.arraycopy(bArr2, 0, bArr9, i7 + 7, i6);
        int i8 = i7 + i6;
        System.arraycopy(bArr3, 0, bArr9, i8 + 7, 4);
        bArr9[i8 + 11] = b6;
        bArr9[i8 + 12] = bArr4[0];
        bArr9[i8 + 13] = bArr4[1];
        bArr9[i8 + 14] = b7;
        System.arraycopy(bArr5, 0, bArr9, i8 + 15, i);
        getCRC(bArr9, bArr9[0] - 1);
        SendCMD(bArr9);
        if (GetCMDData(this.recvBuff, this.recvLength, 3, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr10 = this.recvBuff;
        byte b11 = bArr10[3];
        if (b11 == 0) {
            bArr6[0] = 0;
        } else if ((b11 & UByte.MAX_VALUE) == 252) {
            bArr6[0] = bArr10[4];
        }
        return b11 & UByte.MAX_VALUE;
    }

    public int ExtWriteData_G2(byte b, byte b2, byte b3, byte[] bArr, byte b4, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte b5 = b3;
        int i = (b5 + b2) * 2;
        byte[] bArr6 = new byte[i + 14];
        bArr6[0] = (byte) (i + 13);
        bArr6[1] = b;
        bArr6[2] = 22;
        bArr6[3] = b2;
        bArr6[4] = b5;
        if ((b5 & UByte.MAX_VALUE) == 255) {
            b5 = 0;
        }
        int i2 = b5 * 2;
        System.arraycopy(bArr, 0, bArr6, 5, i2);
        bArr6[i2 + 5] = b4;
        bArr6[i2 + 6] = bArr2[0];
        bArr6[i2 + 7] = bArr2[1];
        int i3 = 2 * b2;
        System.arraycopy(bArr3, 0, bArr6, i2 + 8, i3);
        System.arraycopy(bArr4, 0, bArr6, i2 + i3 + 9, 4);
        getCRC(bArr6, bArr6[0] - 1);
        SendCMD(bArr6);
        if (GetCMDData(this.recvBuff, this.recvLength, 22, PathInterpolatorCompat.MAX_NUM_POINTS) != 0) {
            return 48;
        }
        byte[] bArr7 = this.recvBuff;
        byte b6 = bArr7[3];
        if (b6 == 0) {
            bArr5[0] = 0;
        } else if ((b6 & UByte.MAX_VALUE) == 252) {
            bArr5[0] = bArr7[4];
        }
        return b6 & UByte.MAX_VALUE;
    }

    public int WriteEPC_G2(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int i = b2 * 2;
        byte[] bArr4 = new byte[i + 10];
        bArr4[0] = (byte) (i + 9);
        bArr4[1] = b;
        bArr4[2] = 4;
        bArr4[3] = b2;
        System.arraycopy(bArr, 0, bArr4, 4, 4);
        System.arraycopy(bArr2, 0, bArr4, 8, i);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 4, 2000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        byte b3 = bArr5[3];
        if (b3 == 0) {
            bArr3[0] = 0;
        } else if ((b3 & UByte.MAX_VALUE) == 252) {
            bArr3[0] = bArr5[4];
        }
        return b3 & UByte.MAX_VALUE;
    }

    public int Lock_G2(byte b, byte b2, byte[] bArr, byte b3, byte b4, byte[] bArr2, byte[] bArr3) {
        int i = b2 * 2;
        byte[] bArr4 = new byte[i + 12];
        bArr4[0] = (byte) (i + 11);
        bArr4[1] = b;
        bArr4[2] = 6;
        bArr4[3] = b2;
        System.arraycopy(bArr, 0, bArr4, 4, i);
        bArr4[i + 4] = b3;
        bArr4[i + 5] = b4;
        System.arraycopy(bArr2, 0, bArr4, i + 6, 4);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 6, 1000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        byte b5 = bArr5[3];
        if (b5 == 0) {
            bArr3[0] = 0;
        } else if ((b5 & UByte.MAX_VALUE) == 252) {
            bArr3[0] = bArr5[4];
        }
        return b5 & UByte.MAX_VALUE;
    }

    public int Kill_G2(byte b, byte b2, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int i = b2 * 2;
        byte[] bArr4 = new byte[i + 10];
        bArr4[0] = (byte) (i + 11);
        bArr4[1] = b;
        bArr4[2] = 5;
        bArr4[3] = b2;
        System.arraycopy(bArr, 0, bArr4, 4, i);
        System.arraycopy(bArr2, 0, bArr4, i + 4, 4);
        getCRC(bArr4, bArr4[0] - 1);
        SendCMD(bArr4);
        if (GetCMDData(this.recvBuff, this.recvLength, 6, 1000) != 0) {
            return 48;
        }
        byte[] bArr5 = this.recvBuff;
        byte b3 = bArr5[3];
        if (b3 == 0) {
            bArr3[0] = 0;
        } else if ((b3 & UByte.MAX_VALUE) == 252) {
            bArr3[0] = bArr5[4];
        }
        return b3 & UByte.MAX_VALUE;
    }

    public int MeasureReturnLoss(byte b, byte[] bArr, byte b2, byte[] bArr2) {
        byte[] bArr3 = new byte[10];
        bArr3[0] = 9;
        bArr3[1] = b;
        bArr3[2] = -111;
        System.arraycopy(bArr, 0, bArr3, 3, 4);
        bArr3[7] = b2;
        getCRC(bArr3, bArr3[0] - 1);
        SendCMD(bArr3);
        if (GetCMDData(this.recvBuff, this.recvLength, 145, 600) != 0) {
            return 48;
        }
        byte[] bArr4 = this.recvBuff;
        byte b3 = bArr4[3];
        if (b3 == 0) {
            bArr2[0] = bArr4[4];
        }
        return b3 & UByte.MAX_VALUE;
    }

    public int MeasureTemperature(byte b, byte[] bArr) {
        byte[] bArr2 = {4, b, -110, 0, 0};
        getCRC(bArr2, 4 - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 146, 600) != 0) {
            return 48;
        }
        byte[] bArr3 = this.recvBuff;
        byte b2 = bArr3[3];
        if (b2 == 0) {
            bArr[0] = bArr3[4];
            bArr[1] = bArr3[5];
        }
        return b2 & UByte.MAX_VALUE;
    }

    public int SetCheckAnt(byte b, byte b2) {
        byte[] bArr = {5, b, 102, b2, 0, 0};
        getCRC(bArr, 5 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 102, 500) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int SetReadParameter(byte b, byte[] bArr) {
        byte[] bArr2 = {9, b, 117, bArr[0], bArr[1], bArr[2], bArr[3], bArr[4], 0, 0};
        getCRC(bArr2, 9 - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 117, 500) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int GetReadParameter(byte b, byte[] bArr) {
        byte[] bArr2 = {4, b, 119, 0, 0};
        getCRC(bArr2, 4 - 1);
        SendCMD(bArr2);
        if (GetCMDData(this.recvBuff, this.recvLength, 119, 300) != 0) {
            return 48;
        }
        System.arraycopy(this.recvBuff, 4, bArr, 0, 6);
        return this.recvBuff[3] & UByte.MAX_VALUE;
    }

    public int SetWorkMode(byte b, byte b2) {
        byte[] bArr = {5, b, 118, b2, 0, 0};
        getCRC(bArr, 5 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 118, 1000) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }

    public int SetBaudRate(byte b, byte b2) {
        byte[] bArr = {5, b, 40, b2, 0, 0};
        getCRC(bArr, 5 - 1);
        SendCMD(bArr);
        if (GetCMDData(this.recvBuff, this.recvLength, 40, 1000) == 0) {
            return this.recvBuff[3] & UByte.MAX_VALUE;
        }
        return 48;
    }
}
