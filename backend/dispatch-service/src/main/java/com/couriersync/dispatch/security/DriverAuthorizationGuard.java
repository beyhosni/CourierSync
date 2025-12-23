package com.couriersync.dispatch.security;

import com.couriersync.dispatch.model.Driver;
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
public class DriverAuthorizationGuard {

    private final DriverRepository driverRepository;

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

    public boolean canCreateDriver() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin and dispatchers can create drivers
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"));
    }

    public boolean canUpdateDriver(UUID driverId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and dispatchers can update any driver
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            return true;
        }

        // Drivers can update their own profile
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                Driver driver = driverRepository.findByUserId(UUID.fromString(userId))
                        .orElseThrow(() -> new RuntimeException("Driver not found for user: " + userId));

                return driver.getId().equals(driverId);
            } catch (Exception e) {
                log.error("Error checking driver update access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canDeleteDriver(UUID driverId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin can delete drivers
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public boolean canUpdateDriverStatus(UUID driverId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and dispatchers can update any driver status
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            return true;
        }

        // Drivers can update their own status
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                Driver driver = driverRepository.findByUserId(UUID.fromString(userId))
                        .orElseThrow(() -> new RuntimeException("Driver not found for user: " + userId));

                return driver.getId().equals(driverId);
            } catch (Exception e) {
                log.error("Error checking driver status update access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canUpdateDriverLocation(UUID driverId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only drivers can update their own location
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            try {
                String userId = authentication.getName();
                Driver driver = driverRepository.findByUserId(UUID.fromString(userId))
                        .orElseThrow(() -> new RuntimeException("Driver not found for user: " + userId));

                return driver.getId().equals(driverId);
            } catch (Exception e) {
                log.error("Error checking driver location update access", e);
                return false;
            }
        }

        return false;
    }
}
