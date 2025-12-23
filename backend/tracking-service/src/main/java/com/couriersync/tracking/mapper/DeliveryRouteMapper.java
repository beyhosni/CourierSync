package com.couriersync.tracking.mapper;

import com.couriersync.tracking.model.DeliveryRoute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeliveryRouteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "routePoints", ignore = true)
    @Mapping(target = "actualDistance", ignore = true)
    @Mapping(target = "actualDuration", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
    DeliveryRoute toEntity(DeliveryRouteDto dto);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToDto")
    DeliveryRouteDto toDto(DeliveryRoute entity);

    List<DeliveryRouteDto> toDtoList(List<DeliveryRoute> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deliveryId", ignore = true)
    @Mapping(target = "driverId", ignore = true)
    @Mapping(target = "pickupLocation", ignore = true)
    @Mapping(target = "dropoffLocation", ignore = true)
    @Mapping(target = "estimatedDistance", ignore = true)
    @Mapping(target = "estimatedDuration", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
    void updateEntityFromDto(DeliveryRouteDto dto, @MappingTarget DeliveryRoute entity);

    @Named("mapStatus")
    default DeliveryRoute.RouteStatus mapStatus(DeliveryRouteDto.RouteStatus status) {
        if (status == null) {
            return null;
        }
        return DeliveryRoute.RouteStatus.valueOf(status.name());
    }

    @Named("mapStatusToDto")
    default DeliveryRouteDto.RouteStatus mapStatusToDto(DeliveryRoute.RouteStatus status) {
        if (status == null) {
            return null;
        }
        return DeliveryRouteDto.RouteStatus.valueOf(status.name());
    }

    // DTO class definition
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class DeliveryRouteDto {
        private String id;
        private java.util.UUID deliveryId;
        private java.util.UUID driverId;
        private Location pickupLocation;
        private Location dropoffLocation;
        private java.util.List<LocationPoint> routePoints;
        private java.math.BigDecimal estimatedDistance;
        private Integer estimatedDuration;
        private java.math.BigDecimal actualDistance;
        private Integer actualDuration;
        private RouteStatus status;
        private java.time.LocalDateTime startedAt;
        private java.time.LocalDateTime completedAt;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        public enum RouteStatus {
            PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class Location {
            private String name;
            private String address;
            private java.math.BigDecimal latitude;
            private java.math.BigDecimal longitude;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class LocationPoint {
            private java.math.BigDecimal latitude;
            private java.math.BigDecimal longitude;
            private java.time.LocalDateTime timestamp;
            private java.math.BigDecimal speed;
            private java.math.BigDecimal heading;
        }
    }
}
