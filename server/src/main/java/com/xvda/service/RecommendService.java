package com.xvda.service;

import com.xvda.dto.RecommendVO;

/**
 * 穿搭推荐服务接口
 */
public interface RecommendService {

    /**
     * 按天气 + 场合推荐组合
     *
     * @param city  用户城市（天气查询）
     * @param scene 场合：通勤/休闲/运动/约会，可空=不限
     */
    RecommendVO recommend(Long userId, String city, String scene);
}
