package com.xinxi.wisdomBI.model.dto.user;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

/**
 * 用户注册请求体
 *
 * @author 蒲月理想
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPhone;

    private String userPassword;

    private String checkPassword;
    // 验证码 ,不属于用户表
//   @TableField(exist = false)
    private String captcha;
}
