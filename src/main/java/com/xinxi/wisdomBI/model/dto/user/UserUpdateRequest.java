package com.xinxi.wisdomBI.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新请求
 *
 * @author 蒲月理想
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 更新用户角色：user/admin/ban
     */
    private String userRole;
    /**
     * 更新用户状态
     *
     */


    private static final long serialVersionUID = 1L;
}