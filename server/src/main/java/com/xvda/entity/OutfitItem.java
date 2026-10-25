package com.xvda.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 搭配方案-衣物关联实体（对应 outfit_item 表）
 */
@Data
@TableName("outfit_item")
public class OutfitItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long outfitId;

    private Long clothId;

    /** 槽位：1上装 2下装 3鞋 4配饰 */
    private Integer slot;
}
