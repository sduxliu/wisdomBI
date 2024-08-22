package com.xinxi.wisdomBI.base;

import lombok.Data;

/**
 * SqlEntity sql实体
 *
 * @author 蒲月理想
 */
@Data
public class SqlEntity {

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 字段类型
     */
    private String columnType;
}