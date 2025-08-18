package fr.payetonkawa.products.event;

import com.google.gson.Gson;
import fr.payetonkawa.products.config.RabbitMQConfig;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import static fr.payetonkawa.common.exchange.ExchangeQueues.EXCHANGE_NAME;

@Service
public class EventPublisher {

    private final AmqpTemplate amqpTemplate;
    private final static Gson gson = new Gson();

    public static final String ROUTING_KEY_PRODUCT_PRICE_UPDATED = "product.price.updated";

    public EventPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendEvent(String routingKey, Object payload) {
        amqpTemplate.convertAndSend(EXCHANGE_NAME, routingKey, gson.toJson(payload));
    }
}
