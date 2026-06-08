package com.alcoholfactory.api.modules.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String name,
    String description,
    String category,
    BigDecimal price,
    Integer volumeMl,
    BigDecimal abv,
    String imageUrl,
    boolean active,
    Integer stockQuantity) {}
