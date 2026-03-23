package com.alcoholfactory.api.modules.delivery.dto;

import jakarta.validation.constraints.NotNull;

public record PatchDeliveryAssignRequest(@NotNull Long courierId) {}
