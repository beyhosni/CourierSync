package com.couriersync.tracking.controller;

import com.couriersync.tracking.model.LocationUpdate;
import com.couriersync.tracking.service.LocationUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TrackingWebSocketController {

    private final LocationUpdateService locationUpdateService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Endpoint for receiving location updates from drivers
     */
    @MessageMapping("/location/update")
    public void receiveLocationUpdate(LocationUpdate locationUpdate) {
        log.debug("Received location update from driver: {}", locationUpdate.getDriverId());

        // Save the location update
        LocationUpdate savedUpdate = locationUpdateService.createLocationUpdate(
                com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toDto(locationUpdate));

        // Broadcast the location update to subscribers of this driver's location
        messagingTemplate.convertAndSend(
                "/topic/driver/" + locationUpdate.getDriverId(), 
                savedUpdate);

        // If this location update is associated with a delivery, also broadcast to delivery subscribers
        if (locationUpdate.getDeliveryId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/delivery/" + locationUpdate.getDeliveryId(), 
                    savedUpdate);
        }
    }

    /**
     * Endpoint for subscribing to a driver's location updates
     */
    @MessageMapping("/driver/{driverId}/subscribe")
    @SendTo("/topic/driver/{driverId}")
    public LocationUpdate subscribeToDriverLocation(@DestinationVariable UUID driverId) {
        log.debug("Client subscribed to driver location updates: {}", driverId);

        // Return the latest location update for this driver
        return locationUpdateService.getLatestLocationUpdateByDriverId(driverId)
                .map(dto -> com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toEntity(dto))
                .orElse(null);
    }

    /**
     * Endpoint for subscribing to a delivery's location updates
     */
    @MessageMapping("/delivery/{deliveryId}/subscribe")
    @SendTo("/topic/delivery/{deliveryId}")
    public LocationUpdate subscribeToDeliveryLocation(@DestinationVariable UUID deliveryId) {
        log.debug("Client subscribed to delivery location updates: {}", deliveryId);

        // Return the latest location update for this delivery
        return locationUpdateService.getLatestLocationUpdateByDeliveryId(deliveryId)
                .map(dto -> com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toEntity(dto))
                .orElse(null);
    }
}
