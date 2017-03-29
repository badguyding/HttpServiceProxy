package com.rpc.util;

import java.lang.reflect.Method;

/**
 * <p></p>
 *
 * @author dl
 * @Date 2017/3/29 10:34
 */
public class ReflectUtil {
    public static String buildKey(Method method) {
        return method.toGenericString();
    }
}
