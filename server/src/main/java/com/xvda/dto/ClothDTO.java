package com.xvda.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 衣物新增/编辑入参
 */
@Data
public class ClothDTO {

    /** 编辑时必传 */
    private Long id;

    private String name;

    /** 分类：1上装 2下装 3鞋 4配饰 */
    private Integer category;

    private String color;

    /** 适用季节，逗号分隔 */
    private String seasons;

    /** 适用场合，逗号分隔 */
    private String scenes;

    private String brand;

    private BigDecimal price;

    /** 上传接口返回的图片相对路径 */
    private String imageUrl;

    /** 是否归档：0否 1是 */
    private Integer archived;
}
