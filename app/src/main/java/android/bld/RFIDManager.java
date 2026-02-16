package android.bld;

import android.content.Context;

/**
 * Stub for device-specific BLD RFID framework class.
 * On actual BLD hardware, this is provided by the device ROM.
 */
public class RFIDManager {
    private RFIDManager() {}

    public static RFIDManager getDefaultInstance(Context context) {
        return null;
    }

    public int getRFIDType() {
        return 0;
    }
}
