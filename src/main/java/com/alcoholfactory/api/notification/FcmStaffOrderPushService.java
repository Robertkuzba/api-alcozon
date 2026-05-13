package com.alcoholfactory.api.notification;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.config.FirebaseProperties;
import com.alcoholfactory.api.modules.device.repository.FcmDeviceTokenRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmStaffOrderPushService {

    private final FirebaseProperties firebaseProperties;
    private final FcmDeviceTokenRepository fcmDeviceTokenRepository;

    private volatile FirebaseMessaging firebaseMessaging;

    @PostConstruct
    void init() {
        if (!StringUtils.hasText(firebaseProperties.serviceAccountJson())) {
            log.info("FCM disabled (app.firebase.service-account-json empty)");
            return;
        }
        try {
            var stream = new ByteArrayInputStream(
                    firebaseProperties.serviceAccountJson().getBytes(StandardCharsets.UTF_8));
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                FirebaseApp.initializeApp(options);
            }
            this.firebaseMessaging = FirebaseMessaging.getInstance();
            log.info("Firebase FCM initialized");
        } catch (Exception e) {
            log.warn("Firebase FCM init failed, push disabled: {}", e.toString());
        }
    }

    /**
     * Powiadomienie push do pracowników/managerów z zarejestrowanym tokenem FCM (gdy Firebase jest skonfigurowany).
     */
    public void notifyNewOrderSubmitted(long orderId) {
        if (firebaseMessaging == null) {
            return;
        }
        List<String> tokens = fcmDeviceTokenRepository.findDistinctTokensByUserRoles(
                Set.of(UserRole.EMPLOYEE, UserRole.MANAGER)
        );
        if (tokens.isEmpty()) {
            return;
        }
        Notification notification = Notification.builder()
                .setTitle("Nowe zamówienie")
                .setBody("Zamówienie #" + orderId + " (SUBMITTED)")
                .build();
        int batchSize = 500;
        for (int i = 0; i < tokens.size(); i += batchSize) {
            List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));
            try {
                MulticastMessage message = MulticastMessage.builder()
                        .addAllTokens(batch)
                        .setNotification(notification)
                        .putData("orderId", Long.toString(orderId))
                        .putData("status", "SUBMITTED")
                        .build();
                firebaseMessaging.sendEachForMulticast(message);
            } catch (Exception e) {
                log.warn("FCM send batch failed: {}", e.toString());
            }
        }
    }
}
