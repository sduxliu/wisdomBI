package com.xinxi.wisdomBI.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图表VO
 * @author 蒲月理想
 */
// @Data 的注解是：Lombok 的注解，用于自动生成 getter、setter、equals、hashCode、toString 方法

@Data
public class ChartVO implements Serializable {
    /**
     * id
     */
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
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     * 任务状态
     */
    private String chartStatus;

    /**
     * 执行信息
     */
    private String execMessage;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;


    private static final long serialVersionUID = 1L;

}
