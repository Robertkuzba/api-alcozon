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
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmStaffOrderPushService {

  static final Set<UserRole> STAFF_ROLES = Set.of(UserRole.EMPLOYEE, UserRole.MANAGER);
  static final Set<UserRole> MANAGER_ROLES = Set.of(UserRole.MANAGER);

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
      var stream =
          new ByteArrayInputStream(
              firebaseProperties.serviceAccountJson().getBytes(StandardCharsets.UTF_8));
      GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
        FirebaseApp.initializeApp(options);
      }
      this.firebaseMessaging = FirebaseMessaging.getInstance();
      log.info("Firebase FCM initialized");
    } catch (Exception e) {
      log.warn("Firebase FCM init failed, push disabled: {}", e.toString());
    }
  }

  public void notifyStaffRoles(
      String title, String body, OrderRealtimeEvent event, Collection<UserRole> roles) {
    if (firebaseMessaging == null) {
      return;
    }
    List<String> tokens = fcmDeviceTokenRepository.findDistinctTokensByUserRoles(roles);
    sendMulticast(title, body, event, tokens);
  }

  public void notifyUser(long userId, String title, String body, OrderRealtimeEvent event) {
    if (firebaseMessaging == null) {
      return;
    }
    List<String> tokens = fcmDeviceTokenRepository.findDistinctTokensByUserId(userId);
    sendMulticast(title, body, event, tokens);
  }

  private void sendMulticast(
      String title, String body, OrderRealtimeEvent event, List<String> tokens) {
    if (tokens.isEmpty()) {
      return;
    }
    Notification notification = Notification.builder().setTitle(title).setBody(body).build();
    Map<String, String> data = toDataMap(event);
    int batchSize = 500;
    for (int i = 0; i < tokens.size(); i += batchSize) {
      List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));
      try {
        MulticastMessage message =
            MulticastMessage.builder()
                .addAllTokens(batch)
                .setNotification(notification)
                .putAllData(data)
                .build();
        firebaseMessaging.sendEachForMulticast(message);
      } catch (Exception e) {
        log.warn("FCM send batch failed: {}", e.toString());
      }
    }
  }

  private static Map<String, String> toDataMap(OrderRealtimeEvent event) {
    Map<String, String> data = new HashMap<>();
    data.put("type", event.type().name());
    data.put("orderId", Long.toString(event.orderId()));
    data.put("status", event.status());
    if (event.clientOrderNumber() != null) {
      data.put("clientOrderNumber", event.clientOrderNumber());
    }
    if (event.deliveryId() != null) {
      data.put("deliveryId", event.deliveryId().toString());
    }
    if (event.courierUserId() != null) {
      data.put("courierUserId", event.courierUserId().toString());
    }
    return data;
  }
}
