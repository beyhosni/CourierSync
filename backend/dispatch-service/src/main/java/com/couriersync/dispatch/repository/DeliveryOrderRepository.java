package com.couriersync.dispatch.repository;

import com.couriersync.dispatch.model.DeliveryOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, UUID> {

    Optional<DeliveryOrder> findByOrderNumber(String orderNumber);

    Page<DeliveryOrder> findByStatus(DeliveryOrder.Status status, Pageable pageable);

    Page<DeliveryOrder> findByAssignedDriverId(UUID driverId, Pageable pageable);

    List<DeliveryOrder> findByCustomerId(UUID customerId);

    @Query("SELECT d FROM DeliveryOrder d WHERE d.status = :status AND d.requestedPickupTime BETWEEN :startDate AND :endDate")
    List<DeliveryOrder> findByStatusAndRequestedPickupTimeBetween(
            @Param("status") DeliveryOrder.Status status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT d FROM DeliveryOrder d WHERE d.assignedDriverId = :driverId AND d.status IN :statuses")
    List<DeliveryOrder> findByDriverIdAndStatusIn(
            @Param("driverId") UUID driverId,
            @Param("statuses") List<DeliveryOrder.Status> statuses
    );

    @Query("SELECT COUNT(d) FROM DeliveryOrder d WHERE d.status = :status")
    long countByStatus(@Param("status") DeliveryOrder.Status status);
}
