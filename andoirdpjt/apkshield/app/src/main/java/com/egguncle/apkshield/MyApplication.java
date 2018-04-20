package com.egguncle.apkshield;

import android.app.Application;
import android.util.Log;

/**
 * Created by songyucheng on 18-4-20.
 */

public class MyApplication extends Application {

    private final static String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: payload application onCreate success");
    }
}
