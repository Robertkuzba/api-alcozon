package com.alcoholfactory.api.modules.admin.dto;

import java.time.Instant;

public record AnnouncementResponse(
    Long id,
    String title,
    String content,
    Instant publishedAt,
    Long createdById,
    Instant createdAt) {}
