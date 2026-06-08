package com.alcoholfactory.api.modules.admin.dto;

import com.alcoholfactory.api.common.domain.JobOfferStatus;
import java.time.Instant;

public record JobOfferResponse(
    Long id,
    String title,
    String description,
    JobOfferStatus status,
    Instant createdAt,
    Instant updatedAt) {}
