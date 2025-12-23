package com.couriersync.tracking.config;

import com.couriersync.tracking.security.LocationAuthorizationGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final LocationAuthorizationGuard locationAuthorizationGuard;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // For CONNECT command, validate JWT token
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                // In a real implementation, validate JWT token and set authentication
                // For simplicity, we're skipping the actual JWT validation here
                // but in production, you would validate the token and extract user info

                // Set authentication for the session
                Authentication auth = new WebSocketAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                accessor.setUser(auth);
            } else {
                throw new IllegalArgumentException("No authorization token provided");
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // For SUBSCRIBE command, check authorization
            String destination = accessor.getDestination();
            Authentication user = accessor.getUser();

            if (user != null && destination != null) {
                // Check subscription authorization based on destination
                if (!isSubscriptionAuthorized(user, destination)) {
                    throw new IllegalArgumentException("Not authorized to subscribe to " + destination);
                }
            }
        }

        return message;
    }

    private boolean isSubscriptionAuthorized(Authentication user, String destination) {
        // Parse the destination to extract driver ID or delivery ID
        if (destination.startsWith("/topic/driver/")) {
            try {
                String driverIdStr = destination.substring("/topic/driver/".length());
                UUID driverId = UUID.fromString(driverIdStr);
                return locationAuthorizationGuard.canAccessLocationUpdates(driverId);
            } catch (Exception e) {
                log.error("Error parsing driver ID from destination: " + destination, e);
                return false;
            }
        } else if (destination.startsWith("/topic/delivery/")) {
            try {
                String deliveryIdStr = destination.substring("/topic/delivery/".length());
                UUID deliveryId = UUID.fromString(deliveryIdStr);
                return locationAuthorizationGuard.canAccessRoute(deliveryId);
            } catch (Exception e) {
                log.error("Error parsing delivery ID from destination: " + destination, e);
                return false;
            }
        }

        // Default to false for unknown destinations
        return false;
    }
}
