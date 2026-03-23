package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.domain.CustomOrderStatus;
import jakarta.validation.constraints.NotNull;

public record PatchCustomOrderStatusRequest(@NotNull CustomOrderStatus status) {}
