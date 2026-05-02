package com.ajlekc.monitoringservice.event;

import com.ajlekc.monitoringservice.model.ChangeType;

public record UserEvent(
        Integer externalId,
        String name,
        ChangeType changeType,
        String timestamp
) {}