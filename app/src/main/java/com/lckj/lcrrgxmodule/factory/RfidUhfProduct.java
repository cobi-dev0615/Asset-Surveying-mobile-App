package com.lckj.lcrrgxmodule.factory;

import android.media.SoundPool;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import com.rfid.InventoryTagMap;
import com.rfid.InventoryTagResult;
import com.rfid.trans.BaseReader;
import com.rfid.trans.ReadTag;
import com.rfid.trans.ReaderParameter;
import com.rfid.trans.TagCallback;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.ByteCompanionObject;

/* loaded from: classes3.dex */
public class RfidUhfProduct implements ILcUhfProduct {
    private static String devport = "/dev/ttyS3";
    private static String devport2 = "/dev/ttyS2";
    private long beginTime;
    private TagCallback callback;
    private long endtime;
    private long ttbegintime;
    public static List<InventoryTagMap> lsTagList = new ArrayList();
    public static List<InventoryTagResult> lsList = new ArrayList();
    private final String TAG = "bldAdd#RfidUhfProduct";
    private BaseReader reader = new BaseReader();
    private ReaderParameter param = new ReaderParameter();
    private volatile boolean mWorking = true;
    private volatile Thread mThread = null;
    private volatile boolean soundworking = true;
    private volatile boolean isSound = false;
    private volatile Thread sThread = null;
    private byte[] pOUcharIDList = new byte[25600];
    private volatile int NoCardCOunt = 0;
    private Integer soundid = null;
    private SoundPool soundPool = null;
    public String devName = "";
    private int CurrentNum = 0;

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int GetModuleVersion() {
        return 0;
    }

