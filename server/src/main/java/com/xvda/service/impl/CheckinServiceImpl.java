package com.xvda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xvda.common.BizException;
import com.xvda.dto.CheckinDTO;
import com.xvda.entity.CalendarCheckin;
import com.xvda.entity.Cloth;
import com.xvda.mapper.CalendarCheckinMapper;
import com.xvda.mapper.ClothMapper;
import com.xvda.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 穿搭打卡业务实现
 */
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final CalendarCheckinMapper checkinMapper;
    private final ClothMapper clothMapper;
    private final ObjectMapper objectMapper;

    /** 打卡快照条目 */
    public record SnapItem(Long clothId, String name, String imageUrl, Integer slot) {
    }

    @Override
    @Transactional
    public CalendarCheckin checkin(Long userId, CheckinDTO dto) {
        LocalDate date = parseDate(dto.getDate(), LocalDate.now());
        if (date.isAfter(LocalDate.now())) {
            throw new BizException("不能给未来的日子打卡哦");
        }
        if (dto.getItemIds() == null || dto.getItemIds().isEmpty()) {
            throw new BizException("请选择当日穿搭");
        }

        // 校验衣物归属并生成快照
        List<Cloth> cloths = clothMapper.selectBatchIds(dto.getItemIds());
        Map<Long, Cloth> clothMap = cloths.stream()
                .filter(c -> c.getUserId().equals(userId) && c.getArchived() == 0)
                .collect(Collectors.toMap(Cloth::getId, Function.identity()));
        if (clothMap.size() < dto.getItemIds().stream().distinct().count()) {
            throw new BizException("部分衣物不存在、已归档或不属于你");
        }
        List<SnapItem> snapshot = dto.getItemIds().stream().map(id -> {
            Cloth c = clothMap.get(id);
            return new SnapItem(c.getId(), c.getName(), c.getImageUrl(), c.getCategory());
        }).toList();

        // 同日覆盖：先回退旧次数
        CalendarCheckin old = ofDate(userId, date.toString());
        if (old != null && old.getItemIds() != null && !old.getItemIds().isBlank()) {
            for (String s : old.getItemIds().split(",")) {
                try {
                    unmarkWorn(clothMap.containsKey(Long.parseLong(s)) ? clothMap.get(Long.parseLong(s)) : clothMapper.selectById(Long.parseLong(s)));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String snapJson = toJson(snapshot);
        CalendarCheckin checkin = new CalendarCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(date);
        checkin.setOutfitId(dto.getOutfitId());
        checkin.setItemIds(dto.getItemIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        checkin.setSnapshot(snapJson);
        checkin.setNote(dto.getNote());

        if (old != null) {
            checkin.setId(old.getId());
            checkinMapper.updateById(checkin);
        } else {
            checkinMapper.insert(checkin);
        }

        // 新穿搭次数累加
        for (Long id : dto.getItemIds()) {
            markWorn(clothMap.get(id));
        }
        return checkin;
    }

    @Override
    public List<CalendarCheckin> month(Long userId, String month) {
        YearMonth ym;
        try {
            ym = YearMonth.parse(month == null || month.isBlank() ? LocalDate.now().toString().substring(0, 7) : month);
        } catch (DateTimeParseException e) {
            throw new BizException("月份格式应为 yyyy-MM");
        }
        return checkinMapper.selectList(new LambdaQueryWrapper<CalendarCheckin>()
                .eq(CalendarCheckin::getUserId, userId)
                .ge(CalendarCheckin::getCheckinDate, ym.atDay(1))
                .le(CalendarCheckin::getCheckinDate, ym.atEndOfMonth())
                .orderByAsc(CalendarCheckin::getCheckinDate));
    }

    @Override
    public CalendarCheckin ofDate(Long userId, String date) {
        return checkinMapper.selectOne(new LambdaQueryWrapper<CalendarCheckin>()
                .eq(CalendarCheckin::getUserId, userId)
                .eq(CalendarCheckin::getCheckinDate, parseDate(date, null)));
    }

    @Override
    @Transactional
    public void cancel(Long userId, String date) {
        CalendarCheckin old = ofDate(userId, date);
        if (old == null) {
            throw new BizException("该日期没有打卡记录");
        }
        // 回退穿着次数
        if (old.getItemIds() != null && !old.getItemIds().isBlank()) {
            for (String s : old.getItemIds().split(",")) {
                try {
                    unmarkWorn(clothMapper.selectById(Long.parseLong(s)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        checkinMapper.deleteById(old.getId());
    }

    // ---------- 私有 ----------

    private void markWorn(Cloth c) {
        if (c == null) return;
        c.setWearCount((c.getWearCount() == null ? 0 : c.getWearCount()) + 1);
        c.setLastWearDate(LocalDate.now());
        clothMapper.updateById(c);
    }

    private void unmarkWorn(Cloth c) {
        if (c == null) return;
        c.setWearCount(Math.max(0, (c.getWearCount() == null ? 0 : c.getWearCount()) - 1));
        clothMapper.updateById(c);
    }

    private LocalDate parseDate(String date, LocalDate fallback) {
        if (date == null || date.isBlank()) return fallback;
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new BizException("日期格式应为 yyyy-MM-dd");
        }
    }

    private String toJson(List<SnapItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            throw new BizException("快照生成失败");
        }
    }
}
