package com.couriersync.tracking.mapper;

import com.couriersync.common.dto.LocationUpdateDto;
import com.couriersync.tracking.model.LocationUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationUpdateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "mapTimestamp")
    LocationUpdate toEntity(LocationUpdateDto dto);

    LocationUpdateDto toDto(LocationUpdate entity);

    List<LocationUpdateDto> toDtoList(List<LocationUpdate> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "mapTimestamp")
    void updateEntityFromDto(LocationUpdateDto dto, @MappingTarget LocationUpdate entity);

    @Named("mapTimestamp")
    default java.time.LocalDateTime mapTimestamp(java.time.LocalDateTime timestamp) {
        return timestamp != null ? timestamp : java.time.LocalDateTime.now();
    }
}
