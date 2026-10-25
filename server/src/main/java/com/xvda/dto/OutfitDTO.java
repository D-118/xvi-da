package com.xvda.dto;

import lombok.Data;

import java.util.List;

/**
 * 搭配方案保存入参
 */
@Data
public class OutfitDTO {

    /** 编辑时必传 */
    private Long id;

    private String name;

    /** 场合：通勤/休闲/运动/约会 */
    private String scene;

    /** 方案内衣物：[{clothId, slot}]，slot=1上装 2下装 3鞋 4配饰 */
    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        private Long clothId;
        private Integer slot;
    }
}
