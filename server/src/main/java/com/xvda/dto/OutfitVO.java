package com.xvda.dto;

import com.xvda.entity.Cloth;
import com.xvda.entity.Outfit;
import lombok.Data;

import java.util.List;

/**
 * 搭配方案详情出参：方案 + 内含衣物列表
 */
@Data
public class OutfitVO {

    private Long id;

    private String name;

    private String scene;

    private String createdAt;

    /** 内含衣物（带完整信息，前端直接渲染） */
    private List<Cloth> clothes;

    public static OutfitVO from(Outfit o, List<Cloth> clothes) {
        OutfitVO vo = new OutfitVO();
        vo.setId(o.getId());
        vo.setName(o.getName());
        vo.setScene(o.getScene());
        vo.setCreatedAt(o.getCreatedAt() == null ? null : o.getCreatedAt().toString());
        vo.setClothes(clothes);
        return vo;
    }
}
