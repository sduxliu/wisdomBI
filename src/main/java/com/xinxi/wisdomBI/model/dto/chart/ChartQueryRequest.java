package com.xinxi.wisdomBI.model.dto.chart;

import com.xinxi.wisdomBI.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author 蒲月理想
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {

    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 任务状态
     */
    private String chartStatus;

    /**
     * 用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}