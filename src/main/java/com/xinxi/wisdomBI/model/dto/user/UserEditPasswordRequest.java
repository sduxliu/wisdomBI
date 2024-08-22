package com.xinxi.wisdomBI.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户修改密码请求
 *
 * @author 蒲月理想
 */
@Data
public class UserEditPasswordRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;
    private String userPhone;
    private String captcha;
    private String checkPassword;
    private String userPassword;
}