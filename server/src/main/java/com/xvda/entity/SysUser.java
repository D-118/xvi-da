package com.xvda.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信 openid */
    private String openid;

    private String nickname;

    private String avatarUrl;

    /** 城市（天气查询用） */
    private String city;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    /** 体型：苹果/梨形/沙漏/直筒/运动 */
    private String bodyType;

    /** 风格偏好，逗号分隔：简约,街头 */
    private String stylePrefs;

    private LocalDateTime createdAt;
}
