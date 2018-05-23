package com.egguncle.shield;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.egguncle.shield.util.ShieldReflectUtil;
import com.egguncle.shield.util.Shield;


import org.xml.sax.helpers.LocatorImpl;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * Created by songyucheng on 18-4-16.
 */

public class ShieldApplication extends Application {
    private final static String TAG = ShieldApplication.class.getSimpleName();

    private String appClassName;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            ApplicationInfo appInfo = this.getPackageManager()
                    .getApplicationInfo(this.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            if (bundle != null
                    && bundle.containsKey("APPLICATION_CLASS_NAME")) {
                appClassName = bundle.getString("APPLICATION_CLASS_NAME");
                Log.i(TAG, "onCreate: " + appClassName);
            } else {
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Shield shield = new Shield(this);
        shield.getPayloadApk();

        String pkgName = getPackageName();
        File odex = getFilesDir();
        String odexPath = odex.getAbsolutePath();
        Log.i(TAG, "attachBaseContext: " + odexPath);
        DexClassLoader payloadClassLoader = getPayloadClassLoader(odexPath, pkgName, base);

        //替换类加载器
        Object currentActivityThread = ShieldReflectUtil.invokeMethod(null, "android.app.ActivityThread", "currentActivityThread", new Class[]{}, new Object[]{});
        Map mPackages = (Map) ShieldReflectUtil.getFieldObjByName(currentActivityThread, "android.app.ActivityThread", "mPackages");
        WeakReference wr = (WeakReference) mPackages.get(pkgName);
        ShieldReflectUtil.setFieldObjByName(wr.get(), "android.app.LoadedApk", "mClassLoader", payloadClassLoader);

        try {
            Log.i(TAG, "attachBaseContext: start get load class " + appClassName);
            payloadClassLoader.loadClass(appClassName);
            Log.i(TAG, "attachBaseContext: load application success");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "attachBaseContext: load application failed");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 如果源应用配置有Appliction对象，则替换为源应用Applicaiton，以便不影响源程序逻辑。
        if (appClassName == null) {
            appClassName = "com.egguncle.apkshield.MyApplication";
        }
        Object currentActivityThread = ShieldReflectUtil.invokeMethod(null,
                "android.app.ActivityThread", "currentActivityThread",
                new Class[]{}, new Object[]{});
        Object mBoundApplication = ShieldReflectUtil.getFieldObjByName(currentActivityThread,
                "android.app.ActivityThread", "mBoundApplication");

        Object loadedApkInfo = ShieldReflectUtil.getFieldObjByName(mBoundApplication,
                "android.app.ActivityThread$AppBindData", "info");
        if (null == loadedApkInfo) {
            Log.i(TAG, "onCreate: loadedapkinfo is null");
        } else {
        }
        ShieldReflectUtil.setFieldObjByName(loadedApkInfo, "android.app.LoadedApk", "mApplication", null);
        Object oldApplication = ShieldReflectUtil.getFieldObjByName(currentActivityThread,
                "android.app.ActivityThread", "mInitialApplication");

        ArrayList<Application> mAllApplications = (ArrayList<Application>) ShieldReflectUtil
                .getFieldObjByName(currentActivityThread, "android.app.ActivityThread", "mAllApplications");

        mAllApplications.remove(oldApplication);

        ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo) ShieldReflectUtil
                .getFieldObjByName(loadedApkInfo, "android.app.LoadedApk", "mApplicationInfo");

        ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo) ShieldReflectUtil
                .getFieldObjByName(mBoundApplication, "android.app.ActivityThread$AppBindData", "appInfo");

        appinfo_In_LoadedApk.className = appClassName;
        appinfo_In_AppBindData.className = appClassName;

        Application app = (Application) ShieldReflectUtil.invokeMethod(loadedApkInfo,
                "android.app.LoadedApk", "makeApplication",
                new Class[]{boolean.class, Instrumentation.class},
                new Object[]{false, null});
        ShieldReflectUtil.setFieldObjByName(currentActivityThread, "android.app.ActivityThread", "mInitialApplication", app);

        if (null == app) {
            Log.i(TAG, "onCreate: app is null");
        } else {
            app.onCreate();
        }

    }


    private DexClassLoader getPayloadClassLoader(String odexPath, String pkgName, Context base) {
        String dexFilePath = odexPath + "/payload.dex";
        odexPath = odexPath + "/odexpath";
        File file = new File(odexPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return new DexClassLoader(
                dexFilePath, odexPath, "/data/data/" + pkgName + "/lib", base.getClassLoader().getParent());
    }

}
