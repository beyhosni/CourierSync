package com.couriersync.tracking.security;

import com.couriersync.tracking.model.LocationUpdate;
import com.couriersync.tracking.repository.LocationUpdateRepository;
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
public class LocationAuthorizationGuard {

    private final LocationUpdateRepository locationUpdateRepository;

    public boolean canAccessLocationUpdates(UUID driverId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and dispatchers can access any driver's location
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            return true;
        }

        // Drivers can only access their own location updates
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                UUID userUuid = UUID.fromString(userId);

                // In a real system, we would fetch the driver ID from the user service
                // For now, we assume the user ID is the same as the driver ID
                return userUuid.equals(driverId);
            } catch (Exception e) {
                log.error("Error checking location access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canUpdateLocation(UUID driverId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only drivers can update their own location
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                UUID userUuid = UUID.fromString(userId);

                // In a real system, we would fetch the driver ID from the user service
                // For now, we assume the user ID is the same as the driver ID
                return userUuid.equals(driverId);
            } catch (Exception e) {
                log.error("Error checking location update access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canAccessRoute(UUID deliveryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and dispatchers can access any route
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            return true;
        }

        // Drivers can only access routes of their assigned deliveries
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                UUID userUuid = UUID.fromString(userId);

                // In a real system, we would check if the driver is assigned to this delivery
                // For now, we'll assume a simple check
                return true; // Simplified for demonstration
            } catch (Exception e) {
                log.error("Error checking route access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canCreateRoute() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin and dispatchers can create routes
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"));
    }

    public boolean canUpdateRoute(UUID deliveryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and dispatchers can update any route
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            return true;
        }

        // Drivers can only update routes of their assigned deliveries
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                UUID userUuid = UUID.fromString(userId);

                // In a real system, we would check if the driver is assigned to this delivery
                // For now, we'll assume a simple check
                return true; // Simplified for demonstration
            } catch (Exception e) {
                log.error("Error checking route update access", e);
                return false;
            }
        }

        return false;
    }
}
