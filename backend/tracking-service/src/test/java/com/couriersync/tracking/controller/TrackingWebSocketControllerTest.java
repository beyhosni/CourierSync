package com.couriersync.tracking.controller;

import com.couriersync.tracking.model.LocationUpdate;
import com.couriersync.tracking.service.LocationUpdateService;
import com.couriersync.tracking.mapper.LocationUpdateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingWebSocketControllerTest {

    @Mock
    private LocationUpdateService locationUpdateService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TrackingWebSocketController controller;

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
    void testReceiveLocationUpdate() {
        // Given
        when(locationUpdateService.createLocationUpdate(any()))
                .thenReturn(LocationUpdateMapper.INSTANCE.toDto(locationUpdate));

        // When
        controller.receiveLocationUpdate(locationUpdate);

        // Then
        verify(locationUpdateService).createLocationUpdate(any());
        verify(messagingTemplate).convertAndSend(eq("/topic/driver/" + driverId), eq(locationUpdate));
        verify(messagingTemplate).convertAndSend(eq("/topic/delivery/" + deliveryId), eq(locationUpdate));
    }

    @Test
    void testReceiveLocationUpdateWithoutDeliveryId() {
        // Given
        locationUpdate.setDeliveryId(null);
        when(locationUpdateService.createLocationUpdate(any()))
                .thenReturn(LocationUpdateMapper.INSTANCE.toDto(locationUpdate));

        // When
        controller.receiveLocationUpdate(locationUpdate);

        // Then
        verify(locationUpdateService).createLocationUpdate(any());
        verify(messagingTemplate).convertAndSend(eq("/topic/driver/" + driverId), eq(locationUpdate));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/delivery/" + deliveryId), eq(locationUpdate));
    }

    @Test
    void testSubscribeToDriverLocation() {
        // Given
        when(locationUpdateService.getLatestLocationUpdateByDriverId(driverId))
                .thenReturn(Optional.of(LocationUpdateMapper.INSTANCE.toDto(locationUpdate)));

        // When
        LocationUpdate result = controller.subscribeToDriverLocation(driverId);

        // Then
        assertNotNull(result);
        assertEquals(driverId, result.getDriverId());
        verify(locationUpdateService).getLatestLocationUpdateByDriverId(driverId);
    }

    @Test
    void testSubscribeToDriverLocationNotFound() {
        // Given
        when(locationUpdateService.getLatestLocationUpdateByDriverId(driverId))
                .thenReturn(Optional.empty());

        // When
        LocationUpdate result = controller.subscribeToDriverLocation(driverId);

        // Then
        assertNull(result);
        verify(locationUpdateService).getLatestLocationUpdateByDriverId(driverId);
    }

    @Test
    void testSubscribeToDeliveryLocation() {
        // Given
        when(locationUpdateService.getLatestLocationUpdateByDeliveryId(deliveryId))
                .thenReturn(Optional.of(LocationUpdateMapper.INSTANCE.toDto(locationUpdate)));

        // When
        LocationUpdate result = controller.subscribeToDeliveryLocation(deliveryId);

        // Then
        assertNotNull(result);
        assertEquals(deliveryId, result.getDeliveryId());
        verify(locationUpdateService).getLatestLocationUpdateByDeliveryId(deliveryId);
    }

    @Test
    void testSubscribeToDeliveryLocationNotFound() {
        // Given
        when(locationUpdateService.getLatestLocationUpdateByDeliveryId(deliveryId))
                .thenReturn(Optional.empty());

        // When
        LocationUpdate result = controller.subscribeToDeliveryLocation(deliveryId);

        // Then
        assertNull(result);
        verify(locationUpdateService).getLatestLocationUpdateByDeliveryId(deliveryId);
    }
}
