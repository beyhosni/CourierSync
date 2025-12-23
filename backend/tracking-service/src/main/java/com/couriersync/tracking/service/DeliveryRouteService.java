package com.couriersync.tracking.service;

import com.couriersync.tracking.model.DeliveryRoute;
import com.couriersync.tracking.model.LocationUpdate;
import com.couriersync.tracking.repository.DeliveryRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryRouteService {

    private final DeliveryRouteRepository deliveryRouteRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ROUTE_UPDATES_TOPIC = "tracking.routes";

    public DeliveryRoute createRoute(UUID deliveryId, UUID driverId, 
                                   DeliveryRoute.Location pickupLocation, 
                                   DeliveryRoute.Location dropoffLocation,
                                   BigDecimal estimatedDistance, 
                                   Integer estimatedDuration) {
        log.info("Creating new route for delivery: {}, driver: {}", deliveryId, driverId);

        DeliveryRoute route = DeliveryRoute.builder()
                .deliveryId(deliveryId)
                .driverId(driverId)
                .pickupLocation(pickupLocation)
                .dropoffLocation(dropoffLocation)
                .estimatedDistance(estimatedDistance)
                .estimatedDuration(estimatedDuration)
                .status(DeliveryRoute.RouteStatus.PLANNED)
                .build();

        DeliveryRoute savedRoute = deliveryRouteRepository.save(route);

        // Send to Kafka
        kafkaTemplate.send(ROUTE_UPDATES_TOPIC, savedRoute);

        log.info("Created route with ID: {} for delivery: {}", savedRoute.getId(), deliveryId);
        return savedRoute;
    }

    public Optional<DeliveryRoute> getRouteById(String id) {
        return deliveryRouteRepository.findById(id);
    }

    public List<DeliveryRoute> getRoutesByDeliveryId(UUID deliveryId) {
        return deliveryRouteRepository.findByDeliveryId(deliveryId);
    }

    public List<DeliveryRoute> getRoutesByDriverId(UUID driverId) {
        return deliveryRouteRepository.findByDriverId(driverId);
    }

    public List<DeliveryRoute> getRoutesByStatus(DeliveryRoute.RouteStatus status) {
        return deliveryRouteRepository.findByStatus(status);
    }

    public DeliveryRoute updateRouteStatus(String routeId, DeliveryRoute.RouteStatus status) {
        log.info("Updating route {} status to {}", routeId, status);

        return deliveryRouteRepository.findById(routeId)
                .map(route -> {
                    route.setStatus(status);

                    // Update timestamps based on status
                    if (status == DeliveryRoute.RouteStatus.IN_PROGRESS && route.getStartedAt() == null) {
                        route.setStartedAt(LocalDateTime.now());
                    } else if (status == DeliveryRoute.RouteStatus.COMPLETED) {
                        route.setCompletedAt(LocalDateTime.now());
                    }

                    DeliveryRoute updatedRoute = deliveryRouteRepository.save(route);

                    // Send to Kafka
                    kafkaTemplate.send(ROUTE_UPDATES_TOPIC, updatedRoute);

                    return updatedRoute;
                })
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + routeId));
    }

    public DeliveryRoute addLocationPointToRoute(UUID deliveryId, LocationUpdate locationUpdate) {
        log.debug("Adding location point to route for delivery: {}", deliveryId);

        // Find the most recent active route for this delivery
        DeliveryRoute route = deliveryRouteRepository.findLatestByDriverIdAndStatus(
                locationUpdate.getDriverId(), DeliveryRoute.RouteStatus.IN_PROGRESS);

        if (route == null) {
            // Try to find a planned route
            route = deliveryRouteRepository.findLatestByDriverIdAndStatus(
                    locationUpdate.getDriverId(), DeliveryRoute.RouteStatus.PLANNED);

            if (route == null) {
                log.debug("No active or planned route found for driver: {}", locationUpdate.getDriverId());
                return null;
            }

            // Update status to in progress
            route.setStatus(DeliveryRoute.RouteStatus.IN_PROGRESS);
            route.setStartedAt(LocalDateTime.now());
        }

        // Create a new location point
        DeliveryRoute.LocationPoint locationPoint = DeliveryRoute.LocationPoint.builder()
                .latitude(locationUpdate.getLatitude())
                .longitude(locationUpdate.getLongitude())
                .timestamp(locationUpdate.getTimestamp())
                .speed(locationUpdate.getSpeed())
                .heading(locationUpdate.getHeading())
                .build();

        // Add to route points
        route.getRoutePoints().add(locationPoint);

        // Save the updated route
        DeliveryRoute updatedRoute = deliveryRouteRepository.save(route);

        // Send to Kafka
        kafkaTemplate.send(ROUTE_UPDATES_TOPIC, updatedRoute);

        return updatedRoute;
    }

    public List<DeliveryRoute.LocationPoint> getRoutePointsByDeliveryId(UUID deliveryId) {
        Optional<DeliveryRoute> route = deliveryRouteRepository.findRoutePointsByDeliveryId(deliveryId);
        return route.map(DeliveryRoute::getRoutePoints).orElse(null);
    }

    public DeliveryRoute getRoutePointsAndStatusByDeliveryId(UUID deliveryId) {
        return deliveryRouteRepository.findRoutePointsAndStatusByDeliveryId(deliveryId);
    }

    public List<DeliveryRoute.LocationPoint> getLast100RoutePointsByDeliveryId(UUID deliveryId) {
        Optional<DeliveryRoute> route = deliveryRouteRepository.findLast100RoutePointsByDeliveryId(deliveryId);
        return route.map(DeliveryRoute::getRoutePoints).orElse(null);
    }

    public DeliveryRoute finalizeRoute(String routeId, BigDecimal actualDistance, Integer actualDuration) {
        log.info("Finalizing route {} with actual distance: {} km and duration: {} minutes", 
                routeId, actualDistance, actualDuration);

        return deliveryRouteRepository.findById(routeId)
                .map(route -> {
                    route.setActualDistance(actualDistance);
                    route.setActualDuration(actualDuration);
                    route.setStatus(DeliveryRoute.RouteStatus.COMPLETED);
                    route.setCompletedAt(LocalDateTime.now());

                    DeliveryRoute updatedRoute = deliveryRouteRepository.save(route);

                    // Send to Kafka
                    kafkaTemplate.send(ROUTE_UPDATES_TOPIC, updatedRoute);

                    return updatedRoute;
                })
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + routeId));
    }
}
