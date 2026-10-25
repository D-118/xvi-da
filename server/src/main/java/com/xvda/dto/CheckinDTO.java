package com.xvda.dto;

import lombok.Data;

import java.util.List;

/**
 * 打卡入参
 */
@Data
public class CheckinDTO {

    /** yyyy-MM-dd，不传=今天 */
    private String date;

    /** 来源方案，可空 */
    private Long outfitId;

    /** 当日穿的衣物 id 列表 */
    private List<Long> itemIds;

    /** 备注 */
    private String note;
}
