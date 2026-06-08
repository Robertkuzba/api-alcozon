package com.alcoholfactory.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Opcjonalna konfiguracja Firebase Admin (FCM).
 *
 * <p>{@code serviceAccountJson} — pełny JSON service account (np. zmienna środowiskowa {@code
 * FIREBASE_SERVICE_ACCOUNT_JSON} na Renderze). Puste = brak wysyłki push.
 */
@ConfigurationProperties(prefix = "app.firebase")
public record FirebaseProperties(String serviceAccountJson) {}
