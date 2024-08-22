package com.xinxi.wisdomBI.bizmq;

import com.rabbitmq.client.Channel;
import com.xinxi.wisdomBI.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author 蒲月理想
 */ // Component注解的作用是：将当前类标记为 Spring 组件，将其纳入 Spring 容器的管理范围。
@Component
@Slf4j
public class DlxMessageConsumer {
    @Resource
    ChartService chartService;
   // 监听死信队列的消息
    @SneakyThrows // 该注解的作用是？ -- 自动抛出异常，即当方法内部抛出异常时，会自动封装成 RuntimeException 并抛出
    @RabbitListener(queues = BiMqConstant.DEAD_QUEUE_NAME)
    public void processMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.error("收到死信消息: {}", message);
        if(StringUtils.isAnyBlank(message)){
            // 返回拒绝
            channel.basicReject(deliveryTag, false);
            return;
        }
        // 获取图表ID，更新为失败
        // 检查消息是否为long数字
        if(!NumberUtils.isCreatable(message)){
            channel.basicReject(deliveryTag,false);
           return ;
        }
        long id = Long.parseLong(message);
        // 判断ID
        if(id <= 0){
            channel.basicReject(deliveryTag,false);
            return;
        }
        // 更新图表
        chartService.handleChartUpdateError(id,"消息队列处理图表信息失败");
        // 确认消息
        channel.basicAck(deliveryTag, false);
    }
}
