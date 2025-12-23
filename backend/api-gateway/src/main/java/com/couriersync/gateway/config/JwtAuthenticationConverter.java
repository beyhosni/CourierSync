package com.couriersync.gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();

        // Extract roles from the "roles" claim if present
        if (claims.containsKey("roles")) {
            List<String> roles = (List<String>) claims.get("roles");
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }

        // Extract authorities from the "authorities" claim if present
        if (claims.containsKey("authorities")) {
            List<String> authorities = (List<String>) claims.get("authorities");
            return authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // Default role if no authorities are found
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
