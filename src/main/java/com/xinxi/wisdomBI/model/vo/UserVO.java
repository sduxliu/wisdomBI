package com.xinxi.wisdomBI.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户视图（脱敏）
 *
 * @author 蒲月理想
 */
@Data
public class UserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;
    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 用户电话
     */
    private String userPhone;
    /**
     * 用户性别
     */
    private Integer userGender;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}