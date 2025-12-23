package com.couriersync.tracking.repository;

import com.couriersync.tracking.model.DeliveryRoute;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryRouteRepository extends MongoRepository<DeliveryRoute, String> {

    List<DeliveryRoute> findByDeliveryId(UUID deliveryId);

    List<DeliveryRoute> findByDriverId(UUID driverId);

    List<DeliveryRoute> findByStatus(DeliveryRoute.RouteStatus status);

    @Query(value = "{ 'driverId': ?0, 'status': ?1 }", sort = "{ 'createdAt': -1 }")
    DeliveryRoute findLatestByDriverIdAndStatus(UUID driverId, DeliveryRoute.RouteStatus status);

    @Query(value = "{ 'driverId': ?0, 'status': ?1, 'createdAt': { $gte: ?2 } }")
    List<DeliveryRoute> findByDriverIdAndStatusAndCreatedAtAfter(
            UUID driverId, DeliveryRoute.RouteStatus status, LocalDateTime date);

    @Query(value = "{ 'deliveryId': ?0 }", fields = "{ 'routePoints': 1 }")
    DeliveryRoute findRoutePointsByDeliveryId(UUID deliveryId);

    @Query(value = "{ 'deliveryId': ?0 }", fields = "{ 'routePoints': 1, 'status': 1 }")
    DeliveryRoute findRoutePointsAndStatusByDeliveryId(UUID deliveryId);

    @Query(value = "{ 'deliveryId': ?0 }", fields = "{ 'routePoints': { $slice: -100 } }")
    DeliveryRoute findLast100RoutePointsByDeliveryId(UUID deliveryId);
}
