package com.couriersync.dispatch.producer;

import com.couriersync.common.events.DeliveryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.delivery-events}")
    private String deliveryEventsTopic;

    public void publishDeliveryCreatedEvent(UUID deliveryId, String orderNumber, UUID customerId) {
        log.info("Publishing delivery created event for delivery: {}", deliveryId);

        DeliveryEvent event = new DeliveryEvent("dispatch-service");
        event.setDeliveryId(deliveryId);
        event.setOrderNumber(orderNumber);
        event.setCustomerId(customerId);
        event.setStatus(DeliveryEvent.DeliveryStatus.CREATED);

        kafkaTemplate.send(deliveryEventsTopic, deliveryId.toString(), event);
    }

    public void publishDeliveryStatusUpdatedEvent(UUID deliveryId, String orderNumber, UUID customerId, 
                                          UUID driverId, DeliveryEvent.DeliveryStatus status, 
                                          String reason) {
        log.info("Publishing delivery status updated event for delivery: {}, status: {}", deliveryId, status);

        DeliveryEvent event = new DeliveryEvent("dispatch-service");
        event.setDeliveryId(deliveryId);
        event.setOrderNumber(orderNumber);
        event.setCustomerId(customerId);
        event.setDriverId(driverId);
        event.setStatus(status);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setStatusChangeReason(reason);

        kafkaTemplate.send(deliveryEventsTopic, deliveryId.toString(), event);
    }

    public void publishDeliveryAssignedEvent(UUID deliveryId, String orderNumber, UUID customerId, UUID driverId) {
        log.info("Publishing delivery assigned event for delivery: {}, driver: {}", deliveryId, driverId);

        DeliveryEvent event = new DeliveryEvent("dispatch-service");
        event.setDeliveryId(deliveryId);
        event.setOrderNumber(orderNumber);
        event.setCustomerId(customerId);
        event.setDriverId(driverId);
        event.setStatus(DeliveryEvent.DeliveryStatus.ASSIGNED);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setStatusChangeReason("Delivery assigned to driver");

        kafkaTemplate.send(deliveryEventsTopic, deliveryId.toString(), event);
    }

    public void publishDeliveryPickedUpEvent(UUID deliveryId, String orderNumber, UUID customerId, UUID driverId) {
        log.info("Publishing delivery picked up event for delivery: {}", deliveryId);

        DeliveryEvent event = new DeliveryEvent("dispatch-service");
        event.setDeliveryId(deliveryId);
        event.setOrderNumber(orderNumber);
        event.setCustomerId(customerId);
        event.setDriverId(driverId);
        event.setStatus(DeliveryEvent.DeliveryStatus.PICKED_UP);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setStatusChangeReason("Package picked up from sender");
        event.setActualPickupTime(java.time.LocalDateTime.now());

        kafkaTemplate.send(deliveryEventsTopic, deliveryId.toString(), event);
    }

    public void publishDeliveryInTransitEvent(UUID deliveryId, String orderNumber, UUID customerId, UUID driverId) {
        log.info("Publishing delivery in transit event for delivery: {}", deliveryId);

        DeliveryEvent event = new DeliveryEvent("dispatch-service");
        event.setDeliveryId(deliveryId);
        event.setOrderNumber(orderNumber);
        event.setCustomerId(customerId);
        event.setDriverId(driverId);
        event.setStatus(DeliveryEvent.DeliveryStatus.IN_TRANSIT);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setStatusChangeReason("Package in transit to recipient");

        kafkaTemplate.send(deliveryEventsTopic, deliveryId.toString(), event);
    }

    public void publishDeliveryDeliveredEvent(UUID deliveryId, String orderNumber, UUID customerId, UUID driverId) {
        log.info("Publishing delivery delivered event for delivery: {}", deliveryId);

        DeliveryEvent event = new DeliveryEvent("dispatch-service");
        event.setDeliveryId(deliveryId);
        event.setOrderNumber(orderNumber);
        event.setCustomerId(customerId);
        event.setDriverId(driverId);
        event.setStatus(DeliveryEvent.DeliveryStatus.DELIVERED);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setStatusChangeReason("Package delivered to recipient");
        event.setActualDeliveryTime(java.time.LocalDateTime.now());

        kafkaTemplate.send(deliveryEventsTopic, deliveryId.toString(), event);
    }

    public void publishDeliveryCancelledEvent(UUID deliveryId, String orderNumber, UUID customerId, UUID driverId, String reason) {
        log.info("Publishing delivery cancelled event for delivery: {}, reason: {}", deliveryId, reason);

        DeliveryEvent event = new DeliveryEvent("dispatch-service");
        event.setDeliveryId(deliveryId);
        event.setOrderNumber(orderNumber);
        event.setCustomerId(customerId);
        event.setDriverId(driverId);
        event.setStatus(DeliveryEvent.DeliveryStatus.CANCELLED);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setStatusChangeReason(reason);
        event.setNotes(reason);

        kafkaTemplate.send(deliveryEventsTopic, deliveryId.toString(), event);
    }
}
