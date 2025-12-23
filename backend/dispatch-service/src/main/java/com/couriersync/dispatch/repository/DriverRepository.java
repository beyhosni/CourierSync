package com.couriersync.dispatch.repository;

import com.couriersync.dispatch.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByUserId(UUID userId);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    List<Driver> findByStatus(Driver.Status status);

    @Query("SELECT d FROM Driver d WHERE d.licenseExpiryDate < :expiryDate")
    List<Driver> findDriversWithExpiringLicense(@Param("expiryDate") LocalDate expiryDate);

    @Query("SELECT d FROM Driver d WHERE d.status = :status AND d.vehicleType = :vehicleType")
    List<Driver> findByStatusAndVehicleType(
            @Param("status") Driver.Status status,
            @Param("vehicleType") Driver.VehicleType vehicleType
    );

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.status = :status")
    long countByStatus(@Param("status") Driver.Status status);

    @Query("SELECT d FROM Driver d WHERE d.status = :status ORDER BY d.lastLocationUpdate DESC NULLS LAST")
    List<Driver> findAvailableDriversOrderByLocationUpdate(@Param("status") Driver.Status status);
}
