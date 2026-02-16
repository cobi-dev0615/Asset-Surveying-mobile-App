package com.rfid;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

/* loaded from: classes3.dex */
public class Util {
    public static boolean showWarning(Context context, int i) {
        Toast.makeText(context, i, 1).show();
        return false;
    }

    public static boolean isEtEmpty(EditText editText) {
        String obj = editText.getText().toString();
        return obj == null || obj.equals("");
    }

    public static boolean isLenLegal(EditText editText) {
        String obj;
        return (isEtEmpty(editText) || (obj = editText.getText().toString()) == null || obj.length() % 2 != 0) ? false : true;
    }

    public static boolean isEtsLegal(EditText[] editTextArr) {
        for (EditText editText : editTextArr) {
            if (isLenLegal(editText)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHexNumber(String str) {
        int i = 0;
        boolean z = false;
        while (i < str.length()) {
            char charAt = str.charAt(i);
            if (charAt != '0' && charAt != '1' && charAt != '2' && charAt != '3' && charAt != '4' && charAt != '5' && charAt != '6' && charAt != '7' && charAt != '8' && charAt != '9' && charAt != 'A' && charAt != 'B' && charAt != 'C' && charAt != 'D' && charAt != 'E' && charAt != 'F' && charAt != 'a' && charAt != 'b' && charAt != 'c' && charAt != 'c' && charAt != 'd' && charAt != 'e' && charAt != 'f') {
                return false;
            }
            i++;
            z = true;
        }
        return z;
    }
}
