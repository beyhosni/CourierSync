package com.couriersync.dispatch.repository;

import com.couriersync.dispatch.model.DeliveryStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, Long> {

    List<DeliveryStatusHistory> findByDeliveryId(UUID deliveryId);

    Page<DeliveryStatusHistory> findByDeliveryId(UUID deliveryId, Pageable pageable);

    @Query("SELECT h FROM DeliveryStatusHistory h WHERE h.delivery.id = :deliveryId ORDER BY h.changedAt DESC")
    List<DeliveryStatusHistory> findByDeliveryIdOrderByChangedAtDesc(@Param("deliveryId") UUID deliveryId);

    @Query("SELECT h FROM DeliveryStatusHistory h WHERE h.changedBy = :userId AND h.changedAt BETWEEN :startDate AND :endDate")
    List<DeliveryStatusHistory> findByChangedByAndChangedAtBetween(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT h FROM DeliveryStatusHistory h WHERE h.toStatus = :status AND h.changedAt BETWEEN :startDate AND :endDate")
    List<DeliveryStatusHistory> findByToStatusAndChangedAtBetween(
            @Param("status") com.couriersync.dispatch.model.DeliveryOrder.Status status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
