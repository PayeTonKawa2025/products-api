package fr.payetonkawa.products.event;

import fr.payetonkawa.products.messaging.ExchangeMessage;
import fr.payetonkawa.products.messaging.ExchangeQueues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpTemplate;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EventPublisherTest {

    private AmqpTemplate amqpTemplate;
    private EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        amqpTemplate = mock(AmqpTemplate.class);
        eventPublisher = new EventPublisher(amqpTemplate);
    }

    @Test
    void testSendEvent_setsFieldsAndPublishes() {
        String routingKey = "product.stock.confirmed";
        ExchangeMessage message = new ExchangeMessage();
        message.setPayload("test-payload");

        eventPublisher.sendEvent(routingKey, message);

        assertEquals(ExchangeQueues.EXCHANGE_NAME, message.getExchangeId());
        assertEquals(routingKey, message.getRoutingKey());
        assertEquals(routingKey, message.getType());

        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(amqpTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                payloadCaptor.capture()
        );

        assertEquals(ExchangeQueues.EXCHANGE_NAME, exchangeCaptor.getValue());
        assertEquals(routingKey, routingKeyCaptor.getValue());
        assertTrue(payloadCaptor.getValue().contains("\"payload\":\"test-payload\""));
    }

    @Test
    void testSendEvent_withDifferentRoutingKey() {
        String routingKey = "product.stock.insufficient";
        ExchangeMessage message = new ExchangeMessage();
        message.setPayload("another-payload");

        eventPublisher.sendEvent(routingKey, message);

        assertEquals(ExchangeQueues.EXCHANGE_NAME, message.getExchangeId());
        assertEquals(routingKey, message.getRoutingKey());
        assertEquals(routingKey, message.getType());

        verify(amqpTemplate).convertAndSend(
                eq(ExchangeQueues.EXCHANGE_NAME),
                eq(routingKey),
                contains("\"payload\":\"another-payload\"")
        );
    }

    @Test
    void testSendEvent_nullPayload() {
        String routingKey = "product.stock.confirmed";
        ExchangeMessage message = new ExchangeMessage();
        message.setPayload(null);

        eventPublisher.sendEvent(routingKey, message);

        verify(amqpTemplate).convertAndSend(
                eq(ExchangeQueues.EXCHANGE_NAME),
                eq(routingKey),
                contains("\"payload\":null")
        );
    }
}