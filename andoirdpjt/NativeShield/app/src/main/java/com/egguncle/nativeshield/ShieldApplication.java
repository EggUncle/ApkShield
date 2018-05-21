package com.egguncle.nativeshield;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * Created by songyucheng on 18-5-21.
 */

public class ShieldApplication extends Application {

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected native void attachBaseContext(Context base);

    @Override
    public native void onCreate();

}
