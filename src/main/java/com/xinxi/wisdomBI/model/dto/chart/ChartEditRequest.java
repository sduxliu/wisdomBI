package com.xinxi.wisdomBI.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑请求
 *
 * @author 蒲月理想
 */
@Data
public class ChartEditRequest implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * id
     */
    private Long id;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 图表生成数据
     */
    private String genChart;

    private static final long serialVersionUID = 1L;
}