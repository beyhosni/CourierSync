package com.couriersync.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Auth Service
                .route("user-auth-service", r -> r.path("/api/auth/**", "/api/users/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("user-auth-cb")))
                        .uri("lb://user-auth-service"))

                // Dispatch Service
                .route("dispatch-service", r -> r.path("/api/deliveries/**", "/api/drivers/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("dispatch-cb")))
                        .uri("lb://dispatch-service"))

                // Tracking Service
                .route("tracking-service", r -> r.path("/api/tracking/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("tracking-cb")))
                        .uri("lb://tracking-service"))

                // Billing Service
                .route("billing-service", r -> r.path("/api/invoices/**", "/api/payments/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("billing-cb")))
                        .uri("lb://billing-service"))

                .build();
    }
}
