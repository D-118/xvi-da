package com.xvda.controller;

import com.xvda.common.Result;
import com.xvda.dto.RecommendVO;
import com.xvda.service.RecommendService;
import com.xvda.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 智能推荐接口
 */
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;
    private final UserService userService;

    /**
     * 今日推荐：按天气 + 场合生成 2~3 套组合
     */
    @GetMapping("/today")
    public Result<RecommendVO> today(@RequestAttribute Long userId,
                                     @RequestParam(required = false) String scene) {
        // 用注册资料里的城市查天气
        String city = userService.getById(userId).getCity();
        return Result.ok(recommendService.recommend(userId, city, scene));
    }
}
