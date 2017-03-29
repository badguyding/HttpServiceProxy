package com.rpc.proxy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rpc.annotation.SOAServerRequest;
import com.rpc.client.SOAHttpClient;
import com.rpc.exception.SOAException;
import com.rpc.model.HttpMethod;
import com.rpc.util.JsonUtil;
import com.rpc.util.ParameterNameUtils;
import com.rpc.util.ReflectUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p></p>
 *
 * @author dl
 * @Date 2017/3/29 10:40
 */
public class HttpInvocationHandler implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpInvocationHandler.class);
    private static final Map<String, BindMethodInfo> METHOD_TO_URI = new ConcurrentHashMap<String, BindMethodInfo>();
    public static Set<String> OBJECT_MTHOD = new HashSet();
    private SOAHttpClient client;

    static {
        for (Method method : Object.class.getMethods()) {
            OBJECT_MTHOD.add(ReflectUtil.buildKey(method));
        }
    }

    public HttpInvocationHandler(SOAHttpClient client) {
        this.client = client;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (client == null) {
            throw new SOAException("请初始化客户端");
        }
        if (OBJECT_MTHOD.contains(ReflectUtil.buildKey(method))) {
            return null;
        }
        BindMethodInfo bindMethodInfo = getBindMethodInfo(method);
        Map map = buildParams(args, bindMethodInfo);
        //让其支持get和post，有些数据访问统计放到日志中就行了
        SOAServerRequest annotation = method.getAnnotation(SOAServerRequest.class);
        String responseString;
        long begin = System.currentTimeMillis();
        if (annotation == null || annotation.httpMethod().equals(HttpMethod.GET)) {
            responseString = this.client.httpGet(map, bindMethodInfo.getUrl());
        } else {
            responseString = this.client.httpPost(map, bindMethodInfo.getUrl());
        }
        logger.info("app:{},url:{},cost:{} ms", this.client.getConfiguration().getApp(), bindMethodInfo.getUrl(), System.currentTimeMillis() - begin);
        if (StringUtils.isEmpty(responseString) || responseString.equals("null")) {
            logger.info("返回数据为空:{}", responseString);
            return null;
        }
        if (method.getReturnType().isAssignableFrom(String.class)) {
            return responseString;
        }
        return JsonUtil.jsonToBeanByType(responseString, method.getGenericReturnType());
    }

    private Map<String, String> buildParams(Object[] args, BindMethodInfo bindMethodInfo) {
        Map<String, String> paramsMap = new HashMap();
        for (int i = 0; i < bindMethodInfo.getParamNames().size(); i++) {
            Object obj = args[i];
            if (obj == null) {
                continue;
            }
            String json;
            if (String.class.isAssignableFrom(obj.getClass())) {
                json = (String) obj;
            } else {
                json = JsonUtil.beanToJson(obj);
            }
            //兼容下老的框架
            if (StringUtils.isNoneEmpty(json) && json.startsWith("{")) {
                Map<String, Object> map = JsonUtil.jsonToBeanByTypeReference(json, new TypeReference<Map<String, Object>>() {
                });
                for (String s : map.keySet()) {
                    if (map.get(s) == null) {
                        continue;
                    }
                    if (String.class.isAssignableFrom(map.get(s).getClass())) {
                        paramsMap.put(s, (String) map.get(s));
                        continue;
                    }
                    if (Collection.class.isAssignableFrom(map.get(s).getClass())) {
                        if (((Collection) map.get(s)).size() == 0) {
                            paramsMap.put(s, "");
                            continue;
                        }
                        Collection collection = (Collection) map.get(s);
                        //字符串的集合如何处理
                        if (String.class.isAssignableFrom(collection.toArray()[0].getClass())) {
                            Iterator iterator = collection.iterator();
                            StringBuffer stringBuffer = new StringBuffer();
                            while (iterator.hasNext()) {
                                String str = (String) iterator.next();
                                stringBuffer.append(str + ",");
                            }
                            String s1 = stringBuffer.toString();
                            if (s1.endsWith(",")) {
                                paramsMap.put(s, s1.substring(0, s1.length() - 1));
                            }

                        } else {
                            //非字符串
                            String s1 = JsonUtil.beanToJson(map.get(s));
                            paramsMap.put(s, s1.substring(1, s1.length() - 1));
                        }
                        continue;
                    }
                    paramsMap.put(s, JsonUtil.beanToJson(map.get(s)));
                }
            }

            if (paramsMap.get(bindMethodInfo.getParamNames().get(i)) != null) {
                logger.error("相同参数:{},{}", bindMethodInfo.getUrl(), bindMethodInfo.getParamNames().get(i));
            }
            paramsMap.put(bindMethodInfo.getParamNames().get(i), json);
        }
        Set<String> keys = new HashSet<String>();
        for (Map.Entry<String, String> stringObjectEntry : paramsMap.entrySet()) {
            if (stringObjectEntry.getValue() == null || stringObjectEntry.getValue().equals("null")) {
                keys.add(stringObjectEntry.getKey());
            }

        }
        for (String key : keys) {
            paramsMap.remove(key);
        }
        return paramsMap;
    }

    private BindMethodInfo getBindMethodInfo(Method method) throws SOAException {
        String key = ReflectUtil.buildKey(method);
        BindMethodInfo bindMethodInfo = METHOD_TO_URI.get(key);
        if (bindMethodInfo == null) {
            String uri = buildUri(method);
            bindMethodInfo = new BindMethodInfo(ParameterNameUtils.getMethodParameterNamesByThoughtworks(method), uri);
            METHOD_TO_URI.put(key, bindMethodInfo);
        }
        return bindMethodInfo;
    }

    private String buildUri(Method method) throws SOAException {
        SOAServerRequest methodAnnotation = method.getDeclaringClass().getAnnotation(SOAServerRequest.class);
        String parentUrl = "";
        if (methodAnnotation != null) {
            parentUrl = methodAnnotation.value();
        }
        String childrenUrl = "";
        SOAServerRequest classAnnotation = method.getAnnotation(SOAServerRequest.class);
        if (classAnnotation == null || StringUtils.isEmpty(classAnnotation.value())) {
            childrenUrl = method.getName();
        } else {
            childrenUrl = classAnnotation.value();
        }
        if (parentUrl.length() > 0 && !parentUrl.startsWith("/")) {
            parentUrl = "/" + parentUrl;
        }
        if (StringUtils.isEmpty(childrenUrl)) {
            throw new SOAException("请正确配置请求URI地址");
        }
        while (childrenUrl.startsWith("/")) {
            childrenUrl = childrenUrl.substring(1);
        }
        if (childrenUrl.endsWith("/")) {
            childrenUrl = childrenUrl.substring(0, childrenUrl.length() - 1);
        }
        return parentUrl + "/" + childrenUrl;
    }

    @Data
    @AllArgsConstructor
    static class BindMethodInfo {
        private List<String> paramNames;
        private String url;
    }
}
