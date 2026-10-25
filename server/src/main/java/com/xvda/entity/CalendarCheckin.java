package com.xvda.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 穿搭日历打卡实体（对应 calendar_checkin 表）
 */
@Data
@TableName("calendar_checkin")
public class CalendarCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 打卡日期 */
    private LocalDate checkinDate;

    /** 来源方案id（可空=临时组合） */
    private Long outfitId;

    /** 当日衣物id串，冗余防衣物删除后失效 */
    private String itemIds;

    /** JSON快照：[{clothId,name,imageUrl,slot}] */
    private String snapshot;

    private String note;

    private LocalDateTime createdAt;
}
