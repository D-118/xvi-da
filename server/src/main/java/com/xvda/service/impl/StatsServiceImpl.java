package com.xvda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xvda.dto.StatsVO;
import com.xvda.entity.Cloth;
import com.xvda.mapper.CalendarCheckinMapper;
import com.xvda.mapper.ClothMapper;
import com.xvda.mapper.OutfitMapper;
import com.xvda.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final String[] CATEGORY_LABELS = {"", "上装", "下装", "鞋", "配饰"};
    private static final String[] SEASONS = {"春", "夏", "秋", "冬"};

    private final ClothMapper clothMapper;
    private final CalendarCheckinMapper checkinMapper;
    private final OutfitMapper outfitMapper;

    @Override
    public StatsVO overview(Long userId) {
        StatsVO vo = new StatsVO();

        vo.setTotal(clothMapper.selectCount(new LambdaQueryWrapper<Cloth>()
                .eq(Cloth::getUserId, userId)
                .eq(Cloth::getArchived, 0)));

        vo.setArchived(clothMapper.selectCount(new LambdaQueryWrapper<Cloth>()
                .eq(Cloth::getUserId, userId)
                .eq(Cloth::getArchived, 1)));

        vo.setCheckinDays(checkinMapper.selectCount(new LambdaQueryWrapper<com.xvda.entity.CalendarCheckin>()
                .eq(com.xvda.entity.CalendarCheckin::getUserId, userId)));

        vo.setOutfitCount(outfitMapper.selectCount(new LambdaQueryWrapper<com.xvda.entity.Outfit>()
                .eq(com.xvda.entity.Outfit::getUserId, userId)));

        // 分类分布（含归档，反映整个衣橱资产）
        List<StatsVO.CategoryCount> byCategory = new ArrayList<>();
        List<Cloth> all = clothMapper.selectList(new LambdaQueryWrapper<Cloth>()
                .eq(Cloth::getUserId, userId)
                .select(Cloth::getCategory));
        for (int cat = 1; cat <= 4; cat++) {
            final int c = cat;
            long n = all.stream().filter(x -> x.getCategory() == c).count();
            byCategory.add(new StatsVO.CategoryCount(cat, CATEGORY_LABELS[cat], n));
        }
        vo.setByCategory(byCategory);

        // 季节分布：按 seasons 逗号拆分统计（一件多季各计一次）
        List<StatsVO.SeasonCount> bySeason = new ArrayList<>();
        List<Cloth> seasonRows = clothMapper.selectList(new LambdaQueryWrapper<Cloth>()
                .eq(Cloth::getUserId, userId)
                .select(Cloth::getSeasons));
        long[] bucket = new long[4];
        for (Cloth c : seasonRows) {
            if (c.getSeasons() == null) continue;
            for (String s : c.getSeasons().split(",")) {
                for (int i = 0; i < SEASONS.length; i++) {
                    if (SEASONS[i].equals(s.trim())) bucket[i]++;
                }
            }
        }
        for (int i = 0; i < SEASONS.length; i++) {
            bySeason.add(new StatsVO.SeasonCount(SEASONS[i], bucket[i]));
        }
        vo.setBySeason(bySeason);

        return vo;
    }

    @Override
    public List<Cloth> wearTop(Long userId, int limit) {
        return clothMapper.selectList(new LambdaQueryWrapper<Cloth>()
                .eq(Cloth::getUserId, userId)
                .orderByDesc(Cloth::getWearCount)
                .orderByAsc(Cloth::getName)
                .last("LIMIT " + Math.max(1, Math.min(limit, 50))));
    }

    @Override
    public List<Cloth> idle(Long userId, int idleDays) {
        LocalDate threshold = LocalDate.now().minusDays(idleDays);
        return clothMapper.selectList(new LambdaQueryWrapper<Cloth>()
                .eq(Cloth::getUserId, userId)
                .eq(Cloth::getArchived, 0)
                .and(w -> w.eq(Cloth::getWearCount, 0)
                        .or()
                        .lt(Cloth::getLastWearDate, threshold))
                .orderByAsc(Cloth::getWearCount)
                .last("LIMIT 50"));
    }
}
