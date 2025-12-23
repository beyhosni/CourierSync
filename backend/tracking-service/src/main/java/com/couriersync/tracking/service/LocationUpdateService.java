package com.couriersync.tracking.service;

import com.couriersync.tracking.model.LocationUpdate;
import com.couriersync.tracking.repository.LocationUpdateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationUpdateService {

    private final LocationUpdateRepository locationUpdateRepository;
    private final DeliveryRouteService deliveryRouteService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String LATEST_LOCATION_KEY_PREFIX = "driver:latest_location:";
    private static final String LOCATION_UPDATES_TOPIC = "tracking.updates";

    public LocationUpdate saveLocationUpdate(LocationUpdate locationUpdate) {
        log.debug("Saving location update for driver: {}, delivery: {}", 
                locationUpdate.getDriverId(), locationUpdate.getDeliveryId());

        // Set timestamp if not provided
        if (locationUpdate.getTimestamp() == null) {
            locationUpdate.setTimestamp(LocalDateTime.now());
        }

        // Save to MongoDB
        LocationUpdate savedUpdate = locationUpdateRepository.save(locationUpdate);

        // Update latest location in Redis for quick access
        updateLatestLocationInRedis(savedUpdate);

        // Update route if delivery ID is provided
        if (locationUpdate.getDeliveryId() != null) {
            deliveryRouteService.addLocationPointToRoute(
                    locationUpdate.getDeliveryId(), 
                    locationUpdate);
        }

        // Send to Kafka for real-time updates
        kafkaTemplate.send(LOCATION_UPDATES_TOPIC, savedUpdate);

        log.debug("Saved location update with ID: {}", savedUpdate.getId());
        return savedUpdate;
    }

    public List<LocationUpdate> getLatestLocationUpdates(UUID driverId, int limit) {
        log.debug("Getting latest {} location updates for driver: {}", limit, driverId);
        Pageable pageable = PageRequest.of(0, limit);
        return locationUpdateRepository.findByDriverIdOrderByTimestampDesc(driverId);
    }

    public List<LocationUpdate> getLocationUpdatesForDelivery(UUID deliveryId) {
        log.debug("Getting location updates for delivery: {}", deliveryId);
        return locationUpdateRepository.findByDeliveryIdOrderByTimestampAsc(deliveryId);
    }

    public List<LocationUpdate> getLocationUpdatesForDriverInTimeRange(
            UUID driverId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Getting location updates for driver: {} between {} and {}", 
                driverId, startTime, endTime);
        return locationUpdateRepository.findByDriverIdAndTimestampBetweenOrderByTimestampAsc(
                driverId, startTime, endTime);
    }

    public LocationUpdate getLatestLocationUpdate(UUID driverId) {
        log.debug("Getting latest location update for driver: {}", driverId);

        // First try to get from Redis cache
        LocationUpdate cachedLocation = (LocationUpdate) redisTemplate.opsForValue()
                .get(LATEST_LOCATION_KEY_PREFIX + driverId);

        if (cachedLocation != null) {
            log.debug("Found latest location in cache for driver: {}", driverId);
            return cachedLocation;
        }

        // If not in cache, get from database
        LocationUpdate latestLocation = locationUpdateRepository.findLatestByDriverIdAfter(
                driverId, LocalDateTime.now().minusDays(7));

        if (latestLocation != null) {
            // Update cache
            updateLatestLocationInRedis(latestLocation);
        }

        return latestLocation;
    }

    public List<LocationUpdate> getCoordinatesForDelivery(UUID deliveryId) {
        log.debug("Getting coordinates for delivery: {}", deliveryId);
        return locationUpdateRepository.findCoordinatesByDeliveryId(deliveryId);
    }

    public List<LocationUpdate> getCoordinatesForDriverInTimeRange(
            UUID driverId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Getting coordinates for driver: {} between {} and {}", 
                driverId, startTime, endTime);
        return locationUpdateRepository.findCoordinatesByDriverIdAndTimestampBetween(
                driverId, startTime, endTime);
    }

    private void updateLatestLocationInRedis(LocationUpdate locationUpdate) {
        String key = LATEST_LOCATION_KEY_PREFIX + locationUpdate.getDriverId();
        redisTemplate.opsForValue().set(key, locationUpdate, 24, TimeUnit.HOURS);
    }

    public void saveLocationUpdatesBatch(List<LocationUpdate> locationUpdates) {
        log.info("Saving batch of {} location updates", locationUpdates.size());

        // Save all to MongoDB
        List<LocationUpdate> savedUpdates = locationUpdateRepository.saveAll(locationUpdates);

        // Update Redis cache for each driver
        savedUpdates.forEach(update -> {
            updateLatestLocationInRedis(update);

            // Update route if delivery ID is provided
            if (update.getDeliveryId() != null) {
                deliveryRouteService.addLocationPointToRoute(
                        update.getDeliveryId(), update);
            }

            // Send to Kafka
            kafkaTemplate.send(LOCATION_UPDATES_TOPIC, update);
        });

        log.info("Saved batch of {} location updates", savedUpdates.size());
    }
}
