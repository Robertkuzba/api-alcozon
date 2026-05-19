package com.alcoholfactory.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tymczasowy hook do testów FCM/STOMP (mobilka). Domyślnie wyłączony.
 */
@ConfigurationProperties(prefix = "app.dev.notification-test-hook")
public record DevNotificationTestHookProperties(boolean enabled) {}
