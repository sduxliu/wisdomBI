package com.xinxi.wisdomBI.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 手机号验证登录
 * @author 蒲月理想
 */
@Data
public class UserLoginByPhoneRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userPhone;

    private String captcha;
}