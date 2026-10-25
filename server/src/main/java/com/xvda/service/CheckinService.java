package com.xvda.service;

import com.xvda.dto.CheckinDTO;
import com.xvda.entity.CalendarCheckin;

import java.util.List;

/**
 * 穿搭打卡业务接口
 */
public interface CheckinService {

    /**
     * 打卡（同日重复打卡 = 覆盖更新，穿着次数差额修正）
     */
    CalendarCheckin checkin(Long userId, CheckinDTO dto);

    /**
     * 查某月所有打卡
     */
    List<CalendarCheckin> month(Long userId, String month);

    /**
     * 某天打卡详情
     */
    CalendarCheckin ofDate(Long userId, String date);

    /**
     * 取消某天打卡（穿着次数回退）
     */
    void cancel(Long userId, String date);
}
