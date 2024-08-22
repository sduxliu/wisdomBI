package com.xinxi.wisdomBI.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * &#064;description:  图表状态枚举
 * @author 蒲月理想
 */

@Getter
public enum ChartStatusEnum {
    SUCCEED("成功","succeed"),
    FAIL("失败","failed"),
    WATT("等待","wait"),
    RUNNING("运行中","running");

    private final String text;
    private final String value;

    ChartStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
    /**
     * 获取值列表
     */
    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }
    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ChartStatusEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ChartStatusEnum anEnum : ChartStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
