package com.xinxi.wisdomBI.utils;

import cn.hutool.json.JSONUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author 蒲月理想
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliSMSUtils {

    private final AliSMSProperties aliSMSProperties;
    public boolean sendSms(String phoneNumber, HashMap<String, Object> templateParams) {
        try {
            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", aliSMSProperties.getAccessKeyId(), aliSMSProperties.getAccessKeySecret());
            IAcsClient client = new DefaultAcsClient(profile);

            SendSmsRequest request = new SendSmsRequest();
            request.setSysRegionId("cn-hangzhou");
            request.setPhoneNumbers(phoneNumber);
            request.setSignName(aliSMSProperties.getSignName());
            request.setTemplateCode(aliSMSProperties.getTemplateCode());
            // 将HashMap转化为JSON字符串
            String templateParam = JSONUtil.toJsonStr(templateParams);
            request.setTemplateParam(templateParam);
            log.info("发送短信请求参数：{}", request);
            SendSmsResponse response = client.getAcsResponse(request);

            return "OK".equals(response.getCode());
        } catch (ClientException e) {
            e.printStackTrace();
            return false;
        }
    }
}

