package com.rfid.serialport;

import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* loaded from: classes3.dex */
public class SerialPort {
    private static final String TAG = "SerialPort";
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    private static native FileDescriptor open(String str, int i, int i2);

    public native void close();

    public native void tcflush();

    public SerialPort(File file, int i, int i2) throws SecurityException, IOException {
        FileDescriptor open = open(file.getAbsolutePath(), i, i2);
        this.mFd = open;
        if (open == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        this.mFileInputStream = new FileInputStream(this.mFd);
        this.mFileOutputStream = new FileOutputStream(this.mFd);
    }

    public InputStream getInputStream() {
        return this.mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return this.mFileOutputStream;
    }

    static {
        System.loadLibrary("serial_port");
    }
}
