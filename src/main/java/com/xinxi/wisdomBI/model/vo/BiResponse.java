package com.xinxi.wisdomBI.model.vo;

import lombok.Data;

/**
 * Bi 的返回结果
 * @author 蒲月理想
 */
@Data
public class BiResponse {

    private String genChart;

    private String genResult;

    // 生成的图标id
    private Long chartId;
}
