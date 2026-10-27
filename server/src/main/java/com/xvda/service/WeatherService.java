package com.xvda.service;

import com.xvda.dto.WeatherVO;

/**
 * 天气服务接口
 */
public interface WeatherService {

    /**
     * 查询某城市实时天气（内存缓存 1 小时）
     * 未配置和风天气 key 时，按月份做本地降级推算
     */
    WeatherVO now(String city);
}
