#include <jni.h>
#include <string>
#include<android/log.h>

#define TAG "nativeApkShield"

JNIEXPORT void JNICALL native_onCreate(JNIEnv *env, jobject jobj) {
    //super.onCreate();
    jclass clazz_android_app_Application = env->FindClass("android/app/Application");
    jmethodID method_id_onCreate = env->GetMethodID(clazz_android_app_Application, "onCreate",
                                                    "()V");
    env->CallNonvirtualVoidMethod(jobj, clazz_android_app_Application, method_id_onCreate);

}


JNIEXPORT void JNICALL native_attachBaseContext(JNIEnv *env, jobject jobj, jobject context) {
    //super.attachBaseContext(context);
    jclass clazz_android_content_ContextWrapper = env->FindClass("android/content/ContextWrapper");
    jmethodID method_id_attachBaseContext = env->GetMethodID(clazz_android_content_ContextWrapper,
                                                             "attachBaseContext",
                                                             "(Landroid/content/Context;)V");
    env->CallNonvirtualVoidMethod(jobj, clazz_android_content_ContextWrapper,
                                  method_id_attachBaseContext, context);

    __android_log_write(ANDROID_LOG_INFO,TAG,"just a log");

}

JNINativeMethod nativeMethods[] = {
        {"attachBaseContext", "(Landroid/content/Context;)V", (void *) native_attachBaseContext},
        {"onCreate",          "()V",                          (void *) native_onCreate}
};

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reservered) {
    JNIEnv *env = NULL;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }
    jclass mainActClass = env->FindClass("com/egguncle/nativeshield/ShieldApplication");
    env->RegisterNatives(mainActClass, nativeMethods,
                         sizeof(nativeMethods) / sizeof(nativeMethods[0]));

    // 返回jni的版本
    return JNI_VERSION_1_4;

}