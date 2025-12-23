package com.couriersync.billing.mapper;

import com.couriersync.common.dto.InvoiceDto;
import com.couriersync.common.dto.InvoiceItemDto;
import com.couriersync.billing.model.Invoice;
import com.couriersync.billing.model.InvoiceItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "paymentReference", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
    Invoice toEntity(InvoiceDto dto);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToDto")
    InvoiceDto toDto(Invoice entity);

    List<InvoiceDto> toDtoList(List<Invoice> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "lineTotal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "itemType", source = "itemType", qualifiedByName = "mapItemType")
    InvoiceItem toItemEntity(InvoiceItemDto dto);

    @Mapping(target = "itemType", source = "itemType", qualifiedByName = "mapItemTypeToDto")
    InvoiceItemDto toItemDto(InvoiceItem entity);

    List<InvoiceItemDto> toItemDtoList(List<InvoiceItem> entities);

    @Named("mapStatus")
    default Invoice.InvoiceStatus mapStatus(InvoiceDto.InvoiceStatus status) {
        if (status == null) {
            return null;
        }
        return Invoice.InvoiceStatus.valueOf(status.name());
    }

    @Named("mapStatusToDto")
    default InvoiceDto.InvoiceStatus mapStatusToDto(Invoice.InvoiceStatus status) {
        if (status == null) {
            return null;
        }
        return InvoiceDto.InvoiceStatus.valueOf(status.name());
    }

    @Named("mapItemType")
    default InvoiceItem.ItemType mapItemType(InvoiceItemDto.ItemType itemType) {
        if (itemType == null) {
            return null;
        }
        return InvoiceItem.ItemType.valueOf(itemType.name());
    }

    @Named("mapItemTypeToDto")
    default InvoiceItemDto.ItemType mapItemTypeToDto(InvoiceItem.ItemType itemType) {
        if (itemType == null) {
            return null;
        }
        return InvoiceItemDto.ItemType.valueOf(itemType.name());
    }
}
