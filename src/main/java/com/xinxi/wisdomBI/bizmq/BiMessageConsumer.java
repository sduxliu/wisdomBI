package com.xinxi.wisdomBI.bizmq;

import com.rabbitmq.client.Channel;
import com.xinxi.wisdomBI.common.ErrorCode;
import com.xinxi.wisdomBI.exception.BusinessException;
import com.xinxi.wisdomBI.manager.AiManager;
import com.xinxi.wisdomBI.model.entity.Chart;
import com.xinxi.wisdomBI.model.enums.ChartStatusEnum;
import com.xinxi.wisdomBI.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消费者
 * @author 蒲月理想
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;


    // 指定程序监听的消息队列和确认机制


//    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    // 绑定死信交换机和key， 设置过期时间10分钟
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = BiMqConstant.BI_QUEUE_NAME, durable = "true",
//                    arguments = {
//                            @Argument(name = "x-message-ttl", value = "600000"), // 设置消息过期时间为10分钟
//                            @Argument(name = "x-dead-letter-exchange", value = BiMqConstant.DEAD_EXCHANGE), // 死信交换机
//                            @Argument(name = "x-dead-letter-routing-key", value = BiMqConstant.DEAD_ROUTING_KEY) // 死信路由键
//                    }),
//            exchange = @Exchange(value = BiMqConstant.BI_EXCHANGE_NAME),
//            key = BiMqConstant.BI_ROUTING_KEY
//    ))
    @SneakyThrows // 该注解的作用是？ -- 自动抛出异常，即当方法内部抛出异常时，会自动封装成 RuntimeException 并抛出
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            // 如果失败，消息拒绝，不能重新入队
           channel.basicReject(deliveryTag, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        // 检查消息是否为long数字
        if(!NumberUtils.isCreatable(message)){
            //deliveryTag：标识消息。
            //multiple：是否确认所有早期消息。
            //requeue：拒绝后是否重新入队。
            channel.basicNack(deliveryTag, false,false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "消息错误");
        }
        // 获取图表
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            //deliveryTag：标识消息。
            //requeue：拒绝后是否重新入队。
            channel.basicReject(deliveryTag, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }

        // 先修改图表任务状态为 “执行中”。
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            channel.basicNack(deliveryTag, false, false);
            chartService.handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
            return;
        }

        // 构造用户输入
        // 调用 AI
       boolean updateResult = chartService.aiGenChartAndUpdateOldChartInfo(chartService.getMessage(chart.getGoal(), chart.getChartType(), chart.getChartData()), chart);
        if (!updateResult) {
            // 如果失败，返回nack触发重试
            channel.basicNack(deliveryTag, false, false);
            // 更新图表任务状态为 “失败”
            chartService.handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
        // 消息确认
        channel.basicAck(deliveryTag, false);
    }



}
