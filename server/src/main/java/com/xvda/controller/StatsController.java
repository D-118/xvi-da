package com.xvda.controller;

import com.xvda.common.Result;
import com.xvda.dto.StatsVO;
import com.xvda.entity.Cloth;
import com.xvda.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 衣橱统计接口（个人中心）
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** 总览：件数/分类/季节/打卡天数/方案数 */
    @GetMapping("/overview")
    public Result<StatsVO> overview(@RequestAttribute Long userId) {
        return Result.ok(statsService.overview(userId));
    }

    /** 穿着频次 Top N，默认 5 */
    @GetMapping("/wear-top")
    public Result<List<Cloth>> wearTop(@RequestAttribute Long userId,
                                       @RequestParam(defaultValue = "5") int limit) {
        return Result.ok(statsService.wearTop(userId, limit));
    }

    /** 闲置衣物：从未穿过或超过 idleDays（默认30）天未穿 */
    @GetMapping("/idle")
    public Result<List<Cloth>> idle(@RequestAttribute Long userId,
                                    @RequestParam(defaultValue = "30") int idleDays) {
        return Result.ok(statsService.idle(userId, idleDays));
    }
}
