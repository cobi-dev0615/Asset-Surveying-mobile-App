package com.lckj.lcrrgxmodule.factory;

import android.media.SoundPool;
import com.rfid.InventoryTagMap;
import com.rfid.InventoryTagResult;
import com.rfid.trans.ReaderParameter;
import com.rfid.trans.TagCallback;
import java.util.List;

/* loaded from: classes3.dex */
public interface ILcUhfProduct {
    int Connect();

    int Connect(String str, int i);

    int DisConnect();

    ReaderParameter GetInventoryParameter();

    int GetModuleVersion();

    int GetUHFInformation(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, byte[] bArr6, byte[] bArr7);

    int Kill(String str, String str2);

    int Lock(String str, byte b, byte b2, String str2);

    int MeasureReturnLoss(byte[] bArr);

    int MeasureTemperature(byte[] bArr);

    String ReadDataByEPC(String str, byte b, byte b2, byte b3, byte[] bArr);

    String ReadDataByTID(String str, byte b, byte b2, byte b3, byte[] bArr);

    int RfOutput(byte b);

    int SetAntenna(byte b);

    int SetBaudRate(byte b);

    int SetBeepNotification(int i);

    void SetCallBack(TagCallback tagCallback);

    void SetInventoryParameter(ReaderParameter readerParameter);

    int SetPowerMode(int i);

    int SetRegion(int i, int i2, int i3);

    int SetRfPower(int i);

    int SetWorkMode(byte b);

    int StartRead();

    void StopRead();

    int WriteDataByEPC(String str, byte b, byte b2, byte[] bArr, String str2);

    int WriteDataByTID(String str, byte b, byte b2, byte[] bArr, String str2);

    int WriteEPCByTID(String str, String str2, byte[] bArr);

    void beginSound(boolean z);

    List<InventoryTagMap> getInventoryTagMapList();

    List<InventoryTagResult> getInventoryTagResultList();

    void playSound();

    void setsoundid(int i, SoundPool soundPool);
}
