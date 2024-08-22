package com.xinxi.wisdomBI.bizmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 蒲月理想
 */
@Configuration
public class DirectConfig {
    // 普通的队列和交换机：
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(BiMqConstant.BI_EXCHANGE_NAME);
    }

    @Bean
    public Queue directQueue() {
        // 指定过期时间,和死信交换机和key
         return QueueBuilder.durable(BiMqConstant.BI_QUEUE_NAME)
                 .withArgument("x-message-ttl", 600000)
                 .withArgument("x-dead-letter-exchange", BiMqConstant.DEAD_EXCHANGE)
                 .withArgument("x-dead-letter-routing-key", BiMqConstant.DEAD_ROUTING_KEY)
                 .build();
//       return QueueBuilder.durable(BiMqConstant.BI_QUEUE_NAME).withArgument("x-message-ttl", 600000).build();
//        return QueueBuilder.durable(BiMqConstant.B_IQUEUE_NAME).build();
    }

    @Bean
    Binding bindingDirectExchange(Queue directQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(directQueue).to(directExchange).with(BiMqConstant.BI_ROUTING_KEY);
    }

    // 死信队列
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(BiMqConstant.DEAD_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(BiMqConstant.DEAD_QUEUE_NAME).build();
    }

    @Bean
    public Binding deadLetterBinding(DirectExchange deadLetterExchange, Queue deadLetterQueue) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(BiMqConstant.DEAD_ROUTING_KEY);
    }
    // 处理重试失败记录日志的交换机和队列
    @Bean
    public DirectExchange errorExchange() {
        return new DirectExchange(BiMqConstant.ERROR_DIRECT_EXCHANGE);
    }

    @Bean
    public Queue errorQueue() {
        return QueueBuilder.durable(BiMqConstant.ERROR_QUEUE_NAME).build();
    }

    @Bean
    public Binding errorBinding(DirectExchange errorExchange, Queue errorQueue) {
        return BindingBuilder.bind(errorQueue).to(errorExchange).with(BiMqConstant.ERROR_ROUTING_KEY);
    }

}
