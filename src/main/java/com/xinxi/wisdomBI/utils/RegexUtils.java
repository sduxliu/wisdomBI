package com.xinxi.wisdomBI.utils;

import org.springframework.stereotype.Component;

@Component
public class RegexUtils {

    // 判断是否是邮箱
    public static boolean isEmail(String email) {
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        return email.matches(regex);
    }

    // 判断是否是手机号
    public static boolean isPhone(String phone) {
        String regex = "^1[3-9]\\d{9}$";
        return phone.matches(regex);
    }

    // 判断是否是六位验证码
    public static boolean isVerificationCode(String code) {
        String regex = "^\\d{6}$";
        return code.matches(regex);
    }
    // 判断账户是否有特殊字符
    public static boolean hasSpecialCharacters(String account) {
        String regex = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        return account.matches(regex);
    }
}
