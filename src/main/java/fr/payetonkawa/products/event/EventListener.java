package fr.payetonkawa.products.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.payetonkawa.products.messaging.ExchangeMessage;
import fr.payetonkawa.products.messaging.ExchangeQueues;
import fr.payetonkawa.products.repository.ProductRepository;
import fr.payetonkawa.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = ExchangeQueues.PRODUCT_QUEUE_NAME)
    public void handleEvent(String message, Message amqpMessage) throws Exception {
        String routingKey = amqpMessage.getMessageProperties().getReceivedRoutingKey();
        log.info("📩 Received event with routing key: {}", routingKey);

        JsonNode root = objectMapper.readTree(message);
        JsonNode payloadNode = root.get("payload");
        Long orderId = payloadNode.get("orderId").asLong();

        switch (routingKey) {
            case "order.created" -> {
                List<Map<String, Object>> items = objectMapper.convertValue(
                        payloadNode.get("items"), new TypeReference<>() {}
                );
                handleOrderCreated(orderId, items);
            }
            case "order.deleted" -> {
                List<Map<String, Object>> items = objectMapper.convertValue(
                        payloadNode.get("items"), new TypeReference<>() {}
                );
                handleOrderDeleted(orderId, items);
            }
            case "order.cancelled" -> {
                List<Map<String, Object>> items = objectMapper.convertValue(
                        payloadNode.get("items"), new TypeReference<>() {}
                );
                handleOrderCancelled(orderId, items);
            }
            case "order.updated" -> {
                List<Map<String, Object>> oldItems = objectMapper.convertValue(
                        payloadNode.get("previousItems"), new TypeReference<>() {}
                );
                List<Map<String, Object>> newItems = objectMapper.convertValue(
                        payloadNode.get("items"), new TypeReference<>() {}
                );
                handleOrderUpdated(orderId, oldItems, newItems);
            }
            default -> log.warn("⚠️ Unhandled routing key: {}", routingKey);
        }
    }

    private void handleOrderCreated(Long orderId, List<Map<String, Object>> items) {
        log.info("🛒 Handling 'order.created' for orderId={}", orderId);

        boolean stockOk = productService.verifyAndUpdateStock(items);

        String routingKey = stockOk ? "product.stock.confirmed" : "product.stock.insufficient";

        log.info("📤 Sending event: {} for orderId={}", routingKey, orderId);

        eventPublisher.sendEvent(routingKey, ExchangeMessage.builder()
                .payload(Map.of("orderId", orderId))
                .build());
    }

    private void handleOrderDeleted(Long orderId, List<Map<String, Object>> items) {
        log.info("❌ Handling 'order.deleted' for orderId={}", orderId);

        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("itemId").toString());
            int quantity = (int) item.get("quantity");
            productService.restoreStock(productId, quantity);
        }

        log.info("✅ Stock restored for orderId={}", orderId);
    }

    private void handleOrderUpdated(Long orderId, List<Map<String, Object>> previousItems, List<Map<String, Object>> newItems) {
        log.info("🔁 Handling 'order.updated' for orderId={}", orderId);

        Map<Long, Integer> adjustments = new HashMap<>();

        for (Map<String, Object> item : previousItems) {
            Long id = Long.valueOf(item.get("itemId").toString());
            int qty = (int) item.get("quantity");
            adjustments.put(id, adjustments.getOrDefault(id, 0) - qty);
        }

        for (Map<String, Object> item : newItems) {
            Long id = Long.valueOf(item.get("itemId").toString());
            int qty = (int) item.get("quantity");
            adjustments.put(id, adjustments.getOrDefault(id, 0) + qty);
        }

        // Vérifie que tous les ajouts sont faisables
        for (Map.Entry<Long, Integer> entry : adjustments.entrySet()) {
            if (entry.getValue() > 0) {
                var product = productRepository.findById(entry.getKey()).orElse(null);
                if (product == null || product.getStock() < entry.getValue()) {
                    eventPublisher.sendEvent("product.stock.insufficient", ExchangeMessage.builder()
                            .payload(Map.of("orderId", orderId))
                            .build());
                    return;
                }
            }
        }

        // Applique les ajustements
        for (Map.Entry<Long, Integer> entry : adjustments.entrySet()) {
            var product = productRepository.findById(entry.getKey()).orElse(null);
            if (product == null) continue;
            product.setStock(product.getStock() - entry.getValue()); // négatif = +stock
            productRepository.save(product);
        }

        log.info("✅ Stock adjusted for order.updated, orderId={}", orderId);
        eventPublisher.sendEvent("product.stock.confirmed", ExchangeMessage.builder()
                .payload(Map.of("orderId", orderId))
                .build());
    }

    private void handleOrderCancelled(Long orderId, List<Map<String, Object>> items) {
        log.info("🚫 Handling 'order.cancelled' for orderId={}", orderId);
        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("itemId").toString());
            int quantity = (int) item.get("quantity");
            productService.restoreStock(productId, quantity);
        }
        log.info("✅ Stock restored for orderId={}", orderId);
    }
}

