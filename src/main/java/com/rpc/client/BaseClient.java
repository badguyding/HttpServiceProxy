package com.rpc.client;

import com.rpc.util.http.HTTPLongClient4;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <p></p>
 *
 * @author dl
 * @Date 2017/3/29 10:13
 */
public class BaseClient {
    private static final Logger logger = LoggerFactory.getLogger(BaseClient.class);

    /**
     * 读取超时时间,默认3000毫秒
     */
    private static int DEFAULT_SOTIMEOUT = 3000;
    /**
     * 必须，请求地址
     */
    private String serverURL;

    private HTTPLongClient4 httpClient;

    private String clientVersion = "0.0.0";

    private String actionSuffix = ".action";

    private String app;

    public BaseClient(String serverURL) {
        this(serverURL, DEFAULT_SOTIMEOUT);
    }

    public BaseClient(String serverURL, int soTimeout) {
        clientVersion = this.getClass().getPackage().getImplementationVersion();
        this.serverURL = serverURL;
        httpClient = new HTTPLongClient4(soTimeout);
    }

    protected String httpGet(Map<String, String> params) {
        return httpGet(params, new RuntimeException().getStackTrace()[1].getMethodName());
    }

    protected String httpGet(Map<String, String> params, String methodName) {
        if (StringUtils.isBlank(getApp())) {
            throw new IllegalArgumentException("app参数不可为空");
        }
        StringBuilder buf = new StringBuilder();
        buf.append(serverURL).append(methodName).append(actionSuffix)
                .append("?javaClient=true&app=").append(getApp()).append("&ver=").append(clientVersion);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                buf.append("&" + entry.getKey() + "=" + entry.getValue());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(buf.toString());
        }
        return httpClient.get(buf.toString());
    }

    protected String httpPost(Map<String, String> params) {
        return httpPost(params, new RuntimeException().getStackTrace()[1].getMethodName());
    }

    /**
     * @param params
     * @param retry  重试次数
     * @return
     */
    protected String httpPost(Map<String, String> params, int retry) {
        return httpPost(params, new RuntimeException().getStackTrace()[1].getMethodName(), retry);
    }

    protected String httpPost(Map<String, String> params, String methodName) {
        if (StringUtils.isBlank(getApp())) {
            throw new IllegalArgumentException("app参数不可为空");
        }
        params.put("app", getApp());
        params.put("javaClient", "true");
        params.put("ver", clientVersion);
        String url = serverURL + methodName + actionSuffix;
        if (logger.isDebugEnabled()) {
            logger.debug(url);
        }
        return httpClient.post(url, params);
    }

    protected String httpPost(Map<String, String> params, String methodName, int retry) {
        if (StringUtils.isBlank(getApp())) {
            throw new IllegalArgumentException("app参数不可为空");
        }
        params.put("app", getApp());
        params.put("javaClient", "true");
        params.put("ver", clientVersion);
        String url = serverURL + methodName + actionSuffix;
        if (logger.isDebugEnabled()) {
            logger.debug(url);
        }
        return httpClient.postExRetry(url, params, retry);
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setActionSuffix(String actionSuffix) {
        this.actionSuffix = actionSuffix;
    }
}
