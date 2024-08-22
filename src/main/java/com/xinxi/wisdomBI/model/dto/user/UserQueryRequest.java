package com.xinxi.wisdomBI.model.dto.user;

import java.io.Serializable;

import com.xinxi.wisdomBI.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询请求
 *
 * @author 蒲月理想
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;
    /**
     * 用户性别
     */
    private Integer userGender;
    private static final long serialVersionUID = 1L;
}