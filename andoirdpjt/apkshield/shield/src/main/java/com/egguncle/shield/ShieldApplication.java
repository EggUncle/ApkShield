package com.egguncle.shield;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.egguncle.shield.util.ReflectUtil;
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        String appClassName = null;
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
        Object currentActivityThread = ReflectUtil.invokeMethod(null, "android.app.ActivityThread", "currentActivityThread", new Class[]{}, new Object[]{});
        Map mPackages = (Map) ReflectUtil.getFieldObjByName(currentActivityThread, "android.app.ActivityThread", "mPackages");
        WeakReference wr = (WeakReference) mPackages.get(pkgName);
        ReflectUtil.setFieldObjByName(wr.get(), "android.app.LoadedApk", "mClassLoader", payloadClassLoader);

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
        String appClassName = "com.egguncle.apkshield.MyApplication";
/**
 * 调用静态方法android.app.ActivityThread.currentActivityThread
 * 获取当前activity所在的线程对象
 */
        Object currentActivityThread = ReflectUtil.invokeMethod(null,
                "android.app.ActivityThread", "currentActivityThread",
                new Class[]{}, new Object[]{});
/**
 * 获取currentActivityThread中的mBoundApplication属性对象，该对象是一个
 *  AppBindData类对象，该类是ActivityThread的一个内部类
 */
        Object mBoundApplication = ReflectUtil.getFieldObjByName(currentActivityThread,
                "android.app.ActivityThread", "mBoundApplication");
/**
 * 获取mBoundApplication中的info属性，info 是 LoadedApk类对象
 */

        Object loadedApkInfo = ReflectUtil.getFieldObjByName(mBoundApplication,
                "android.app.ActivityThread$AppBindData", "info");
        if (null == loadedApkInfo) {
            Log.i(TAG, "onCreate: loadedapkinfo is null");
        } else {
        }

/**
 * loadedApkInfo对象的mApplication属性置为null
 */
        ReflectUtil.setFieldObjByName(loadedApkInfo, "android.app.LoadedApk", "mApplication", null);


/**
 * 获取currentActivityThread对象中的mInitialApplication属性
 * 这货是个正牌的 Application
 */
        Object oldApplication = ReflectUtil.getFieldObjByName(currentActivityThread,
                "android.app.ActivityThread", "mInitialApplication");


/**
 * 获取currentActivityThread对象中的mAllApplications属性
 * 这货是 装Application的列表
 */
        ArrayList<Application> mAllApplications = (ArrayList<Application>) ReflectUtil
                .getFieldObjByName(currentActivityThread, "android.app.ActivityThread", "mAllApplications");
//列表对象终于可以直接调用了 remove调了之前获取的application 抹去记录的样子
        mAllApplications.remove(oldApplication);


/**
 * 获取前面得到LoadedApk对象中的mApplicationInfo属性，是个ApplicationInfo对象
 */
        ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo) ReflectUtil
                .getFieldObjByName(loadedApkInfo, "android.app.LoadedApk", "mApplicationInfo");

/**
 * 获取前面得到AppBindData对象中的appInfo属性，也是个ApplicationInfo对象
 */
        ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo) ReflectUtil
                .getFieldObjByName(mBoundApplication, "android.app.ActivityThread$AppBindData", "appInfo");

//把这两个对象的className属性设置为从meta-data中获取的被加密apk的application路径
        appinfo_In_LoadedApk.className = appClassName;
        appinfo_In_AppBindData.className = appClassName;

/**

 * 调用LoadedApk中的makeApplication 方法 造一个application
 * 前面改过路径了
 */
        Application app = (Application) ReflectUtil.invokeMethod(loadedApkInfo,
                "android.app.LoadedApk", "makeApplication",
                new Class[]{boolean.class, Instrumentation.class},
                new Object[]{false, null});
        ReflectUtil.setFieldObjByName(currentActivityThread, "android.app.ActivityThread", "mInitialApplication", app);

        if (null == app) {
            Log.i(TAG, "onCreate: app is null");
        } else {
            app.onCreate();
        }

    }


    private DexClassLoader getPayloadClassLoader(String odexPath, String pkgName, Context base) {
        return new DexClassLoader(
                odexPath + "/payload.apk", odexPath, "/data/data/" + pkgName + "/lib", base.getClassLoader().getParent());
    }

}
