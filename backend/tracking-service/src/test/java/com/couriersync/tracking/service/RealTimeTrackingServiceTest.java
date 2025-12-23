package com.couriersync.tracking.service;

import com.couriersync.tracking.model.LocationUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealTimeTrackingServiceTest {

    @Mock
    private LocationUpdateService locationUpdateService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RealTimeTrackingService realTimeTrackingService;

    private LocationUpdate locationUpdate;
    private UUID driverId;
    private UUID deliveryId;

    @BeforeEach
    void setUp() {
        driverId = UUID.randomUUID();
        deliveryId = UUID.randomUUID();

        locationUpdate = LocationUpdate.builder()
                .id(UUID.randomUUID())
                .driverId(driverId)
                .deliveryId(deliveryId)
                .latitude(new BigDecimal("40.7128"))
                .longitude(new BigDecimal("-74.0060"))
                .accuracy(new BigDecimal("10.0"))
                .speed(new BigDecimal("30.0"))
                .heading(new BigDecimal("90.0"))
                .timestamp(LocalDateTime.now())
                .batteryLevel(85)
                .deviceId("device-123")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testBroadcastLocationUpdate() {
        // Given
        when(locationUpdateService.createLocationUpdate(any()))
                .thenReturn(com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toDto(locationUpdate));

        // When
        CompletableFuture<Void> result = realTimeTrackingService.broadcastLocationUpdate(locationUpdate);

        // Then
        assertNotNull(result);
        verify(messagingTemplate).convertAndSend(eq("/topic/driver/" + driverId), eq(locationUpdate));
        verify(messagingTemplate).convertAndSend(eq("/topic/delivery/" + deliveryId), eq(locationUpdate));
    }

    @Test
    void testBroadcastLocationUpdateWithoutDeliveryId() {
        // Given
        locationUpdate.setDeliveryId(null);

        // When
        CompletableFuture<Void> result = realTimeTrackingService.broadcastLocationUpdate(locationUpdate);

        // Then
        assertNotNull(result);
        verify(messagingTemplate).convertAndSend(eq("/topic/driver/" + driverId), eq(locationUpdate));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/delivery/" + deliveryId), eq(locationUpdate));
    }

    @Test
    void testBroadcastLocationUpdates() {
        // Given
        LocationUpdate locationUpdate2 = LocationUpdate.builder()
                .id(UUID.randomUUID())
                .driverId(UUID.randomUUID())
                .deliveryId(UUID.randomUUID())
                .latitude(new BigDecimal("40.7580"))
                .longitude(new BigDecimal("-73.9855"))
                .accuracy(new BigDecimal("10.0"))
                .speed(new BigDecimal("25.0"))
                .heading(new BigDecimal("45.0"))
                .timestamp(LocalDateTime.now())
                .batteryLevel(84)
                .deviceId("device-123")
                .createdAt(LocalDateTime.now())
                .build();

        List<LocationUpdate> locationUpdates = List.of(locationUpdate, locationUpdate2);

        // When
        CompletableFuture<Void> result = realTimeTrackingService.broadcastLocationUpdates(locationUpdates);

        // Then
        assertNotNull(result);
        verify(messagingTemplate).convertAndSend(eq("/topic/driver/" + driverId), eq(locationUpdate));
        verify(messagingTemplate).convertAndSend(eq("/topic/delivery/" + deliveryId), eq(locationUpdate));
        verify(messagingTemplate).convertAndSend(eq("/topic/driver/" + locationUpdate2.getDriverId()), eq(locationUpdate2));
        verify(messagingTemplate).convertAndSend(eq("/topic/delivery/" + locationUpdate2.getDeliveryId()), eq(locationUpdate2));
    }

    @Test
    void testSendLatestLocationUpdate() {
        // Given
        String sessionId = "session-123";
        when(locationUpdateService.getLatestLocationUpdateByDriverId(driverId))
                .thenReturn(Optional.of(com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toDto(locationUpdate)));

        // When
        CompletableFuture<Void> result = realTimeTrackingService.sendLatestLocationUpdate(driverId, sessionId);

        // Then
        assertNotNull(result);
        verify(messagingTemplate).convertAndSendToUser(
                eq(sessionId),
                eq("/queue/driver/" + driverId),
                eq(locationUpdate));
    }

    @Test
    void testSendLatestLocationUpdateNotFound() {
        // Given
        String sessionId = "session-123";
        when(locationUpdateService.getLatestLocationUpdateByDriverId(driverId))
                .thenReturn(Optional.empty());

        // When
        CompletableFuture<Void> result = realTimeTrackingService.sendLatestLocationUpdate(driverId, sessionId);

        // Then
        assertNotNull(result);
        verify(messagingTemplate, never()).convertAndSendToUser(
                anyString(),
                anyString(),
                any(LocationUpdate.class));
    }

    @Test
    void testSendLatestDeliveryLocationUpdate() {
        // Given
        String sessionId = "session-123";
        when(locationUpdateService.getLatestLocationUpdateByDeliveryId(deliveryId))
                .thenReturn(Optional.of(com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toDto(locationUpdate)));

        // When
        CompletableFuture<Void> result = realTimeTrackingService.sendLatestDeliveryLocationUpdate(deliveryId, sessionId);

        // Then
        assertNotNull(result);
        verify(messagingTemplate).convertAndSendToUser(
                eq(sessionId),
                eq("/queue/delivery/" + deliveryId),
                eq(locationUpdate));
    }

    @Test
    void testSendLatestDeliveryLocationUpdateNotFound() {
        // Given
        String sessionId = "session-123";
        when(locationUpdateService.getLatestLocationUpdateByDeliveryId(deliveryId))
                .thenReturn(Optional.empty());

        // When
        CompletableFuture<Void> result = realTimeTrackingService.sendLatestDeliveryLocationUpdate(deliveryId, sessionId);

        // Then
        assertNotNull(result);
        verify(messagingTemplate, never()).convertAndSendToUser(
                anyString(),
                anyString(),
                any(LocationUpdate.class));
    }

    @Test
    void testNotifyDriverOffline() {
        // When
        CompletableFuture<Void> result = realTimeTrackingService.notifyDriverOffline(driverId);

        // Then
        assertNotNull(result);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/driver/" + driverId),
                argThat(update -> 
                        update.getDriverId().equals(driverId) && 
                        update.getTimestamp() != null &&
                        update.getLatitude() == null &&
                        update.getLongitude() == null
                ));
    }

    @Test
    void testNotifyDriverOnline() {
        // Given
        when(locationUpdateService.getLatestLocationUpdateByDriverId(driverId))
                .thenReturn(Optional.of(com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toDto(locationUpdate)));

        // When
        CompletableFuture<Void> result = realTimeTrackingService.notifyDriverOnline(driverId);

        // Then
        assertNotNull(result);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/driver/" + driverId),
                eq(locationUpdate));
    }

    @Test
    void testNotifyDriverOnlineWithoutLatestLocation() {
        // Given
        when(locationUpdateService.getLatestLocationUpdateByDriverId(driverId))
                .thenReturn(Optional.empty());

        // When
        CompletableFuture<Void> result = realTimeTrackingService.notifyDriverOnline(driverId);

        // Then
        assertNotNull(result);
        verify(messagingTemplate, never()).convertAndSend(
                anyString(),
                any(LocationUpdate.class));
    }
}
