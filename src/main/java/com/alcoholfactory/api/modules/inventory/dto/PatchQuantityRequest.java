package com.alcoholfactory.api.modules.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record PatchQuantityRequest(
        @NotNull Integer delta
) {}
