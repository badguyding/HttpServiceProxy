package com.rpc;

import com.rpc.client.SOAHttpClient;
import com.rpc.model.ClientConfiguration;
import com.rpc.proxy.HttpInvocationHandler;
import com.rpc.proxy.ProxyFactory;
import com.rpc.proxy.impl.JdkProxyFactory;
import lombok.Getter;

/**
 * <p></p>
 *
 * @author dl
 * @Date 2017/3/29 10:51
 */
public class SOAServiceHttpFactory {
    @Getter
    private SOAHttpClient soaHttpClient;

    private ProxyFactory proxyFactory = new JdkProxyFactory();

    private HttpInvocationHandler httpInvocationHandler;

    public SOAServiceHttpFactory(ClientConfiguration clientConfiguration) {
        this.soaHttpClient = new SOAHttpClient(clientConfiguration);
        this.httpInvocationHandler = new HttpInvocationHandler(soaHttpClient);
    }

    /**
     * 复用http客户端
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public <T> T create(Class<T> serviceClass) {
        return proxyFactory.getProxy(serviceClass, this.httpInvocationHandler);
    }

    /**
     * 独占http客户端
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public <T> T createBySelfIndependentClient(Class<T> serviceClass) {
        SOAHttpClient soaHttpClient = new SOAHttpClient(this.soaHttpClient.getConfiguration());
        HttpInvocationHandler httpInvocationHandler = new HttpInvocationHandler(soaHttpClient);
        return proxyFactory.getProxy(serviceClass, httpInvocationHandler);
    }

}
