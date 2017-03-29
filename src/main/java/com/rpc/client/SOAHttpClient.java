package com.rpc.client;

import com.rpc.model.ClientConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * <p></p>
 *
 * @author dl
 * @Date 2017/3/29 10:42
 */
@Data
public class SOAHttpClient {
    private ClientConfiguration configuration;

    private BaseClient client;

    public SOAHttpClient(ClientConfiguration configuration) {
        this.configuration = configuration;
        String serverUrl = configuration.getServerUrl();
        int soTimeout = configuration.getSoTimeout();
        if (soTimeout == 0) {
            this.client = new BaseClient(standardizeUrl(serverUrl));
        } else {
            this.client = new BaseClient(standardizeUrl(serverUrl), soTimeout);
        }
        this.client.setApp(configuration.getApp());
        if (!StringUtils.isEmpty(configuration.getActionSuffix())) {
            this.client.setActionSuffix(configuration.getActionSuffix());
        }
    }

    private static String standardizeUrl(String url) {
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public String httpGet(Map<String, String> params, String uri) {
        return this.client.httpPost(params, uri);
    }

    public String httpPost(Map<String, String> params, String uri) {
        return this.client.httpPost(params, uri);
    }
}
