package com.xvda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xvda.common.BizException;
import com.xvda.dto.RecommendVO;
import com.xvda.dto.WeatherVO;
import com.xvda.entity.Cloth;
import com.xvda.mapper.ClothMapper;
import com.xvda.service.RecommendService;
import com.xvda.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 推荐规则引擎（纯规则打分，不依赖外部模型）：
 * 1) 硬过滤：只取在柜且适用当前季节的衣物（该槽位无候选时放宽）
 * 2) 打分：场合匹配 / 气温-颜色适配 / 新鲜度（少穿的优先）/ 色彩呼应
 * 3) 组装：各槽位取分最高者成一套，再生成 2~3 套备选
 */
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final ClothMapper clothMapper;
    private final WeatherService weatherService;

    /** 气温高的天气适合浅色 */
    private static final List<String> LIGHT_COLORS = List.of("白", "米", "黄", "粉", "浅");
    /** 气温低的天气适合深色 */
    private static final List<String> DARK_COLORS = List.of("黑", "灰", "棕", "深", "紫");
    /** 经典上下装配色对 */
    private static final List<String> CLASSIC_PAIRS = List.of(
            "白|蓝", "白|黑", "黑|白", "灰|黑", "米|棕", "白|棕", "黑|蓝", "米|黑");

    @Override
    public RecommendVO recommend(Long userId, String city, String scene) {
        WeatherVO weather = weatherService.now(city);
        String season = weather.getSeason() == null ? seasonByMonth() : weather.getSeason();

        List<Cloth> all = clothMapper.selectList(new LambdaQueryWrapper<Cloth>()
                .eq(Cloth::getUserId, userId)
                .eq(Cloth::getArchived, 0)
                .orderByDesc(Cloth::getCreatedAt));
        if (all.size() < 3) {
            throw new BizException("衣橱里至少需要 3 件在柜衣物才能生成推荐");
        }

        // 按槽位分组，先按季节硬过滤，无候选再放宽
        Map<Integer, List<Cloth>> bySlot = all.stream().collect(Collectors.groupingBy(Cloth::getCategory));
        Map<Integer, List<Cloth>> candidates = bySlot.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    List<Cloth> inSeason = e.getValue().stream()
                            .filter(c -> c.getSeasons() != null && c.getSeasons().contains(season))
                            .collect(Collectors.toList());
                    return inSeason.isEmpty() ? e.getValue() : inSeason;
                }));

        List<String> reasonsCommon = new ArrayList<>();
        if (StringUtils.hasText(scene)) {
            reasonsCommon.add("按「" + scene + "」场合优选");
        }
        reasonsCommon.add(weather.getTip());

        List<RecommendVO.Combo> combos = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int want = 3;
        for (int n = 0; n < want; n++) {
            List<Cloth> picked = new ArrayList<>();
            List<String> reasons = new ArrayList<>(reasonsCommon);
            double total = 0;
            boolean complete = true;

            for (int slot = 1; slot <= 3; slot++) {
                List<Cloth> pool = candidates.get(slot);
                if (pool == null || pool.isEmpty()) {
                    complete = false;
                    break;
                }
                // 每套组合对衣物打分并排序，第 n 套取第 n 名（候选不足时回绕取首位）
                List<Scored> ranked = pool.stream()
                        .map(c -> score(c, weather, scene))
                        .sorted(Comparator.comparingDouble((Scored s) -> s.score).reversed())
                        .toList();
                int idx = Math.min(n, ranked.size() - 1);
                // 回绕会造成整套重复，改用去重签名跳过
                Scored best = ranked.get(idx);
                picked.add(best.cloth);
                total += best.score;
            }

            if (!complete || picked.size() < 3) continue;

            // 整套去重：三件 id 拼签名
            String signature = picked.stream().map(c -> String.valueOf(c.getId())).sorted().collect(Collectors.joining("-"));
            if (!seen.add(signature)) continue;

            // 上下装经典配色加分
            String top = picked.get(0).getColor(), bottom = picked.get(1).getColor();
            if (top != null && bottom != null) {
                String pair = top + "|" + bottom;
                if (CLASSIC_PAIRS.contains(pair)) {
                    total += 1.5;
                    reasons.add("上" + top + "下" + bottom + "是经典配色");
                }
            }

            RecommendVO.Combo combo = new RecommendVO.Combo();
            combo.setItems(picked);
            combo.setScore(Math.round(total * 10) / 10.0);
            combo.setReasons(reasons);
            combos.add(combo);
        }

        if (combos.isEmpty()) {
            throw new BizException("当前季节衣物不足，先去衣橱补充几件吧");
        }
        combos.sort(Comparator.comparingDouble(RecommendVO.Combo::getScore).reversed());

        RecommendVO vo = new RecommendVO();
        vo.setWeather(weather);
        vo.setCombos(combos);
        vo.setSummary("今日" + weather.getText() + " " + weather.getTemp() + "℃ · 按" + season + "季推荐");
        return vo;
    }

    /** 单件衣物打分 */
    private Scored score(Cloth c, WeatherVO weather, String scene) {
        double s = 1.0;
        List<String> why = new ArrayList<>();

        // 场合匹配
        if (StringUtils.hasText(scene) && c.getScenes() != null && c.getScenes().contains(scene)) {
            s += 2.0;
        }

        // 气温-颜色适配
        Integer temp = weather.getTemp();
        if (temp != null && c.getColor() != null) {
            if (temp >= 26 && LIGHT_COLORS.stream().anyMatch(c.getColor()::contains)) {
                s += 1.0;
                why.add("浅色散热，适合" + temp + "℃");
            } else if (temp <= 8 && DARK_COLORS.stream().anyMatch(c.getColor()::contains)) {
                s += 1.0;
                why.add("深色耐脏保暖，适合" + temp + "℃");
            }
        }

        // 新鲜度：少穿的优先，避免总是同一件
        int worn = c.getWearCount() == null ? 0 : c.getWearCount();
        if (worn == 0) {
            s += 1.5;
            why.add("还没穿过，快给新衣服机会");
        } else if (worn <= 3) {
            s += 0.5;
        } else {
            s -= Math.min(2.0, worn * 0.1);
        }

        return new Scored(c, s, why);
    }

    private String seasonByMonth() {
        int v = LocalDate.now().getMonthValue();
        if (v >= 6 && v <= 8) return "夏";
        if (v >= 3 && v <= 5) return "春";
        if (v >= 9 && v <= 11) return "秋";
        return "冬";
    }

    private record Scored(Cloth cloth, double score, List<String> why) {
    }
}
