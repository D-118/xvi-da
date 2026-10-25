package com.xvda.controller;

import com.xvda.common.Result;
import com.xvda.dto.CheckinDTO;
import com.xvda.entity.CalendarCheckin;
import com.xvda.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 穿搭日历打卡接口
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    /**
     * 打卡（同日覆盖）
     */
    @PostMapping("/checkin")
    public Result<CalendarCheckin> checkin(@RequestAttribute Long userId, @RequestBody CheckinDTO dto) {
        return Result.ok(checkinService.checkin(userId, dto));
    }

    /**
     * 某月打卡列表：?month=2026-10
     */
    @GetMapping("/month")
    public Result<List<CalendarCheckin>> month(@RequestAttribute Long userId,
                                               @RequestParam(required = false) String month) {
        return Result.ok(checkinService.month(userId, month));
    }

    /**
     * 某天打卡详情：?date=2026-10-25
     */
    @GetMapping("/date")
    public Result<CalendarCheckin> ofDate(@RequestAttribute Long userId, @RequestParam String date) {
        return Result.ok(checkinService.ofDate(userId, date));
    }

    /**
     * 取消某天打卡
     */
    @DeleteMapping("/checkin")
    public Result<Void> cancel(@RequestAttribute Long userId, @RequestParam String date) {
        checkinService.cancel(userId, date);
        return Result.ok();
    }
}
