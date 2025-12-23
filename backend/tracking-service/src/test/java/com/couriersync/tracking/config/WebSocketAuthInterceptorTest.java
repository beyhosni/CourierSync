package com.couriersync.tracking.config;

import com.couriersync.tracking.security.LocationAuthorizationGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    private LocationAuthorizationGuard locationAuthorizationGuard;

    @InjectMocks
    private WebSocketAuthInterceptor authInterceptor;

    private StompHeaderAccessor accessor;
    private MessageChannel channel;
    private Map<String, Object> headers;

    @BeforeEach
    void setUp() {
        headers = new HashMap<>();
        accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeaders(headers);
        channel = mock(MessageChannel.class);
    }

    @Test
    void testConnectWithValidToken() {
        // Given
        String token = "valid-jwt-token";
        headers.put("Authorization", "Bearer " + token);
        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(accessor.getMessageHeaders());

        // When
        Message<?> result = authInterceptor.preSend(message, channel);

        // Then
        assertNotNull(result);
        assertNotNull(accessor.getUser());
        assertEquals(result, message);
    }

    @Test
    void testConnectWithoutToken() {
        // Given
        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(accessor.getMessageHeaders());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authInterceptor.preSend(message, channel);
        });
    }

    @Test
    void testConnectWithInvalidToken() {
        // Given
        String token = "invalid-token";
        headers.put("Authorization", token); // Missing "Bearer " prefix
        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(accessor.getMessageHeaders());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authInterceptor.preSend(message, channel);
        });
    }

    @Test
    void testSubscribeToDriverLocationAuthorized() {
        // Given
        UUID driverId = UUID.randomUUID();
        String token = "valid-jwt-token";
        headers.put("Authorization", "Bearer " + token);

        // First, simulate a CONNECT to set up authentication
        accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeaders(headers);
        Message<?> connectMessage = mock(Message.class);
        when(connectMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());
        authInterceptor.preSend(connectMessage, channel);

        // Now simulate a SUBSCRIBE
        accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/driver/" + driverId);
        accessor.setUser(accessor.getUser());
        Message<?> subscribeMessage = mock(Message.class);
        when(subscribeMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());

        when(locationAuthorizationGuard.canAccessLocationUpdates(driverId)).thenReturn(true);

        // When
        Message<?> result = authInterceptor.preSend(subscribeMessage, channel);

        // Then
        assertNotNull(result);
        assertEquals(result, subscribeMessage);
        verify(locationAuthorizationGuard).canAccessLocationUpdates(driverId);
    }

    @Test
    void testSubscribeToDriverLocationUnauthorized() {
        // Given
        UUID driverId = UUID.randomUUID();
        String token = "valid-jwt-token";
        headers.put("Authorization", "Bearer " + token);

        // First, simulate a CONNECT to set up authentication
        accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeaders(headers);
        Message<?> connectMessage = mock(Message.class);
        when(connectMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());
        authInterceptor.preSend(connectMessage, channel);

        // Now simulate a SUBSCRIBE
        accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/driver/" + driverId);
        accessor.setUser(accessor.getUser());
        Message<?> subscribeMessage = mock(Message.class);
        when(subscribeMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());

        when(locationAuthorizationGuard.canAccessLocationUpdates(driverId)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authInterceptor.preSend(subscribeMessage, channel);
        });
        verify(locationAuthorizationGuard).canAccessLocationUpdates(driverId);
    }

    @Test
    void testSubscribeToDeliveryLocationAuthorized() {
        // Given
        UUID deliveryId = UUID.randomUUID();
        String token = "valid-jwt-token";
        headers.put("Authorization", "Bearer " + token);

        // First, simulate a CONNECT to set up authentication
        accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeaders(headers);
        Message<?> connectMessage = mock(Message.class);
        when(connectMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());
        authInterceptor.preSend(connectMessage, channel);

        // Now simulate a SUBSCRIBE
        accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/delivery/" + deliveryId);
        accessor.setUser(accessor.getUser());
        Message<?> subscribeMessage = mock(Message.class);
        when(subscribeMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());

        when(locationAuthorizationGuard.canAccessRoute(deliveryId)).thenReturn(true);

        // When
        Message<?> result = authInterceptor.preSend(subscribeMessage, channel);

        // Then
        assertNotNull(result);
        assertEquals(result, subscribeMessage);
        verify(locationAuthorizationGuard).canAccessRoute(deliveryId);
    }

    @Test
    void testSubscribeToDeliveryLocationUnauthorized() {
        // Given
        UUID deliveryId = UUID.randomUUID();
        String token = "valid-jwt-token";
        headers.put("Authorization", "Bearer " + token);

        // First, simulate a CONNECT to set up authentication
        accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeaders(headers);
        Message<?> connectMessage = mock(Message.class);
        when(connectMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());
        authInterceptor.preSend(connectMessage, channel);

        // Now simulate a SUBSCRIBE
        accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/delivery/" + deliveryId);
        accessor.setUser(accessor.getUser());
        Message<?> subscribeMessage = mock(Message.class);
        when(subscribeMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());

        when(locationAuthorizationGuard.canAccessRoute(deliveryId)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authInterceptor.preSend(subscribeMessage, channel);
        });
        verify(locationAuthorizationGuard).canAccessRoute(deliveryId);
    }

    @Test
    void testSubscribeToUnknownDestination() {
        // Given
        String token = "valid-jwt-token";
        headers.put("Authorization", "Bearer " + token);

        // First, simulate a CONNECT to set up authentication
        accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeaders(headers);
        Message<?> connectMessage = mock(Message.class);
        when(connectMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());
        authInterceptor.preSend(connectMessage, channel);

        // Now simulate a SUBSCRIBE to an unknown destination
        accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/unknown");
        accessor.setUser(accessor.getUser());
        Message<?> subscribeMessage = mock(Message.class);
        when(subscribeMessage.getHeaders()).thenReturn(accessor.getMessageHeaders());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authInterceptor.preSend(subscribeMessage, channel);
        });
    }
}
