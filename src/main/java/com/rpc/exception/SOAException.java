package com.rpc.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p></p>
 *
 * @author dl
 * @Date 2017/3/29 10:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SOAException extends Exception {
    private String msg;
}
