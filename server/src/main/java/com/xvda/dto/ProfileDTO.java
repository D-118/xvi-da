package com.xvda.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户资料（查看/更新共用）
 */
@Data
public class ProfileDTO {

    private String nickname;
    private String avatarUrl;
    private String city;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String bodyType;
    private String stylePrefs;
}
