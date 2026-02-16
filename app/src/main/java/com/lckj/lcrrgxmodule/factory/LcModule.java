package com.lckj.lcrrgxmodule.factory;

import android.bld.RFIDManager;
import android.content.Context;
import android.util.Log;

/* loaded from: classes3.dex */
public class LcModule implements IUhfModule {
    private final String TAG = "bldAdd#LcModule";
    private Context mContext;
    private RFIDManager rfidManager;

    public LcModule() {
    }

    public LcModule(Context context) {
        this.mContext = context;
    }

    @Override // com.lckj.lcrrgxmodule.factory.IUhfModule
    public ILcUhfProduct createProduct() {
        if (this.rfidManager == null) {
            try {
                Log.d(this.TAG, "createProduct: rfidManager -->" + this.rfidManager);
                this.rfidManager = RFIDManager.getDefaultInstance(this.mContext);
            } catch (Exception unused) {
            }
        }
        RFIDManager rFIDManager = this.rfidManager;
        int rFIDType = rFIDManager != null ? rFIDManager.getRFIDType() : 0;
        Log.d(this.TAG, "createProduct: --> " + rFIDType);
        if (rFIDType == 32) {
            Log.e(this.TAG, "GX!!!!");
            return new GxUhfProduct();
        }
        Log.e(this.TAG, "RR!!!!");
        return new RfidUhfProduct();
    }

    @Override // com.lckj.lcrrgxmodule.factory.IUhfModule
    public ILcUhfProduct createProduct(int i) {
        if (this.rfidManager == null) {
            try {
                Log.d(this.TAG, "createProduct: rfidManager -->" + this.rfidManager);
                this.rfidManager = RFIDManager.getDefaultInstance(this.mContext);
            } catch (Exception unused) {
            }
        }
        if (i == 32) {
            Log.e(this.TAG, "GX!!!!");
            return new GxUhfProduct();
        }
        Log.e(this.TAG, "RR!!!!");
        return new RfidUhfProduct();
    }
}
