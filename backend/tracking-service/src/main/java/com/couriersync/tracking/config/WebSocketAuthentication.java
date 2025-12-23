package com.couriersync.tracking.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public class WebSocketAuthentication extends AbstractAuthenticationToken {

    private final String token;
    private final UUID userId;

    public WebSocketAuthentication(String token) {
        super(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.token = token;

        // In a real implementation, extract user ID from JWT token
        // For simplicity, we're generating a random UUID here
        this.userId = UUID.randomUUID();
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return userId.toString();
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
