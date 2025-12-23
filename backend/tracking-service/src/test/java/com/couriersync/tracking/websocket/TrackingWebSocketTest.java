package com.couriersync.tracking.websocket;

import com.couriersync.tracking.config.WebSocketConfig;
import com.couriersync.tracking.controller.TrackingWebSocketController;
import com.couriersync.tracking.model.LocationUpdate;
import com.couriersync.tracking.service.LocationUpdateService;
import com.couriersync.tracking.service.RealTimeTrackingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TrackingWebSocketTest {

    @MockBean
    private LocationUpdateService locationUpdateService;

    @MockBean
    private RealTimeTrackingService realTimeTrackingService;

    @MockBean
    private SimpMessageSendingOperations messagingTemplate;

    private WebSocketStompClient stompClient;
    private String WEBSOCKET_URI;
    private final String WEBSOCKET_TOPIC = "/topic/driver/";

    @BeforeEach
    public void setup() {
        int port = 8080; // In a real test, get this from the test environment
        WEBSOCKET_URI = "ws://localhost:" + port + "/ws/tracking";

        stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    @Test
    public void testSubscribeToDriverLocation() throws Exception {
        // Given
        UUID driverId = UUID.randomUUID();
        LocationUpdate locationUpdate = createTestLocationUpdate(driverId);
        when(locationUpdateService.getLatestLocationUpdateByDriverId(driverId))
                .thenReturn(com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toDto(locationUpdate));

        // Create a latch to wait for the message
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<LocationUpdate> receivedUpdate = new AtomicReference<>();

        // Connect and subscribe
        StompSession session = stompClient.connect(WEBSOCKET_URI, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);

        session.subscribe(WEBSOCKET_TOPIC + driverId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LocationUpdate.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedUpdate.set((LocationUpdate) payload);
                latch.countDown();
            }
        });

        // Subscribe to the driver location
        session.send("/app/driver/" + driverId + "/subscribe", null);

        // Wait for the message
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Did not receive location update within timeout");

        // Verify the received update
        LocationUpdate received = receivedUpdate.get();
        assertNotNull(received);
        assertEquals(driverId, received.getDriverId());
        assertEquals(locationUpdate.getLatitude(), received.getLatitude());
        assertEquals(locationUpdate.getLongitude(), received.getLongitude());

        // Verify service method was called
        verify(locationUpdateService).getLatestLocationUpdateByDriverId(driverId);
    }

    @Test
    public void testReceiveLocationUpdate() throws Exception {
        // Given
        UUID driverId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        LocationUpdate locationUpdate = createTestLocationUpdate(driverId, deliveryId);
        when(locationUpdateService.createLocationUpdate(any()))
                .thenReturn(com.couriersync.tracking.mapper.LocationUpdateMapper.INSTANCE.toDto(locationUpdate));

        // Create a latch to wait for the message
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<LocationUpdate> receivedUpdate = new AtomicReference<>();

        // Connect and subscribe
        StompSession session = stompClient.connect(WEBSOCKET_URI, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);

        session.subscribe(WEBSOCKET_TOPIC + driverId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LocationUpdate.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedUpdate.set((LocationUpdate) payload);
                latch.countDown();
            }
        });

        // Send a location update
        session.send("/app/location/update", locationUpdate);

        // Wait for the message
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Did not receive location update within timeout");

        // Verify the received update
        LocationUpdate received = receivedUpdate.get();
        assertNotNull(received);
        assertEquals(driverId, received.getDriverId());
        assertEquals(deliveryId, received.getDeliveryId());
        assertEquals(locationUpdate.getLatitude(), received.getLatitude());
        assertEquals(locationUpdate.getLongitude(), received.getLongitude());

        // Verify service method was called
        verify(locationUpdateService).createLocationUpdate(any());
        verify(messagingTemplate).convertAndSend(eq(WEBSOCKET_TOPIC + driverId), any(LocationUpdate.class));
    }

    private LocationUpdate createTestLocationUpdate(UUID driverId) {
        return createTestLocationUpdate(driverId, null);
    }

    private LocationUpdate createTestLocationUpdate(UUID driverId, UUID deliveryId) {
        return LocationUpdate.builder()
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
}
