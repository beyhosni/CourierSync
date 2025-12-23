package com.couriersync.dispatch.security;

import com.couriersync.dispatch.model.DeliveryOrder;
import com.couriersync.dispatch.model.Driver;
import com.couriersync.dispatch.repository.DeliveryOrderRepository;
import com.couriersync.dispatch.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryAuthorizationGuard {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final DriverRepository driverRepository;

    public boolean canAccessDelivery(UUID deliveryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and dispatchers can access any delivery
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            return true;
        }

        // Drivers can only access their assigned deliveries
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                Driver driver = driverRepository.findByUserId(UUID.fromString(userId))
                        .orElseThrow(() -> new RuntimeException("Driver not found for user: " + userId));

                DeliveryOrder delivery = deliveryOrderRepository.findById(deliveryId)
                        .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

                return driver.getId().equals(delivery.getAssignedDriverId());
            } catch (Exception e) {
                log.error("Error checking delivery access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canUpdateDeliveryStatus(UUID deliveryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and dispatchers can update any delivery status
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            return true;
        }

        // Drivers can only update status of their assigned deliveries
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                Driver driver = driverRepository.findByUserId(UUID.fromString(userId))
                        .orElseThrow(() -> new RuntimeException("Driver not found for user: " + userId));

                DeliveryOrder delivery = deliveryOrderRepository.findById(deliveryId)
                        .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

                return driver.getId().equals(delivery.getAssignedDriverId());
            } catch (Exception e) {
                log.error("Error checking delivery status update access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canAssignDriver() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin and dispatchers can assign drivers
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"));
    }

    public boolean canAccessDriver(UUID driverId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and dispatchers can access any driver
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            return true;
        }

        // Drivers can only access their own profile
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                Driver driver = driverRepository.findByUserId(UUID.fromString(userId))
                        .orElseThrow(() -> new RuntimeException("Driver not found for user: " + userId));

                return driver.getId().equals(driverId);
            } catch (Exception e) {
                log.error("Error checking driver access", e);
                return false;
            }
        }

        return false;
    }
}