    public RfidUhfProduct() {
        this.param.ComAddr = (byte) -1;
        this.param.ScanTime = 20;
        this.param.Session = 0;
        this.param.QValue = 4;
        this.param.TidLen = 0;
        this.param.TidPtr = 0;
        this.param.Antenna = 128;
        this.param.Interval = 20;
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public void beginSound(boolean z) {
        this.isSound = z;
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public void setsoundid(int i, SoundPool soundPool) {
        this.soundid = Integer.valueOf(i);
        this.soundPool = soundPool;
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public void playSound() {
        SoundPool soundPool;
        Integer num = this.soundid;
        if (num == null || (soundPool = this.soundPool) == null) {
            return;
        }
        try {
            soundPool.play(num.intValue(), 1.0f, 1.0f, 1, 0, 1.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int Connect() {
        String str;
        if (Build.VERSION.SDK_INT == 28) {
            str = devport2;
        } else {
            str = devport;
        }
        return Connect(str, 115200);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int Connect(String str, int i) {
        int Connect = this.reader.Connect(str, i, 1);
        if (Connect == 0) {
            SystemClock.sleep(100L);
            Connect = GetUHFInformation(new byte[2], new byte[1], new byte[1], new byte[1], new byte[1], new byte[1], new byte[1]);
            if (Connect != 0) {
                this.reader.DisConnect();
            }
            this.devName = str;
            this.isSound = false;
            this.soundworking = true;
            this.sThread = new Thread(new Runnable() { // from class: com.lckj.lcrrgxmodule.factory.RfidUhfProduct$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    RfidUhfProduct.this.m653lambda$Connect$0$comlckjlcrrgxmodulefactoryRfidUhfProduct();
                }
            });
            this.sThread.start();
        }
        return Connect;
    }

    /* renamed from: lambda$Connect$0$com-lckj-lcrrgxmodule-factory-RfidUhfProduct, reason: not valid java name */
    /* synthetic */ void m653lambda$Connect$0$comlckjlcrrgxmodulefactoryRfidUhfProduct() {
        while (this.soundworking) {
            if (this.isSound) {
                playSound();
                SystemClock.sleep(50L);
            }
        }
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int DisConnect() {
        try {
            this.isSound = false;
            this.soundworking = false;
            this.sThread.interrupt();
            this.sThread = null;
            SoundPool soundPool = this.soundPool;
            if (soundPool != null) {
                soundPool.release();
                this.soundPool = null;
            }
            List<InventoryTagMap> list = lsTagList;
            if (list != null) {
                list.clear();
            }
            List<InventoryTagResult> list2 = lsList;
            if (list2 != null) {
                list2.clear();
            }
        } catch (Exception e) {
            Log.e(this.TAG, "DisConnect Exception=" + e.getMessage());
        }
        return this.reader.DisConnect();
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public void SetInventoryParameter(ReaderParameter readerParameter) {
        this.param = readerParameter;
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public ReaderParameter GetInventoryParameter() {
        return this.param;
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int GetUHFInformation(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, byte[] bArr6, byte[] bArr7) {
        byte[] bArr8 = {-1};
        int GetReaderInformation = this.reader.GetReaderInformation(bArr8, bArr, new byte[1], new byte[1], bArr3, bArr4, bArr5, bArr2, new byte[1], bArr7, bArr6, new byte[1], new byte[1]);
        if (GetReaderInformation == 0) {
            this.param.ComAddr = bArr8[0];
            this.param.Antenna = bArr7[0];
        }
        return GetReaderInformation;
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int SetRfPower(int i) {
        return this.reader.SetRfPower(this.param.ComAddr, (byte) i);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int SetRegion(int i, int i2, int i3) {
        return this.reader.SetRegion(this.param.ComAddr, i, i2, i3);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int RfOutput(byte b) {
        return this.reader.RfOutput(this.param.ComAddr, b);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int SetPowerMode(int i) {
        return this.reader.SetPowerMode(this.param.ComAddr, (byte) (i | 128));
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int MeasureTemperature(byte[] bArr) {
        return this.reader.MeasureTemperature(this.param.ComAddr, bArr);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int MeasureReturnLoss(byte[] bArr) {
        return this.reader.MeasureReturnLoss(this.param.ComAddr, new byte[]{0, 13, -9, 50}, (byte) 0, bArr);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int SetAntenna(byte b) {
        int SetAntennaMultiplexing = this.reader.SetAntennaMultiplexing(this.param.ComAddr, (byte) 1, (byte) 0, b);
        if (SetAntennaMultiplexing == 0) {
            this.param.Antenna = b;
        }
        return SetAntennaMultiplexing;
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int SetBeepNotification(int i) {
        return this.reader.SetBeepNotification(this.param.ComAddr, (byte) i);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int SetWorkMode(byte b) {
        return this.reader.SetWorkMode(this.param.ComAddr, b);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int SetBaudRate(byte b) {
        return this.reader.SetBaudRate(this.param.ComAddr, b);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public String ReadDataByEPC(String str, byte b, byte b2, byte b3, byte[] bArr) {
        if (str.length() % 4 != 0) {
            return "FF";
        }
        int i = b3 * 2;
        byte[] bArr2 = new byte[i];
        int ReadData_G2 = this.reader.ReadData_G2(this.param.ComAddr, (byte) (str.length() / 4), this.reader.hexStringToBytes(str), b, b2, b3, bArr, (byte) 0, new byte[2], (byte) 0, new byte[12], (byte) 0, bArr2, new byte[1]);
        return ReadData_G2 == 0 ? this.reader.bytesToHexString(bArr2, 0, i) : String.format("%2X", Integer.valueOf(ReadData_G2));
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public String ReadDataByTID(String str, byte b, byte b2, byte b3, byte[] bArr) {
        if (str.length() % 4 != 0) {
            return "FF";
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str);
        byte length = (byte) (str.length() * 4);
        byte[] bArr2 = new byte[str.length()];
        System.arraycopy(hexStringToBytes, 0, bArr2, 0, hexStringToBytes.length);
        int i = b3 * 2;
        byte[] bArr3 = new byte[i];
        int ReadData_G2 = this.reader.ReadData_G2(this.param.ComAddr, (byte) -1, new byte[12], b, b2, b3, bArr, (byte) 2, new byte[]{0, 0}, length, bArr2, (byte) 1, bArr3, new byte[1]);
        return ReadData_G2 == 0 ? this.reader.bytesToHexString(bArr3, 0, i) : String.format("%2X", Integer.valueOf(ReadData_G2));
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int WriteDataByEPC(String str, byte b, byte b2, byte[] bArr, String str2) {
        if (str.length() % 4 != 0 || str2.length() % 4 != 0) {
            return 255;
        }
        byte length = (byte) (str.length() / 4);
        return this.reader.WriteData_G2(this.param.ComAddr, (byte) (str2.length() / 4), length, this.reader.hexStringToBytes(str), b, b2, this.reader.hexStringToBytes(str2), bArr, (byte) 0, new byte[2], (byte) 0, new byte[12], (byte) 0, new byte[1]);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int WriteDataByTID(String str, byte b, byte b2, byte[] bArr, String str2) {
        if (str.length() % 4 != 0 || str2.length() % 4 != 0) {
            return 255;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str2);
        byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
        byte[] bArr2 = {0, 0};
        byte length = (byte) (str.length() * 4);
        byte[] bArr3 = new byte[str.length()];
        System.arraycopy(hexStringToBytes2, 0, bArr3, 0, hexStringToBytes2.length);
        return this.reader.WriteData_G2(this.param.ComAddr, (byte) (str2.length() / 4), (byte) -1, new byte[12], b, b2, hexStringToBytes, bArr, (byte) 2, bArr2, length, bArr3, (byte) 1, new byte[1]);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int WriteEPCByTID(String str, String str2, byte[] bArr) {
        String str3;
        if (str.length() % 4 != 0 || str2.length() % 4 != 0) {
            return 255;
        }
        byte length = (byte) (str2.length() / 4);
        byte[] bArr2 = new byte[12];
        switch (length) {
            case 1:
                str3 = "0800";
                break;
            case 2:
                str3 = "1000";
                break;
            case 3:
                str3 = "1800";
                break;
            case 4:
                str3 = "2000";
                break;
            case 5:
                str3 = "2800";
                break;
            case 6:
                str3 = "3000";
                break;
            case 7:
                str3 = "3800";
                break;
            case 8:
                str3 = "4000";
                break;
            case 9:
                str3 = "4800";
                break;
            case 10:
                str3 = "5000";
                break;
            case 11:
                str3 = "5800";
                break;
            case 12:
                str3 = "6000";
                break;
            case 13:
                str3 = "6800";
                break;
            case 14:
                str3 = "7000";
                break;
            case 15:
                str3 = "7800";
                break;
            case 16:
                str3 = "8000";
                break;
            default:
                str3 = "";
                break;
        }
        byte[] hexStringToBytes = this.reader.hexStringToBytes(str3 + str2);
        byte[] hexStringToBytes2 = this.reader.hexStringToBytes(str);
        byte[] bArr3 = {0, 0};
        byte length2 = (byte) (str.length() * 4);
        byte[] bArr4 = new byte[str.length()];
        System.arraycopy(hexStringToBytes2, 0, bArr4, 0, hexStringToBytes2.length);
        return this.reader.WriteData_G2(this.param.ComAddr, length, (byte) -1, bArr2, (byte) 1, (byte) 1, hexStringToBytes, bArr, (byte) 2, bArr3, length2, bArr4, (byte) 1, new byte[1]);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int Lock(String str, byte b, byte b2, String str2) {
        if (str.length() % 4 != 0 || str2.length() != 8) {
            return 255;
        }
        return this.reader.Lock_G2(this.param.ComAddr, (byte) (str.length() / 4), this.reader.hexStringToBytes(str), b, b2, this.reader.hexStringToBytes(str2), new byte[1]);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int Kill(String str, String str2) {
        if (str.length() % 4 != 0 || str2.length() != 8) {
            return 255;
        }
        return this.reader.Kill_G2(this.param.ComAddr, (byte) (str.length() / 4), this.reader.hexStringToBytes(str), this.reader.hexStringToBytes(str2), new byte[1]);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public void SetCallBack(TagCallback tagCallback) {
        this.callback = tagCallback;
        this.reader.SetCallBack(tagCallback);
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public int StartRead() {
        if (this.mThread != null) {
            return 1;
        }
        this.mWorking = true;
        this.mThread = new Thread(new Runnable() { // from class: com.lckj.lcrrgxmodule.factory.RfidUhfProduct$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                RfidUhfProduct.this.m654lambda$StartRead$1$comlckjlcrrgxmodulefactoryRfidUhfProduct();
            }
        });
        this.mThread.start();
        return 0;
    }

    /* renamed from: lambda$StartRead$1$com-lckj-lcrrgxmodule-factory-RfidUhfProduct, reason: not valid java name */
    /* synthetic */ void m654lambda$StartRead$1$comlckjlcrrgxmodulefactoryRfidUhfProduct() {
        this.CurrentNum = 0;
        this.ttbegintime = SystemClock.elapsedRealtime();
        byte b = 0;
        while (this.mWorking) {
            int[] iArr = {0};
            int[] iArr2 = {0};
            if (this.param.Session == 0 || this.param.Session == 1) {
                this.NoCardCOunt = 0;
                b = 0;
            }
            this.beginTime = SystemClock.elapsedRealtime();
            iArr2[0] = 0;
            int Inventory_G2 = this.reader.Inventory_G2(this.param.ComAddr, (byte) this.param.QValue, (byte) this.param.Session, (byte) this.param.TidPtr, (byte) this.param.TidLen, b, ByteCompanionObject.MIN_VALUE, this.param.ScanTime, this.pOUcharIDList, iArr2, iArr);
            if (iArr2[0] == 0) {
                this.isSound = false;
                if (this.param.Session > 1) {
                    this.NoCardCOunt++;
                    if (this.NoCardCOunt > 7) {
                        b = (byte) (1 - b);
                        this.NoCardCOunt = 0;
                    }
                }
            } else {
                this.NoCardCOunt = 0;
                this.isSound = true;
            }
            this.endtime = SystemClock.elapsedRealtime();
            InventoryTagResult inventoryTagResult = new InventoryTagResult();
            inventoryTagResult.nCount = iArr2[0];
            inventoryTagResult.nTime = this.endtime - this.beginTime;
            inventoryTagResult.speed = (int) ((inventoryTagResult.nCount * 1000) / inventoryTagResult.nTime);
            inventoryTagResult.result = Inventory_G2;
            inventoryTagResult.NewNum = lsTagList.size() - this.CurrentNum;
            this.CurrentNum = lsTagList.size();
            inventoryTagResult.TotalTime = SystemClock.elapsedRealtime() - this.ttbegintime;
            lsList.add(inventoryTagResult);
            SystemClock.sleep(this.param.Interval);
        }
        this.isSound = false;
        this.mThread = null;
        TagCallback tagCallback = this.callback;
        if (tagCallback != null) {
            tagCallback.FinishCallBack();
        }
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public void StopRead() {
        this.mWorking = false;
        if (this.mThread != null) {
            this.isSound = false;
        }
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public List<InventoryTagMap> getInventoryTagMapList() {
        return lsTagList;
    }

    @Override // com.lckj.lcrrgxmodule.factory.ILcUhfProduct
    public List<InventoryTagResult> getInventoryTagResultList() {
        return lsList;
    }

    public class OutDataCB implements TagCallback {
        @Override // com.rfid.trans.TagCallback
        public int CRCErrorCallBack(int i) {
            return 0;
        }

        @Override // com.rfid.trans.TagCallback
        public void FinishCallBack() {
        }

        @Override // com.rfid.trans.TagCallback
        public int tagCallbackFailed(int i) {
            return 0;
        }

        public OutDataCB() {
        }

        @Override // com.rfid.trans.TagCallback
        public void tagCallback(ReadTag readTag) {
            readTag.epcId.toUpperCase();
        }
    }
}
