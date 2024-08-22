package com.xinxi.wisdomBI.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 删除请求
 * @author 蒲月理想
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}