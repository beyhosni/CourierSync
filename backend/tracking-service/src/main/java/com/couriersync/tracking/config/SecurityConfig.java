package com.couriersync.tracking.config;

import com.couriersync.common.security.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/locations/**").hasAnyRole("ADMIN", "DISPATCHER", "DRIVER")
                .requestMatchers(HttpMethod.POST, "/api/locations").hasAnyRole("ADMIN", "DRIVER")
                .requestMatchers(HttpMethod.GET, "/api/routes/**").hasAnyRole("ADMIN", "DISPATCHER", "DRIVER")
                .requestMatchers(HttpMethod.POST, "/api/routes").hasAnyRole("ADMIN", "DISPATCHER")
                .requestMatchers(HttpMethod.PUT, "/api/routes/**").hasAnyRole("ADMIN", "DISPATCHER")
                .requestMatchers("/ws/tracking/**").hasAnyRole("ADMIN", "DISPATCHER", "DRIVER")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter())));

        return http.build();
    }
}
