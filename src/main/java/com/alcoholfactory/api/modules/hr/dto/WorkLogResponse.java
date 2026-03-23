package com.alcoholfactory.api.modules.hr.dto;

import java.time.Instant;

public record WorkLogResponse(
        Long id,
        Instant clockInAt,
        Instant clockOutAt,
        Instant breakStartedAt,
        Instant breakEndedAt,
        String notes
) {}
