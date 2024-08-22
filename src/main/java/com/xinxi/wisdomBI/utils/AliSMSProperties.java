package com.xinxi.wisdomBI.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 蒲月理想
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliSMSProperties {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;
}
