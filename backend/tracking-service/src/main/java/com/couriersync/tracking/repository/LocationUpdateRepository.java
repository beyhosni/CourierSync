package com.couriersync.tracking.repository;

import com.couriersync.tracking.model.LocationUpdate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationUpdateRepository extends MongoRepository<LocationUpdate, String> {

    List<LocationUpdate> findByDriverIdOrderByTimestampDesc(UUID driverId);

    List<LocationUpdate> findByDeliveryIdOrderByTimestampAsc(UUID deliveryId);

    List<LocationUpdate> findByDriverIdAndTimestampBetweenOrderByTimestampAsc(
            UUID driverId, LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = "{ 'driverId': ?0, 'timestamp': { $gte: ?1 } }", sort = "{ 'timestamp': -1 }")
    LocationUpdate findLatestByDriverIdAfter(UUID driverId, LocalDateTime timestamp);

    @Query(value = "{ 'deliveryId': ?0 }", fields = "{ 'latitude': 1, 'longitude': 1, 'timestamp': 1 }")
    List<LocationUpdate> findCoordinatesByDeliveryId(UUID deliveryId);

    @Query(value = "{ 'driverId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }", 
           fields = "{ 'latitude': 1, 'longitude': 1, 'timestamp': 1 }")
    List<LocationUpdate> findCoordinatesByDriverIdAndTimestampBetween(
            UUID driverId, LocalDateTime startTime, LocalDateTime endTime);
}
