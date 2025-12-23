package com.couriersync.dispatch.mapper;

import com.couriersync.common.dto.DeliveryRequestDto;
import com.couriersync.dispatch.model.DeliveryOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeliveryOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "assignedDriverId", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "actualPickupTime", ignore = true)
    @Mapping(target = "actualDeliveryTime", ignore = true)
    @Mapping(target = "estimatedDeliveryTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "priority", source = "priority", qualifiedByName = "mapPriority")
    DeliveryOrder toEntity(DeliveryRequestDto dto);

    @Mapping(target = "priority", source = "priority", qualifiedByName = "mapPriorityToDto")
    DeliveryRequestDto toDto(DeliveryOrder entity);

    List<DeliveryRequestDto> toDtoList(List<DeliveryOrder> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "assignedDriverId", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "actualPickupTime", ignore = true)
    @Mapping(target = "actualDeliveryTime", ignore = true)
    @Mapping(target = "estimatedDeliveryTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "priority", source = "priority", qualifiedByName = "mapPriority")
    void updateEntityFromDto(DeliveryRequestDto dto, @MappingTarget DeliveryOrder entity);

    @Named("mapPriority")
    default DeliveryOrder.Priority mapPriority(DeliveryRequestDto.Priority priority) {
        if (priority == null) {
            return null;
        }
        return DeliveryOrder.Priority.valueOf(priority.name());
    }

    @Named("mapPriorityToDto")
    default DeliveryRequestDto.Priority mapPriorityToDto(DeliveryOrder.Priority priority) {
        if (priority == null) {
            return null;
        }
        return DeliveryRequestDto.Priority.valueOf(priority.name());
    }
}
