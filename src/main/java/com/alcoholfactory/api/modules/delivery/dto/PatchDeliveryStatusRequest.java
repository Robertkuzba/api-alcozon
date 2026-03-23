package com.alcoholfactory.api.modules.delivery.dto;

import com.alcoholfactory.api.common.domain.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record PatchDeliveryStatusRequest(@NotNull DeliveryStatus status) {}
