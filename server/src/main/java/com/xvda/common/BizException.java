package com.xvda.common;

import lombok.Getter;

/**
 * 业务异常：msg 为面向用户的中文提示
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String msg) {
        this(500, msg);
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
