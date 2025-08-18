package fr.payetonkawa.products.event;

import com.google.gson.Gson;
import fr.payetonkawa.common.exchange.ExchangeMessage;
import fr.payetonkawa.common.exchange.ExchangeQueues;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static fr.payetonkawa.common.exchange.ExchangeQueues.PRODUCT_QUEUE_NAME;

@Component
@Log4j2
public class EventListener {

    private static final Gson gson = new Gson();

    @RabbitListener(queues = ExchangeQueues.PRODUCT_QUEUE_NAME)
    public void handleEvent(String message, Message amqpMessage) {
        String routingKey = amqpMessage.getMessageProperties().getReceivedRoutingKey();
        ExchangeMessage exchangeMessage = gson.fromJson(message, ExchangeMessage.class);
        log.info("Received event with routing key: {} from queue: {}", routingKey, PRODUCT_QUEUE_NAME);
    }

}
