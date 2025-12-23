package com.couriersync.dispatch.mapper;

import com.couriersync.dispatch.model.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentLatitude", ignore = true)
    @Mapping(target = "currentLongitude", ignore = true)
    @Mapping(target = "lastLocationUpdate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "vehicleType", source = "vehicleType", qualifiedByName = "mapVehicleType")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
    Driver toEntity(DriverDto dto);

    @Mapping(target = "vehicleType", source = "vehicleType", qualifiedByName = "mapVehicleTypeToDto")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToDto")
    DriverDto toDto(Driver entity);

    List<DriverDto> toDtoList(List<Driver> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "licenseNumber", ignore = true)
    @Mapping(target = "licenseExpiryDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "vehicleType", source = "vehicleType", qualifiedByName = "mapVehicleType")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
    void updateEntityFromDto(DriverDto dto, @MappingTarget Driver entity);

    @Named("mapVehicleType")
    default Driver.VehicleType mapVehicleType(DriverDto.VehicleType vehicleType) {
        if (vehicleType == null) {
            return null;
        }
        return Driver.VehicleType.valueOf(vehicleType.name());
    }

    @Named("mapVehicleTypeToDto")
    default DriverDto.VehicleType mapVehicleTypeToDto(Driver.VehicleType vehicleType) {
        if (vehicleType == null) {
            return null;
        }
        return DriverDto.VehicleType.valueOf(vehicleType.name());
    }

    @Named("mapStatus")
    default Driver.Status mapStatus(DriverDto.Status status) {
        if (status == null) {
            return null;
        }
        return Driver.Status.valueOf(status.name());
    }

    @Named("mapStatusToDto")
    default DriverDto.Status mapStatusToDto(Driver.Status status) {
        if (status == null) {
            return null;
        }
        return DriverDto.Status.valueOf(status.name());
    }

    // DTO class definition
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class DriverDto {
        private java.util.UUID id;
        private java.util.UUID userId;
        private String licenseNumber;
        private java.time.LocalDate licenseExpiryDate;
        private VehicleType vehicleType;
        private String vehiclePlate;
        private String vehicleModel;
        private Status status;
        private java.math.BigDecimal currentLatitude;
        private java.math.BigDecimal currentLongitude;
        private java.time.LocalDateTime lastLocationUpdate;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        public enum VehicleType {
            CAR, VAN, MOTORCYCLE, BIKE
        }

        public enum Status {
            AVAILABLE, ON_DUTY, OFF_DUTY, BREAK
        }
    }
}
