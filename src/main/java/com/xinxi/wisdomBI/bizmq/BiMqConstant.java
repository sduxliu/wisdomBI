package com.xinxi.wisdomBI.bizmq;

/**
 * 消息队列常量
 * @author 蒲月理想
 */
public interface BiMqConstant {

    String BI_EXCHANGE_NAME = "bi_exchange";

    String BI_QUEUE_NAME = "bi_queue";

    String BI_ROUTING_KEY = "bi_routingKey";
    String ERROR_DIRECT_EXCHANGE = "error_direct_exchange";
    String ERROR_QUEUE_NAME = "error_queue";
    String ERROR_ROUTING_KEY = "error_routing_key";
    String DEAD_QUEUE_NAME = "dead_queue";
    String DEAD_ROUTING_KEY = "dead_routing_key";
    String DEAD_EXCHANGE = "dead_exchange";
}
