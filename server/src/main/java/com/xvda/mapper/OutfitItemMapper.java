package com.xvda.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xvda.entity.OutfitItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 搭配方案-衣物关联 Mapper
 */
@Mapper
public interface OutfitItemMapper extends BaseMapper<OutfitItem> {
}
