package com.xinxi.wisdomBI.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 用户消息处理失败的
 * @author 蒲月理想
 */
@Component
public class ErrorMessageReceiver {
    @Bean
   public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate){
        return new RepublishMessageRecoverer(rabbitTemplate,BiMqConstant.ERROR_DIRECT_EXCHANGE,BiMqConstant.ERROR_ROUTING_KEY);
    }
}
