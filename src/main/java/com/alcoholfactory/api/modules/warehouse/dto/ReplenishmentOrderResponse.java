package com.alcoholfactory.api.modules.warehouse.dto;

import java.time.Instant;
import java.util.List;

public record ReplenishmentOrderResponse(
    Long id,
    Long managerId,
    String managerEmail,
    String note,
    String status,
    Instant createdAt,
    List<ReplenishmentLineResponse> lines) {}
