package com.xinxi.wisdomBI.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 蒲月理想
 */
@Data
public class UserSendCaptchaRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;

    String userPhone;

}
