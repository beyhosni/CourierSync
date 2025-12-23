package com.couriersync.tracking.consumer;

import com.couriersync.common.events.DeliveryEvent;
import com.couriersync.tracking.service.DeliveryRouteService;
import com.couriersync.tracking.service.LocationUpdateService;
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

    private final DeliveryRouteService deliveryRouteService;
    private final LocationUpdateService locationUpdateService;

    @KafkaListener(topics = "${app.kafka.topics.delivery-events}", groupId = "${spring.application.name}")
    public void handleDeliveryEvent(@Payload DeliveryEvent event) {
        log.info("Received delivery event: {} for delivery: {}", event.getEventType(), event.getDeliveryId());

        try {
            switch (event.getEventType()) {
                case "delivery.assigned":
                    // Create a new route when a delivery is assigned to a driver
                    if (event.getDeliveryId() != null && event.getDriverId() != null) {
                        DeliveryRoute.Location pickupLocation = DeliveryRoute.Location.builder()
                                .name(event.getOrderNumber())
                                .address("Pickup Location")
                                .latitude(event.getPickupLatitude())
                                .longitude(event.getPickupLongitude())
                                .build();

                        DeliveryRoute.Location dropoffLocation = DeliveryRoute.Location.builder()
                                .name(event.getOrderNumber())
                                .address("Dropoff Location")
                                .latitude(event.getDropoffLatitude())
                                .longitude(event.getDropoffLongitude())
                                .build();

                        // Estimate distance (simplified - in a real system, use a routing service)
                        BigDecimal estimatedDistance = calculateDistance(
                                event.getPickupLatitude(), event.getPickupLongitude(),
                                event.getDropoffLatitude(), event.getDropoffLongitude());

                        // Estimate duration (simplified - 30 km/h average speed)
                        Integer estimatedDuration = estimatedDistance.multiply(new BigDecimal("60"))
                                .divide(new BigDecimal("30"), 0, BigDecimal.ROUND_UP)
                                .intValue();

                        deliveryRouteService.createRoute(
                                event.getDeliveryId(), 
                                event.getDriverId(), 
                                pickupLocation, 
                                dropoffLocation,
                                estimatedDistance,
                                estimatedDuration
                        );
                    }
                    break;

                case "delivery.status.updated":
                    // Update route status based on delivery status
                    if (event.getDeliveryId() != null && event.getStatus() != null) {
                        switch (event.getStatus()) {
                            case PICKED_UP:
                                deliveryRouteService.updateRouteStatus(
                                        findRouteIdByDeliveryId(event.getDeliveryId()),
                                        DeliveryRoute.RouteStatus.IN_PROGRESS
                                );
                                break;

                            case DELIVERED:
                                deliveryRouteService.finalizeRoute(
                                        findRouteIdByDeliveryId(event.getDeliveryId()),
                                        null, // Actual distance would be calculated from route points
                                        null  // Actual duration would be calculated from timestamps
                                );
                                break;

                            case CANCELLED:
                                deliveryRouteService.updateRouteStatus(
                                        findRouteIdByDeliveryId(event.getDeliveryId()),
                                        DeliveryRoute.RouteStatus.CANCELLED
                                );
                                break;
                        }
                    }
                    break;

                default:
                    log.debug("Ignoring delivery event: {} for tracking service", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing delivery event: {}", event, e);
        }
    }

    private String findRouteIdByDeliveryId(java.util.UUID deliveryId) {
        // In a real implementation, this would query the database
        // For now, return a placeholder
        return "route-" + deliveryId.toString().substring(0, 8);
    }

    private BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        // Simplified distance calculation using Haversine formula
        // In a real system, use a proper mapping service

        double earthRadius = 6371; // km

        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue())) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return new BigDecimal(earthRadius * c).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
