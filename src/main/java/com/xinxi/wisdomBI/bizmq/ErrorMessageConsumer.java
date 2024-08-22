package com.xinxi.wisdomBI.bizmq;

import com.rabbitmq.client.Channel;
import com.xinxi.wisdomBI.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 错误消息消费者
 * &#064;Author  蒲月与理想
 * @author 蒲月理想
 */
@Component
@Slf4j
public class ErrorMessageConsumer {
    // 实现错误消息的处理逻辑
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = BiMqConstant.ERROR_QUEUE_NAME, durable = "true"),
//            exchange = @Exchange(value = BiMqConstant.ERROR_DIRECT_EXCHANGE),// 默认direct和 持久化durable
//            key = BiMqConstant.ERROR_ROUTING_KEY
//    ))
    @SneakyThrows // 该注解的作用是？ -- 自动抛出异常，即当方法内部抛出异常时，会自动封装成 RuntimeException 并抛出
    @RabbitListener(queues = BiMqConstant.ERROR_QUEUE_NAME )
    public void process(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        // 处理错误消息的逻辑对错误的消息进行日志记录
        log.error("ErrorMessageConsumer接收到消息:{}",message);
        channel.basicAck(deliveryTag, false);
    }
}
