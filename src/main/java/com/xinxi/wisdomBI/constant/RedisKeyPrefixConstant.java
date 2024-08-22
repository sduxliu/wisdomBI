package com.xinxi.wisdomBI.constant;

/**
 * redis key前缀
 */
public interface RedisKeyPrefixConstant {

    String CAPTCHA_KEY = "CAPTCHA:";

    // 互斥锁：
    String MUTEX_LOCK_KEY = "MUTEX_LOCK:";

}
