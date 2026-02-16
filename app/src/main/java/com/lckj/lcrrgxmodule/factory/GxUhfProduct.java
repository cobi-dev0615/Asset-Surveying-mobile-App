package com.lckj.lcrrgxmodule.factory;

import android.media.SoundPool;
import com.rfid.InventoryTagMap;
import com.rfid.InventoryTagResult;
import com.rfid.trans.ReaderParameter;
import com.rfid.trans.TagCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub for GX-type UHF RFID product (decompiled vendor library).
 * Full implementation was not included in the original classes3.dex extraction.
 */
public class GxUhfProduct implements ILcUhfProduct {

    @Override public int Connect() { return -1; }
    @Override public int Connect(String str, int i) { return -1; }
    @Override public int DisConnect() { return 0; }
    @Override public ReaderParameter GetInventoryParameter() { return new ReaderParameter(); }
    @Override public int GetModuleVersion() { return 0; }
    @Override public int GetUHFInformation(byte[] b1, byte[] b2, byte[] b3, byte[] b4, byte[] b5, byte[] b6, byte[] b7) { return -1; }
    @Override public int Kill(String str, String str2) { return -1; }
    @Override public int Lock(String str, byte b, byte b2, String str2) { return -1; }
    @Override public int MeasureReturnLoss(byte[] bArr) { return -1; }
    @Override public int MeasureTemperature(byte[] bArr) { return -1; }
    @Override public String ReadDataByEPC(String str, byte b, byte b2, byte b3, byte[] bArr) { return "FF"; }
    @Override public String ReadDataByTID(String str, byte b, byte b2, byte b3, byte[] bArr) { return "FF"; }
    @Override public int RfOutput(byte b) { return -1; }
    @Override public int SetAntenna(byte b) { return -1; }
    @Override public int SetBaudRate(byte b) { return -1; }
    @Override public int SetBeepNotification(int i) { return -1; }
    @Override public void SetCallBack(TagCallback tagCallback) {}
    @Override public void SetInventoryParameter(ReaderParameter readerParameter) {}
    @Override public int SetPowerMode(int i) { return -1; }
    @Override public int SetRegion(int i, int i2, int i3) { return -1; }
    @Override public int SetRfPower(int i) { return -1; }
    @Override public int SetWorkMode(byte b) { return -1; }
    @Override public int StartRead() { return -1; }
    @Override public void StopRead() {}
    @Override public int WriteDataByEPC(String str, byte b, byte b2, byte[] bArr, String str2) { return -1; }
    @Override public int WriteDataByTID(String str, byte b, byte b2, byte[] bArr, String str2) { return -1; }
    @Override public int WriteEPCByTID(String str, String str2, byte[] bArr) { return -1; }
    @Override public void beginSound(boolean z) {}
    @Override public List<InventoryTagMap> getInventoryTagMapList() { return new ArrayList<>(); }
    @Override public List<InventoryTagResult> getInventoryTagResultList() { return new ArrayList<>(); }
    @Override public void playSound() {}
    @Override public void setsoundid(int i, SoundPool soundPool) {}
}
