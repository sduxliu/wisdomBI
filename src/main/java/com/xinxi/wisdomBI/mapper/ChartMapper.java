package com.xinxi.wisdomBI.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinxi.wisdomBI.base.SqlEntity;
import com.xinxi.wisdomBI.model.entity.Chart;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * chart(图标信息表)Mapper
 *
 * @author 蒲月理想
 */
public interface ChartMapper extends BaseMapper<Chart> {

    List<Map<String, Object>> queryChartData(String querySql);
    /**
     * 创建 图表信息原始数据表
     *
     * @param chartId 图表id
     * @param colList 字段
     * @return boolean
     */
//    boolean creatChartTable(@Param("chartId") Long chartId, @Param("colList") List<SqlEntity> colList);

    /**
     * 插入图表信息原始数据
     *
     * @param chartId 图表id
     * @param dataMap 插入数据
     * @return 数据id
     */
//    int insertBatchChart(@Param("chartId") Long chartId, @Param("columns") List<String> columns, @Param("data") List<Map<String, Object>> dataMap);
}