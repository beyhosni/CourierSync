package com.couriersync.tracking.service;

import com.couriersync.tracking.model.LocationUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeTrackingService {

    private final LocationUpdateService locationUpdateService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcasts a location update to all subscribers of a driver's location
     */
    @Async
    public CompletableFuture<Void> broadcastLocationUpdate(LocationUpdate locationUpdate) {
        log.debug("Broadcasting location update for driver: {}", locationUpdate.getDriverId());

        // Broadcast to driver subscribers
        messagingTemplate.convertAndSend(
                "/topic/driver/" + locationUpdate.getDriverId(), 
                locationUpdate);

        // If associated with a delivery, also broadcast to delivery subscribers
        if (locationUpdate.getDeliveryId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/delivery/" + locationUpdate.getDeliveryId(), 
                    locationUpdate);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Broadcasts a batch of location updates to all subscribers
     */
    @Async
    public CompletableFuture<Void> broadcastLocationUpdates(List<LocationUpdate> locationUpdates) {
        log.debug("Broadcasting {} location updates", locationUpdates.size());

        // Group updates by driver and delivery for efficient broadcasting
        locationUpdates.forEach(update -> {
            // Broadcast to driver subscribers
            messagingTemplate.convertAndSend(
                    "/topic/driver/" + update.getDriverId(), 
                    update);

            // If associated with a delivery, also broadcast to delivery subscribers
            if (update.getDeliveryId() != null) {
                messagingTemplate.convertAndSend(
                        "/topic/delivery/" + update.getDeliveryId(), 
                        update);
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sends the latest location update for a driver to a specific subscriber
     */
    @Async
    public CompletableFuture<Void> sendLatestLocationUpdate(UUID driverId, String sessionId) {
        log.debug("Sending latest location update for driver: {} to session: {}", driverId, sessionId);

        locationUpdateService.getLatestLocationUpdateByDriverId(driverId)
                .ifPresent(dto -> {
                    LocationUpdate locationUpdate = com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toEntity(dto);
                    messagingTemplate.convertAndSendToUser(
                            sessionId,
                            "/queue/driver/" + driverId,
                            locationUpdate);
                });

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sends the latest location update for a delivery to a specific subscriber
     */
    @Async
    public CompletableFuture<Void> sendLatestDeliveryLocationUpdate(UUID deliveryId, String sessionId) {
        log.debug("Sending latest location update for delivery: {} to session: {}", deliveryId, sessionId);

        locationUpdateService.getLatestLocationUpdateByDeliveryId(deliveryId)
                .ifPresent(dto -> {
                    LocationUpdate locationUpdate = com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toEntity(dto);
                    messagingTemplate.convertAndSendToUser(
                            sessionId,
                            "/queue/delivery/" + deliveryId,
                            locationUpdate);
                });

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Notifies subscribers when a driver goes offline
     */
    @Async
    public CompletableFuture<Void> notifyDriverOffline(UUID driverId) {
        log.debug("Notifying subscribers that driver {} is offline", driverId);

        LocationUpdate offlineUpdate = LocationUpdate.builder()
                .driverId(driverId)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        // Broadcast to driver subscribers
        messagingTemplate.convertAndSend(
                "/topic/driver/" + driverId, 
                offlineUpdate);

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Notifies subscribers when a driver comes back online
     */
    @Async
    public CompletableFuture<Void> notifyDriverOnline(UUID driverId) {
        log.debug("Notifying subscribers that driver {} is online", driverId);

        // Send the latest location update to indicate the driver is online
        locationUpdateService.getLatestLocationUpdateByDriverId(driverId)
                .ifPresent(dto -> {
                    LocationUpdate locationUpdate = com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toEntity(dto);
                    messagingTemplate.convertAndSend(
                            "/topic/driver/" + driverId, 
                            locationUpdate);
                });

        return CompletableFuture.completedFuture(null);
    }
}
