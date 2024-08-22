package com.xinxi.wisdomBI.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MyMessageProducerTest {
    @Resource
    BiMessageProducer biMessageProducer;
    @Test
    void testDlx(){
        biMessageProducer.sendMessage("hello1");
        // 暂停一下
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}