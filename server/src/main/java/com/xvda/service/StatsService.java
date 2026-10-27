package com.xvda.service;

import com.xvda.dto.StatsVO;
import com.xvda.entity.Cloth;

import java.util.List;

/**
 * 衣橱统计服务
 */
public interface StatsService {

    /** 总览：件数、分类分布、季节分布、打卡天数、方案数 */
    StatsVO overview(Long userId);

    /** 穿着频次 Top N（未删除衣物） */
    List<Cloth> wearTop(Long userId, int limit);

    /** 闲置衣物：从未穿过，或超过 idleDays 天未穿 */
    List<Cloth> idle(Long userId, int idleDays);
}
