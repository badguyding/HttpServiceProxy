package com.rpc.proxy.impl;

import com.rpc.proxy.ProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * <p>JDK代理</p>
 *
 * @author dl
 * @Date 2017/3/29 10:37
 */
public class JdkProxyFactory implements ProxyFactory {
    public <T> T getProxy(Class<T> clz, InvocationHandler invocationHandler) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clz}, invocationHandler);
    }
}
