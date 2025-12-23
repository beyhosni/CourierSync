package com.couriersync.dispatch.service;

import com.couriersync.dispatch.model.Driver;
import com.couriersync.dispatch.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DriverService {

    private final DriverRepository driverRepository;

    public Driver createDriver(Driver driver) {
        log.info("Creating new driver for user: {}", driver.getUserId());

        // Set default status if not provided
        if (driver.getStatus() == null) {
            driver.setStatus(Driver.Status.AVAILABLE);
        }

        Driver savedDriver = driverRepository.save(driver);
        log.info("Created driver with ID: {}", savedDriver.getId());
        return savedDriver;
    }

    public Optional<Driver> getDriverById(UUID id) {
        return driverRepository.findById(id);
    }

    public Optional<Driver> getDriverByUserId(UUID userId) {
        return driverRepository.findByUserId(userId);
    }

    public Optional<Driver> getDriverByLicenseNumber(String licenseNumber) {
        return driverRepository.findByLicenseNumber(licenseNumber);
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public List<Driver> getDriversByStatus(Driver.Status status) {
        return driverRepository.findByStatus(status);
    }

    public Driver updateDriver(UUID id, Driver driverDetails) {
        log.info("Updating driver with ID: {}", id);

        return driverRepository.findById(id)
                .map(driver -> {
                    // Update driver information
                    driver.setLicenseNumber(driverDetails.getLicenseNumber());
                    driver.setLicenseExpiryDate(driverDetails.getLicenseExpiryDate());

                    // Update vehicle information
                    driver.setVehicleType(driverDetails.getVehicleType());
                    driver.setVehiclePlate(driverDetails.getVehiclePlate());
                    driver.setVehicleModel(driverDetails.getVehicleModel());

                    // Update status
                    driver.setStatus(driverDetails.getStatus());

                    // Update location if provided
                    if (driverDetails.getCurrentLatitude() != null && driverDetails.getCurrentLongitude() != null) {
                        driver.setCurrentLatitude(driverDetails.getCurrentLatitude());
                        driver.setCurrentLongitude(driverDetails.getCurrentLongitude());
                        driver.setLastLocationUpdate(java.time.LocalDateTime.now());
                    }

                    return driverRepository.save(driver);
                })
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + id));
    }

    public Driver updateDriverLocation(UUID driverId, BigDecimal latitude, BigDecimal longitude) {
        log.info("Updating location for driver {}: lat={}, lng={}", driverId, latitude, longitude);

        return driverRepository.findById(driverId)
                .map(driver -> {
                    driver.setCurrentLatitude(latitude);
                    driver.setCurrentLongitude(longitude);
                    driver.setLastLocationUpdate(java.time.LocalDateTime.now());

                    return driverRepository.save(driver);
                })
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));
    }

    public Driver updateDriverStatus(UUID driverId, Driver.Status status) {
        log.info("Updating status for driver {} to {}", driverId, status);

        return driverRepository.findById(driverId)
                .map(driver -> {
                    driver.setStatus(status);
                    return driverRepository.save(driver);
                })
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));
    }

    public void deleteDriver(UUID id) {
        log.info("Deleting driver with ID: {}", id);

        if (!driverRepository.existsById(id)) {
            throw new RuntimeException("Driver not found with ID: " + id);
        }

        driverRepository.deleteById(id);
    }

    public List<Driver> findDriversWithExpiringLicense(LocalDate expiryDate) {
        return driverRepository.findDriversWithExpiringLicense(expiryDate);
    }

    public List<Driver> findAvailableDriversByVehicleType(Driver.VehicleType vehicleType) {
        return driverRepository.findByStatusAndVehicleType(Driver.Status.AVAILABLE, vehicleType);
    }

    public List<Driver> findAvailableDriversOrderByLocationUpdate() {
        return driverRepository.findAvailableDriversOrderByLocationUpdate(Driver.Status.AVAILABLE);
    }

    public long countDriversByStatus(Driver.Status status) {
        return driverRepository.countByStatus(status);
    }
}
