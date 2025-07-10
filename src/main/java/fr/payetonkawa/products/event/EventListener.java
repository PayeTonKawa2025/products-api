package fr.payetonkawa.products.event;

import fr.payetonkawa.products.config.RabbitMQConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class EventListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleEvent(String message, Message amqpMessage) {
        String routingKey = amqpMessage.getMessageProperties().getReceivedRoutingKey();
        log.info("Received event: {} from {}", message, routingKey);
    }

}
