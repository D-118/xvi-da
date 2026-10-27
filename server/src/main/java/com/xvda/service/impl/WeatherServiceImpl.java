package com.xvda.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xvda.dto.WeatherVO;
import com.xvda.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

/**
 * 天气服务实现：和风天气 v7 实时天气 + GeoAPI 城市定位，缓存 1 小时；无 key 走本地降级
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    @Value("${xvda.weather.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 天气缓存：city -> {vo, at} */
    private final Map<String, CacheEntry> cache = new HashMap<>();
    /** 城市定位缓存：city -> locationId */
    private final Map<String, String> locationCache = new HashMap<>();

    private static final long CACHE_MS = 60 * 60 * 1000;
    private static final String WEATHER_URL = "https://devapi.qweather.com/v7/weather/now?location=%s&key=%s";
    private static final String GEO_URL = "https://geoapi.qweather.com/v2/city/lookup?location=%s&key=%s";

    @Override
    public WeatherVO now(String city) {
        String key = city == null || city.isBlank() ? "哈尔滨" : city;
        CacheEntry hit = cache.get(key);
        if (hit != null && System.currentTimeMillis() - hit.at < CACHE_MS) {
            return hit.vo;
        }
        WeatherVO vo = hasKey() ? fetchReal(key) : fallback(key);
        cache.put(key, new CacheEntry(vo, System.currentTimeMillis()));
        return vo;
    }

    private boolean hasKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** 调和风天气：城市名 -> locationId -> 实时天气 */
    private WeatherVO fetchReal(String city) {
        try {
            String locationId = locationCache.computeIfAbsent(city, c -> resolveLocation(c));
            if (locationId == null) return fallback(city);

            String body = restTemplate.getForObject(String.format(WEATHER_URL, locationId, apiKey), String.class);
            JsonNode root = objectMapper.readTree(body);
            if (!"200".equals(root.path("code").asText())) {
                log.warn("和风天气返回异常 code={}", root.path("code").asText());
                return fallback(city);
            }
            JsonNode now = root.path("now");
            WeatherVO vo = new WeatherVO();
            vo.setText(now.path("text").asText());
            vo.setTemp(Integer.parseInt(now.path("temp").asText("20")));
            vo.setSeason(seasonOf(vo.getTemp(), LocalDate.now().getMonth()));
            vo.setTip(tipOf(vo));
            vo.setReal(true);
            return vo;
        } catch (Exception e) {
            log.warn("天气查询失败，降级处理: {}", e.getMessage());
            return fallback(city);
        }
    }

    private String resolveLocation(String city) {
        try {
            String body = restTemplate.getForObject(String.format(GEO_URL, city, apiKey), String.class);
            JsonNode root = objectMapper.readTree(body);
            return root.path("location").path(0).path("id").asText(null);
        } catch (Exception e) {
            log.warn("城市定位失败 {}: {}", city, e.getMessage());
            return null;
        }
    }

    /** 降级：按月份推算季节与典型温度（哈尔滨参照） */
    private WeatherVO fallback(String city) {
        Month m = LocalDate.now().getMonth();
        int temp;
        String text;
        switch (m) {
            case DECEMBER, JANUARY, FEBRUARY -> { temp = -12; text = "晴（寒冷）"; }
            case MARCH, APRIL, MAY -> { temp = 10; text = "多云（转暖）"; }
            case JUNE, JULY, AUGUST -> { temp = 25; text = "晴（炎热）"; }
            default -> { temp = 8; text = "晴（微凉）"; }
        }
        WeatherVO vo = new WeatherVO();
        vo.setText(text);
        vo.setTemp(temp);
        vo.setSeason(seasonOf(temp, m));
        vo.setTip(tipOf(vo));
        vo.setReal(false);
        return vo;
    }

    /** 气温优先、月份兜底 */
    private String seasonOf(Integer temp, Month m) {
        if (temp != null) {
            if (temp >= 24) return "夏";
            if (temp >= 12) return "春";
            if (temp >= 2) return "秋";
            return "冬";
        }
        int v = m.getValue();
        if (v >= 6 && v <= 8) return "夏";
        if (v >= 3 && v <= 5) return "春";
        if (v >= 9 && v <= 11) return "秋";
        return "冬";
    }

    private String tipOf(WeatherVO vo) {
        if (vo.getTemp() == null) return "";
        int t = vo.getTemp();
        if (t <= 0) return "冰点以下，建议厚外套+保暖内搭";
        if (t <= 12) return "偏凉，外套备上";
        if (t >= 28) return "高温天，浅色透气优先";
        if (vo.getText() != null && vo.getText().contains("雨")) return "有降水，别忘带伞";
        return "体感舒适，随意搭";
    }

    private record CacheEntry(WeatherVO vo, long at) {
    }
}
