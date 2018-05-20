package com.egguncle.shield.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by songyucheng on 18-4-19.
 */

public class ShieldReflectUtil {

    public static Object invokeMethod(Object thiz, String clazzName, String funcName, Class[] paramTypes,
                                      Object[] params) {
        try {
            Class clazz = Class.forName(clazzName);
            Method method = clazz.getDeclaredMethod(funcName, paramTypes);
            method.setAccessible(true);
            return method.invoke(thiz, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getFieldObjByName(Object thiz, String clazzName, String fieldName) {
        try {
            Class clazz = Class.forName(clazzName);
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(thiz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object setFieldObjByName(Object thiz, String clazzName, String fieldName, Object vaule) {
        try {
            Class clazz = Class.forName(clazzName);
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(thiz, vaule);
            return field.get(thiz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
