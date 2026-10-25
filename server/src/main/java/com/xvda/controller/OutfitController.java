package com.xvda.controller;

import com.xvda.common.Result;
import com.xvda.dto.OutfitDTO;
import com.xvda.dto.OutfitVO;
import com.xvda.entity.Outfit;
import com.xvda.service.OutfitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搭配方案接口
 */
@RestController
@RequestMapping("/api/outfit")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitService outfitService;

    /**
     * 保存方案（新增或编辑）
     */
    @PostMapping
    public Result<Outfit> save(@RequestAttribute Long userId, @RequestBody OutfitDTO dto) {
        return Result.ok(outfitService.save(userId, dto));
    }

    /**
     * 我的方案列表
     */
    @GetMapping("/list")
    public Result<List<OutfitVO>> list(@RequestAttribute Long userId) {
        return Result.ok(outfitService.list(userId));
    }

    /**
     * 方案详情
     */
    @GetMapping("/{id}")
    public Result<OutfitVO> detail(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.ok(outfitService.detail(userId, id));
    }

    /**
     * 删除方案
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute Long userId, @PathVariable Long id) {
        outfitService.delete(userId, id);
        return Result.ok();
    }
}
