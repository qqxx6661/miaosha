package cn.monitor4all.miaoshaweb.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitMq配置
 * 为方便初学者启动项目，暂时注释掉@Configuration，需要使用请去除@Configuration
 */
//@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue delCacheQueue() {
        return new Queue("delCache");
    }

    @Bean
    public Queue orderQueue() {
        return new Queue("orderQueue");
    }

}
