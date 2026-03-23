package com.alcoholfactory.api.modules.product.dto;

import com.alcoholfactory.api.common.validation.ValidationPatterns;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank @Pattern(regexp = ValidationPatterns.SAFE_TEXT) @Size(max = 255) String name,
        @Pattern(regexp = ValidationPatterns.SAFE_TEXT) @Size(max = 5000) String description,
        @Pattern(regexp = ValidationPatterns.SAFE_TEXT) @Size(max = 100) String category,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        Integer volumeMl,
        BigDecimal abv,
        @Size(max = 500) String imageUrl,
        @NotNull Integer initialStock
) {}
