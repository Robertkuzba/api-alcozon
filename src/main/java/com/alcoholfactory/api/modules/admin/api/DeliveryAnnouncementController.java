package com.alcoholfactory.api.modules.admin.api;

import com.alcoholfactory.api.modules.admin.dto.AnnouncementRequest;
import com.alcoholfactory.api.modules.admin.dto.AnnouncementResponse;
import com.alcoholfactory.api.modules.admin.service.DeliveryAnnouncementService;
import com.alcoholfactory.api.security.AppUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/delivery-announcements")
@RequiredArgsConstructor
@Tag(name = "Delivery announcements")
public class DeliveryAnnouncementController {

  private final DeliveryAnnouncementService deliveryAnnouncementService;

  @GetMapping
  @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
  public List<AnnouncementResponse> list() {
    return deliveryAnnouncementService.list();
  }

  @PostMapping
  @PreAuthorize("hasRole('MANAGER')")
  @ResponseStatus(HttpStatus.CREATED)
  public AnnouncementResponse create(
      @AuthenticationPrincipal AppUserDetails user, @Valid @RequestBody AnnouncementRequest req) {
    return deliveryAnnouncementService.create(user.getId(), req);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  public AnnouncementResponse update(
      @PathVariable Long id, @Valid @RequestBody AnnouncementRequest req) {
    return deliveryAnnouncementService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    deliveryAnnouncementService.delete(id);
  }
}
