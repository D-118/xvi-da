package com.xvda.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搭配方案实体（对应 outfit 表）
 */
@Data
@TableName("outfit")
public class Outfit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    /** 场合：通勤/休闲/运动/约会 */
    private String scene;

    private LocalDateTime createdAt;
}
