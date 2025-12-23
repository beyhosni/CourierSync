package com.couriersync.tracking.producer;

import com.couriersync.common.events.LocationUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.location-events}")
    private String locationEventsTopic;

    public void publishLocationUpdateEvent(UUID driverId, UUID deliveryId, BigDecimal latitude, BigDecimal longitude,
                                      BigDecimal accuracy, BigDecimal speed, BigDecimal heading, Integer batteryLevel, String deviceId) {
        log.debug("Publishing location update event for driver: {}, lat: {}, lng: {}", driverId, latitude, longitude);

        LocationUpdateEvent event = new LocationUpdateEvent("tracking-service");
        event.setDriverId(driverId);
        event.setDeliveryId(deliveryId);
        event.setLatitude(latitude);
        event.setLongitude(longitude);
        event.setAccuracy(accuracy);
        event.setSpeed(speed);
        event.setHeading(heading);
        event.setTimestamp(LocalDateTime.now());
        event.setBatteryLevel(batteryLevel);
        event.setDeviceId(deviceId);

        kafkaTemplate.send(locationEventsTopic, driverId.toString(), event);
    }

    public void publishLocationUpdateEventBatch(java.util.List<LocationUpdateEvent> events) {
        log.info("Publishing batch of {} location update events", events.size());

        for (LocationUpdateEvent event : events) {
            kafkaTemplate.send(locationEventsTopic, event.getDriverId().toString(), event);
        }
    }
}
