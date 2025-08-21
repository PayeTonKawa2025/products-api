package fr.payetonkawa.products.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.payetonkawa.products.messaging.ExchangeMessage;
import fr.payetonkawa.products.messaging.ExchangeQueues;
import fr.payetonkawa.products.repository.ProductRepository;
import fr.payetonkawa.products.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EventListenerTest {

    @Mock
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private EventListener eventListener;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventListener = new EventListener(productService, productRepository, eventPublisher);
    }

    private Message mockAmqpMessage(String routingKey) {
        MessageProperties props = new MessageProperties();
        props.setReceivedRoutingKey(routingKey);
        return new Message("{}".getBytes(StandardCharsets.UTF_8), props);
    }

    @Test
    void testHandleOrderCreated_stockOk() throws Exception {
        String routingKey = "order.created";
        List<Map<String, Object>> items = List.of(Map.of("itemId", 1L, "quantity", 2));
        Map<String, Object> payload = Map.of("orderId", 42L, "items", items);
        String message = objectMapper.writeValueAsString(Map.of("payload", payload));
        Message amqpMessage = mockAmqpMessage(routingKey);

        when(productService.verifyAndUpdateStock(any())).thenReturn(true);

        eventListener.handleEvent(message, amqpMessage);

        verify(productService).verifyAndUpdateStock(any());
        verify(eventPublisher).sendEvent(eq("product.stock.confirmed"), argThat(msg ->
                ((Map<?, ?>) msg.getPayload()).get("orderId").equals(42L)
        ));
    }

    @Test
    void testHandleOrderCreated_stockInsufficient() throws Exception {
        String routingKey = "order.created";
        List<Map<String, Object>> items = List.of(Map.of("itemId", 1L, "quantity", 2));
        Map<String, Object> payload = Map.of("orderId", 43L, "items", items);
        String message = objectMapper.writeValueAsString(Map.of("payload", payload));
        Message amqpMessage = mockAmqpMessage(routingKey);

        when(productService.verifyAndUpdateStock(any())).thenReturn(false);

        eventListener.handleEvent(message, amqpMessage);

        verify(productService).verifyAndUpdateStock(any());
        verify(eventPublisher).sendEvent(eq("product.stock.insufficient"), argThat(msg ->
                ((Map<?, ?>) msg.getPayload()).get("orderId").equals(43L)
        ));
    }

    @Test
    void testHandleOrderDeleted() throws Exception {
        String routingKey = "order.deleted";
        List<Map<String, Object>> items = List.of(
                Map.of("itemId", 1L, "quantity", 2),
                Map.of("itemId", 2L, "quantity", 3)
        );
        Map<String, Object> payload = Map.of("orderId", 44L, "items", items);
        String message = objectMapper.writeValueAsString(Map.of("payload", payload));
        Message amqpMessage = mockAmqpMessage(routingKey);

        eventListener.handleEvent(message, amqpMessage);

        verify(productService).restoreStock(1L, 2);
        verify(productService).restoreStock(2L, 3);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void testHandleOrderCancelled() throws Exception {
        String routingKey = "order.cancelled";
        List<Map<String, Object>> items = List.of(
                Map.of("itemId", 5L, "quantity", 1)
        );
        Map<String, Object> payload = Map.of("orderId", 45L, "items", items);
        String message = objectMapper.writeValueAsString(Map.of("payload", payload));
        Message amqpMessage = mockAmqpMessage(routingKey);

        eventListener.handleEvent(message, amqpMessage);

        verify(productService).restoreStock(5L, 1);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void testHandleOrderUpdated_stockConfirmed() throws Exception {
        String routingKey = "order.updated";
        List<Map<String, Object>> previousItems = List.of(
                Map.of("itemId", 1L, "quantity", 2)
        );
        List<Map<String, Object>> newItems = List.of(
                Map.of("itemId", 1L, "quantity", 3)
        );
        Map<String, Object> payload = Map.of(
                "orderId", 46L,
                "previousItems", previousItems,
                "items", newItems
        );
        String message = objectMapper.writeValueAsString(Map.of("payload", payload));
        Message amqpMessage = mockAmqpMessage(routingKey);

        var product = mock(fr.payetonkawa.products.entity.Product.class);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(product.getStock()).thenReturn(10);

        eventListener.handleEvent(message, amqpMessage);

        verify(productRepository, times(2)).findById(1L);
        verify(product).setStock(anyInt());
        verify(productRepository).save(product);
        verify(eventPublisher).sendEvent(eq("product.stock.confirmed"), any());
    }

    @Test
    void testHandleOrderUpdated_stockInsufficient() throws Exception {
        String routingKey = "order.updated";
        List<Map<String, Object>> previousItems = List.of(
                Map.of("itemId", 1L, "quantity", 1)
        );
        List<Map<String, Object>> newItems = List.of(
                Map.of("itemId", 1L, "quantity", 10)
        );
        Map<String, Object> payload = Map.of(
                "orderId", 47L,
                "previousItems", previousItems,
                "items", newItems
        );
        String message = objectMapper.writeValueAsString(Map.of("payload", payload));
        Message amqpMessage = mockAmqpMessage(routingKey);

        var product = mock(fr.payetonkawa.products.entity.Product.class);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(product.getStock()).thenReturn(5);

        eventListener.handleEvent(message, amqpMessage);

        verify(productRepository, times(1)).findById(1L);
        verify(eventPublisher).sendEvent(eq("product.stock.insufficient"), any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testHandleEvent_unhandledRoutingKey() throws Exception {
        String routingKey = "unknown.event";
        Map<String, Object> payload = Map.of("orderId", 99L, "items", List.of());
        String message = objectMapper.writeValueAsString(Map.of("payload", payload));
        Message amqpMessage = mockAmqpMessage(routingKey);

        eventListener.handleEvent(message, amqpMessage);

        verifyNoInteractions(productService, productRepository, eventPublisher);
    }
}