package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record PatchCustomOrderStatusRequest(@NotNull OrderStatus status) {}
