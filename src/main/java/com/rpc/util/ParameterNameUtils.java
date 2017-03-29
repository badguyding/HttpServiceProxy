package com.rpc.util;

import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * <p></p>
 *
 * @author dl
 * @Date 2017/3/29 10:32
 */
public class ParameterNameUtils {
    public static List<String> getMethodParameterNamesByThoughtworks(Method method) {
        Paranamer paranamer = new CachingParanamer();
        String[] parameterNames = paranamer.lookupParameterNames(method);
        return Arrays.asList(parameterNames);
    }
}
