package com.xvda.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xvda.dto.ClothDTO;
import com.xvda.entity.Cloth;

/**
 * 衣物业务接口
 */
public interface ClothService {

    /**
     * 新增衣物
     */
    Cloth add(Long userId, ClothDTO dto);

    /**
     * 编辑衣物（校验归属）
     */
    void update(Long userId, ClothDTO dto);

    /**
     * 删除衣物（逻辑删除；被搭配方案引用时拦截）
     */
    void delete(Long userId, Long id);

    /**
     * 详情（校验归属）
     */
    Cloth detail(Long userId, Long id);

    /**
     * 分页查询：分类/关键词/季节/归档筛选
     *
     * @param keyword  名称/品牌模糊搜索，可空
     * @param category 分类，可空
     * @param season   季节单选（从 seasons 逗号字段中匹配），可空
     * @param archived 0在柜 1已归档，null 全部
     */
    IPage<Cloth> page(Long userId, long page, long size,
                      String keyword, Integer category, String season, Integer archived);

    /**
     * 穿着次数 +1 并刷新最近穿着日期（打卡时调用）
     */
    void markWorn(Long userId, Long clothId);
}
