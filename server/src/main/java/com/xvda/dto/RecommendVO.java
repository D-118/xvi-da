package com.xvda.dto;

import com.xvda.entity.Cloth;
import lombok.Data;

import java.util.List;

/**
 * 推荐结果出参
 */
@Data
public class RecommendVO {

    private WeatherVO weather;

    /** 推荐组合（按得分排序） */
    private List<Combo> combos;

    /** 汇总说明 */
    private String summary;

    @Data
    public static class Combo {
        /** 组合内衣物 */
        private List<Cloth> items;

        /** 得分 */
        private double score;

        /** 推荐理由（逐条） */
        private List<String> reasons;
    }
}
