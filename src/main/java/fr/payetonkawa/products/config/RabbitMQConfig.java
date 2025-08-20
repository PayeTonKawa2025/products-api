package fr.payetonkawa.products.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static fr.payetonkawa.common.exchange.ExchangeQueues.EXCHANGE_NAME;
import static fr.payetonkawa.common.exchange.ExchangeQueues.PRODUCT_QUEUE_NAME;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue productQueue() {
        return new Queue(PRODUCT_QUEUE_NAME);
    }

    @Bean
    public Binding productBinding(Queue productQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(productQueue)
                .to(exchange)
                .with("#");
    }

}
