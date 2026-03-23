package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record PatchOrderStatusRequest(@NotNull OrderStatus status) {}
