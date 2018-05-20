package com.egguncle.shield.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by songyucheng on 18-4-14.
 */

public class Shield {
    private final static String TAG = Shield.class.getSimpleName();
    private final static String PAYLOAD_APK_NAME = "payload.apk";
    private Context mContext;

    public Shield(Context context) {
        mContext = context;
    }

    public void getPayloadApk() {
        byte[] dexData = null;
        try {
            dexData = FileUtil.readDexFileFromApk(getShieldApk());
            dexData = decryptData(dexData);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "getPayloadApk: " + e.getLocalizedMessage());
        }
        int payloadLength = getPayloadApkLength(dexData);
        Log.i(TAG, "getPayloadApk: " + payloadLength);
        byte[] payloadData = getPayloadApkDataFromShield(dexData, payloadLength);
        writePayloadDataToDisk(payloadData);
    }

    private String getShieldApk() {
        String path = "";
        //先调试一下看看,把现成加好payload的apk放在sd目录下
        //return getPath();
        return mContext.getApplicationInfo().sourceDir;
    }

    private String getPath() {
        File file = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sdCardDir = Environment.getExternalStorageDirectory();
            try {
                file = new File(sdCardDir.getCanonicalPath() + "/test.apk");
                if (!file.exists()) {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "getPath: " + file.getPath());
        return file.getPath();
    }

    private int getPayloadApkLength(byte[] dexData) {
        byte[] bytes = new byte[4];
        System.arraycopy(dexData, dexData.length - 4, bytes, 0, 4);
        Log.i(TAG, "getPayloadApkLength: " + bytesToHexString(bytes));
        Log.i(TAG, "getPayloadApkLength: " + Integer.parseInt(bytesToHexString(bytes), 16));
        return Integer.parseInt(bytesToHexString(bytes), 16);
    }

    private byte[] getPayloadApkDataFromShield(byte[] dexData, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(dexData, dexData.length - length - 4, bytes, 0, length);
        return bytes;
    }

    //解密apk的方法,这里直接返回
    private byte[] decryptData(byte[] data) {
        return data;
    }

    private void writePayloadDataToDisk(byte[] data) {
        try {
            FileOutputStream fout = mContext.openFileOutput(PAYLOAD_APK_NAME, MODE_PRIVATE);
            fout.write(data);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int bytesToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value |= (bytes[i] & 0xFF) << (8 * i);
        }
        return value;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();

    }
}
