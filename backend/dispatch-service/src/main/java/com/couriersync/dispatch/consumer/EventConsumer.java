package com.couriersync.dispatch.consumer;

import com.couriersync.common.events.DeliveryEvent;
import com.couriersync.common.events.LocationUpdateEvent;
import com.couriersync.dispatch.service.DeliveryOrderService;
import com.couriersync.dispatch.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final DeliveryOrderService deliveryOrderService;
    private final DriverService driverService;

    @KafkaListener(topics = "${app.kafka.topics.delivery-events}", groupId = "${spring.application.name}")
    public void handleDeliveryEvent(@Payload DeliveryEvent event) {
        log.info("Received delivery event: {} for delivery: {}", event.getEventType(), event.getDeliveryId());

        try {
            switch (event.getEventType()) {
                case "delivery.created":
                    // Event from user service when a new delivery request is created
                    log.info("Processing new delivery request: {}", event.getDeliveryId());
                    // This would be handled by the service directly via REST API
                    break;

                case "delivery.status.updated":
                    // Update the delivery status in our database
                    if (event.getDeliveryId() != null && event.getStatus() != null) {
                        deliveryOrderService.updateDeliveryStatus(
                                event.getDeliveryId(), 
                                null, // We don't know the previous status
                                DeliveryOrder.Status.valueOf(event.getStatus().name()),
                                event.getDeliveryId(), // Using deliveryId as changedBy for system updates
                                event.getStatusChangeReason()
                        );
                    }
                    break;

                case "delivery.assigned":
                    // Update driver assignment
                    if (event.getDeliveryId() != null && event.getDriverId() != null) {
                        deliveryOrderService.assignDriver(
                                event.getDeliveryId(), 
                                event.getDriverId(), 
                                event.getDeliveryId() // Using deliveryId as assignedBy for system updates
                        );
                    }
                    break;

                default:
                    log.warn("Unknown delivery event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing delivery event: {}", event, e);
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.location-events}", groupId = "${spring.application.name}")
    public void handleLocationUpdateEvent(@Payload LocationUpdateEvent event) {
        log.debug("Received location update event for driver: {}", event.getDriverId());

        try {
            // Update driver location
            if (event.getDriverId() != null && event.getLatitude() != null && event.getLongitude() != null) {
                driverService.updateDriverLocation(
                        event.getDriverId(), 
                        event.getLatitude(), 
                        event.getLongitude()
                );
            }
        } catch (Exception e) {
            log.error("Error processing location update event: {}", event, e);
        }
    }
}
