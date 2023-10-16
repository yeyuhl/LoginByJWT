package io.github.yeyuhl.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

/**
 * RabbitMQ消息队列配置
 *
 * @author yeyuhl
 * @since 2023/10/15
 */
@Configuration
public class RabbitConfiguration {
    @Bean("mailQueue")
    public Queue queue() {
        return QueueBuilder
                .durable("mail")
                .build();
    }
}