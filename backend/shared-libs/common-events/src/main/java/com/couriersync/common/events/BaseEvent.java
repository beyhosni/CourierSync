package com.couriersync.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String sourceService;

    public BaseEvent(String eventType, String sourceService) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.sourceService = sourceService;
    }
}
