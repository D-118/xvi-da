package com.xvda.service;

import com.xvda.dto.OutfitDTO;
import com.xvda.dto.OutfitVO;
import com.xvda.entity.Outfit;

import java.util.List;

/**
 * 搭配方案业务接口
 */
public interface OutfitService {

    /**
     * 保存（新增或更新）。校验：至少上装+下装+鞋；衣物归属当前用户且未归档。
     */
    Outfit save(Long userId, OutfitDTO dto);

    /**
     * 方案列表（带内含衣物缩略信息）
     */
    List<OutfitVO> list(Long userId);

    /**
     * 方案详情
     */
    OutfitVO detail(Long userId, Long id);

    /**
     * 删除方案（顺带清理关联）
     */
    void delete(Long userId, Long id);
}
