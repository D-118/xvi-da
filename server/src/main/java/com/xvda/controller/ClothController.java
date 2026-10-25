package com.xvda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xvda.common.Result;
import com.xvda.dto.ClothDTO;
import com.xvda.entity.Cloth;
import com.xvda.service.ClothService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 衣物管理接口
 */
@RestController
@RequestMapping("/api/cloth")
@RequiredArgsConstructor
public class ClothController {

    private final ClothService clothService;

    /**
     * 新增衣物
     */
    @PostMapping
    public Result<Cloth> add(@RequestAttribute Long userId, @RequestBody ClothDTO dto) {
        return Result.ok(clothService.add(userId, dto));
    }

    /**
     * 编辑衣物
     */
    @PutMapping
    public Result<Void> update(@RequestAttribute Long userId, @RequestBody ClothDTO dto) {
        clothService.update(userId, dto);
        return Result.ok(null);
    }

    /**
     * 删除衣物（被方案引用时会被拦截）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute Long userId, @PathVariable Long id) {
        clothService.delete(userId, id);
        return Result.ok(null);
    }

    /**
     * 衣物详情
     */
    @GetMapping("/{id}")
    public Result<Cloth> detail(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.ok(clothService.detail(userId, id));
    }

    /**
     * 分页查询
     * 参数：page/size/keyword/category/season/archived
     */
    @GetMapping("/page")
    public Result<IPage<Cloth>> page(@RequestAttribute Long userId,
                                     @RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "12") long size,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Integer category,
                                     @RequestParam(required = false) String season,
                                     @RequestParam(required = false) Integer archived) {
        return Result.ok(clothService.page(userId, page, size, keyword, category, season, archived));
    }
}
