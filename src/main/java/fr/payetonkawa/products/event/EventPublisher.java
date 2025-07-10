package fr.payetonkawa.products.event;

import fr.payetonkawa.products.config.RabbitMQConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final AmqpTemplate amqpTemplate;

    public EventPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendEvent(String routingKey, Object payload) {
        amqpTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, payload);
    }
}
