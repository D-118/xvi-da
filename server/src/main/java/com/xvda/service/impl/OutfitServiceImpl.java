package com.xvda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xvda.common.BizException;
import com.xvda.dto.OutfitDTO;
import com.xvda.dto.OutfitVO;
import com.xvda.entity.Cloth;
import com.xvda.entity.Outfit;
import com.xvda.entity.OutfitItem;
import com.xvda.mapper.ClothMapper;
import com.xvda.mapper.OutfitItemMapper;
import com.xvda.mapper.OutfitMapper;
import com.xvda.service.OutfitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 搭配方案业务实现
 */
@Service
@RequiredArgsConstructor
public class OutfitServiceImpl implements OutfitService {

    private final OutfitMapper outfitMapper;
    private final OutfitItemMapper outfitItemMapper;
    private final ClothMapper clothMapper;

    @Override
    @Transactional
    public Outfit save(Long userId, OutfitDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BizException("请至少选择一件衣物");
        }
        // 上装+下装+鞋 是一套的基础，配饰可选
        Set<Integer> slots = dto.getItems().stream().map(OutfitDTO.ItemDTO::getSlot).collect(Collectors.toSet());
        for (int required : new int[]{1, 2, 3}) {
            if (!slots.contains(required)) {
                throw new BizException("完整搭配需要：上装 + 下装 + 鞋");
            }
        }
        if (slots.size() < dto.getItems().size()) {
            throw new BizException("同一槽位不能重复选择");
        }

        // 校验衣物归属与状态
        List<Long> clothIds = dto.getItems().stream().map(OutfitDTO.ItemDTO::getClothId).toList();
        List<Cloth> cloths = clothMapper.selectBatchIds(clothIds);
        Map<Long, Cloth> clothMap = cloths.stream().collect(Collectors.toMap(Cloth::getId, Function.identity()));
        for (Long cid : clothIds) {
            Cloth c = clothMap.get(cid);
            if (c == null || c.getUserId() != userId) {
                throw new BizException("衣物不存在或不属于当前用户");
            }
            if (c.getArchived() == 1) {
                throw new BizException("「" + c.getName() + "」已归档，请先取回在柜");
            }
        }

        Outfit outfit = new Outfit();
        outfit.setUserId(userId);
        outfit.setName(StringUtils.hasText(dto.getName()) ? dto.getName() : "未命名搭配");
        outfit.setScene(dto.getScene());
        if (dto.getId() != null) {
            outfit.setId(dto.getId());
            getOwned(userId, dto.getId());
            outfitMapper.updateById(outfit);
            outfitItemMapper.delete(new LambdaQueryWrapper<OutfitItem>().eq(OutfitItem::getOutfitId, dto.getId()));
        } else {
            outfitMapper.insert(outfit);
        }

        for (OutfitDTO.ItemDTO item : dto.getItems()) {
            OutfitItem oi = new OutfitItem();
            oi.setOutfitId(outfit.getId());
            oi.setClothId(item.getClothId());
            oi.setSlot(item.getSlot());
            outfitItemMapper.insert(oi);
        }
        return outfit;
    }

    @Override
    public List<OutfitVO> list(Long userId) {
        List<Outfit> outfits = outfitMapper.selectList(
                new LambdaQueryWrapper<Outfit>().eq(Outfit::getUserId, userId).orderByDesc(Outfit::getCreatedAt));
        List<OutfitVO> result = new ArrayList<>();
        for (Outfit o : outfits) {
            result.add(detail(userId, o.getId()));
        }
        return result;
    }

    @Override
    public OutfitVO detail(Long userId, Long id) {
        Outfit outfit = getOwned(userId, id);
        List<OutfitItem> items = outfitItemMapper.selectList(
                new LambdaQueryWrapper<OutfitItem>().eq(OutfitItem::getOutfitId, id).orderByAsc(OutfitItem::getSlot));
        List<Long> clothIds = items.stream().map(OutfitItem::getClothId).toList();
        List<Cloth> cloths = clothIds.isEmpty() ? List.of() : clothMapper.selectBatchIds(clothIds);
        // 已被删除的衣物自然查不出来，按槽位排序保持展示顺序
        cloths.sort((a, b) -> {
            int sa = slotOf(items, a.getId());
            int sb = slotOf(items, b.getId());
            return Integer.compare(sa, sb);
        });
        return OutfitVO.from(outfit, cloths);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        getOwned(userId, id);
        outfitItemMapper.delete(new LambdaQueryWrapper<OutfitItem>().eq(OutfitItem::getOutfitId, id));
        outfitMapper.deleteById(id);
    }

    // ---------- 私有 ----------

    private Outfit getOwned(Long userId, Long id) {
        Outfit o = outfitMapper.selectById(id);
        if (o == null || !o.getUserId().equals(userId)) {
            throw new BizException("方案不存在");
        }
        return o;
    }

    private int slotOf(List<OutfitItem> items, Long clothId) {
        return items.stream().filter(i -> i.getClothId().equals(clothId))
                .findFirst().map(OutfitItem::getSlot).orElse(9);
    }
}
