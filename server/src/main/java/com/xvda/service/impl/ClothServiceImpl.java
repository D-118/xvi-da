package com.xvda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xvda.common.BizException;
import com.xvda.dto.ClothDTO;
import com.xvda.entity.Cloth;
import com.xvda.mapper.ClothMapper;
import com.xvda.mapper.OutfitItemMapper;
import com.xvda.service.ClothService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 衣物业务实现
 */
@Service
@RequiredArgsConstructor
public class ClothServiceImpl implements ClothService {

    private final ClothMapper clothMapper;
    private final OutfitItemMapper outfitItemMapper;

    @Override
    public Cloth add(Long userId, ClothDTO dto) {
        validate(dto);
        Cloth c = new Cloth();
        copy(dto, c);
        c.setUserId(userId);
        c.setWearCount(0);
        c.setArchived(dto.getArchived() == null ? 0 : dto.getArchived());
        clothMapper.insert(c);
        return c;
    }

    @Override
    public void update(Long userId, ClothDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("缺少衣物 id");
        }
        Cloth db = getOwned(userId, dto.getId());
        validate(dto);
        copy(dto, db);
        if (dto.getArchived() != null) {
            db.setArchived(dto.getArchived());
        }
        clothMapper.updateById(db);
    }

    @Override
    public void delete(Long userId, Long id) {
        getOwned(userId, id);
        // 被搭配方案引用时拦截，避免破坏已有方案
        Long refCount = outfitItemMapper.selectCount(
                new LambdaQueryWrapper<com.xvda.entity.OutfitItem>()
                        .eq(com.xvda.entity.OutfitItem::getClothId, id));
        if (refCount != null && refCount > 0) {
            throw new BizException("该衣物已被 " + refCount + " 个搭配方案引用，请先在搭配器中移除后再删除");
        }
        clothMapper.deleteById(id);
    }

    @Override
    public Cloth detail(Long userId, Long id) {
        return getOwned(userId, id);
    }

    @Override
    public IPage<Cloth> page(Long userId, long page, long size,
                             String keyword, Integer category, String season, Integer archived) {
        LambdaQueryWrapper<Cloth> qw = new LambdaQueryWrapper<Cloth>()
                .eq(Cloth::getUserId, userId)
                .eq(category != null, Cloth::getCategory, category)
                .eq(archived != null, Cloth::getArchived, archived)
                .like(StringUtils.hasText(season), Cloth::getSeasons, season)
                .orderByDesc(Cloth::getCreatedAt);
        // 名称或品牌模糊搜索，加括号分组避免与季节条件混叠
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(Cloth::getName, keyword).or().like(Cloth::getBrand, keyword));
        }
        return clothMapper.selectPage(new Page<>(page, size), qw);
    }

    @Override
    public void markWorn(Long userId, Long clothId) {
        Cloth c = getOwned(userId, clothId);
        c.setWearCount((c.getWearCount() == null ? 0 : c.getWearCount()) + 1);
        c.setLastWearDate(LocalDate.now());
        clothMapper.updateById(c);
    }

    // ---------- 私有方法 ----------

    private Cloth getOwned(Long userId, Long id) {
        Cloth c = clothMapper.selectById(id);
        if (c == null || c.getDeleted() == 1) {
            throw new BizException("衣物不存在或已删除");
        }
        if (!c.getUserId().equals(userId)) {
            // 不暴露他人衣物是否存在
            throw new BizException("衣物不存在或已删除");
        }
        return c;
    }

    private void validate(ClothDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BizException("请填写衣物名称");
        }
        if (dto.getName().length() > 50) {
            throw new BizException("衣物名称不能超过 50 字");
        }
        if (dto.getCategory() == null || dto.getCategory() < 1 || dto.getCategory() > 4) {
            throw new BizException("请选择正确分类：1上装 2下装 3鞋 4配饰");
        }
        if (!StringUtils.hasText(dto.getImageUrl())) {
            throw new BizException("请上传衣物图片");
        }
    }

    private void copy(ClothDTO from, Cloth to) {
        to.setName(from.getName());
        to.setCategory(from.getCategory());
        to.setColor(from.getColor());
        to.setSeasons(from.getSeasons());
        to.setScenes(from.getScenes());
        to.setBrand(from.getBrand());
        to.setPrice(from.getPrice());
        to.setImageUrl(from.getImageUrl());
    }
}
