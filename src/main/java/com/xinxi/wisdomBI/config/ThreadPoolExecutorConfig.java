package com.xinxi.wisdomBI.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池配置
 * @author 蒲月理想
 */
@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程" + count);
                count++;
                return thread;
            }
        };
        // 使用LinkedBlockingQueue，队列长度为4
        return new ThreadPoolExecutor(2, 4, 100, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(4), threadFactory);
    }
}
