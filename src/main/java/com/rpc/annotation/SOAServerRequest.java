package com.rpc.annotation;

import com.rpc.model.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * <p>可定制URI和http方法</p>
 *
 * @author dl
 * @Date 2017/3/29 10:03
 */
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SOAServerRequest {
    String value();

    HttpMethod httpMethod() default HttpMethod.GET;
}
