package com.rpc.proxy;

import java.lang.reflect.InvocationHandler;

/**
 * <p></p>
 *
 * @author dl
 * @Date 2017/3/29 10:36
 */
public interface ProxyFactory {
    <T> T getProxy(Class<T> clz, InvocationHandler invocationHandler);
}
