package com.rpc.model;

import lombok.Data;

/**
 * <p></p>
 *
 * @author dl
 * @Date 2017/3/29 10:10
 */
@Data
public class ClientConfiguration {
    /**
     * SOA服务器地址
     */
    private String serverUrl;
    /**
     * 使用SOAClient的应用名称
     */
    private String app;
    /**
     * 访问超时
     */
    private int soTimeout;
    /**
     * SOA服务器地址url后缀
     */
    private String actionSuffix = "";
}
