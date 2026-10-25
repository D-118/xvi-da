package com.xvda.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 衣物实体（对应 cloth 表）
 */
@Data
@TableName("cloth")
public class Cloth {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 衣物名称 */
    private String name;

    /** 分类：1上装 2下装 3鞋 4配饰 */
    private Integer category;

    /** 主色（标准色名，推荐打分用） */
    private String color;

    /** 适用季节，逗号分隔：春,夏,秋,冬 */
    private String seasons;

    /** 适用场合，逗号分隔：通勤,休闲,运动,约会 */
    private String scenes;

    private String brand;

    private BigDecimal price;

    /** 图片地址（相对路径） */
    private String imageUrl;

    /** 累计穿着次数 */
    private Integer wearCount;

    /** 最近穿着日期（闲置提醒用） */
    private LocalDate lastWearDate;

    /** 是否归档：0否 1是 */
    private Integer archived;

    /** 逻辑删除：0否 1是 */
    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
}
