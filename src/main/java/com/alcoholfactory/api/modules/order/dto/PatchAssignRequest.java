package com.alcoholfactory.api.modules.order.dto;

import jakarta.validation.constraints.NotNull;

public record PatchAssignRequest(@NotNull Long assigneeUserId) {}
