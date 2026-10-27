package com.xvda.dto;

import lombok.Data;

/**
 * 天气信息出参
 */
@Data
public class WeatherVO {

    /** 天气现象文字：晴/多云/小雨… */
    private String text;

    /** 当前气温 ℃ */
    private Integer temp;

    /** 据气温与月份推导的季节：春/夏/秋/冬 */
    private String season;

    /** 提示语 */
    private String tip;

    /** 是否真实数据（false=未配置 key 的降级推算） */
    private boolean real;
}
