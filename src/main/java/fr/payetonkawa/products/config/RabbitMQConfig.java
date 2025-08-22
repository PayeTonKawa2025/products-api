package fr.payetonkawa.products.config;

import fr.payetonkawa.products.messaging.ExchangeQueues;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(ExchangeQueues.EXCHANGE_NAME);
    }

    @Bean
    public Queue productQueue() {
        return new Queue(ExchangeQueues.PRODUCT_QUEUE_NAME);
    }

    @Bean
    public Binding productBinding(Queue productQueue, TopicExchange exchange) {
        return BindingBuilder.bind(productQueue).to(exchange).with("order.*");
    }
}
