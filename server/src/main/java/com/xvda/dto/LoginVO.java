package com.xvda.dto;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String nickname;

    /** true 表示走开发 Mock 登录（服务端未配置 AppSecret） */
    private Boolean devMode;
}
