package com.rfid.trans;

/* loaded from: classes3.dex */
public interface TagCallback {
    int CRCErrorCallBack(int i);

    void FinishCallBack();

    void tagCallback(ReadTag readTag);

    int tagCallbackFailed(int i);
}
