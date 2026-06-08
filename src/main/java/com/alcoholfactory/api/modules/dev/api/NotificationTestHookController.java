package com.alcoholfactory.api.modules.dev.api;

import com.alcoholfactory.api.modules.dev.dto.NotificationTestHookResponse;
import com.alcoholfactory.api.modules.dev.service.NotificationTestHookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tymczasowy endpoint bez auth — tylko gdy {@code app.dev.notification-test-hook.enabled=true}. Po
 * testach FCM wyłącz flagę na Renderze.
 */
@RestController
@RequestMapping("/api/dev/notification-test")
@ConditionalOnProperty(name = "app.dev.notification-test-hook.enabled", havingValue = "true")
@RequiredArgsConstructor
@Tag(name = "Dev — test powiadomień (tymczasowy)")
public class NotificationTestHookController {

  private final NotificationTestHookService notificationTestHookService;

  @PostMapping("/order-assigned-to-employee")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Utwórz zamówienie i przypisz dostawę do employee@example.com (bez JWT)")
  public NotificationTestHookResponse orderAssignedToEmployee() {
    return notificationTestHookService.createOrderAndAssignToSeedEmployee();
  }
}
