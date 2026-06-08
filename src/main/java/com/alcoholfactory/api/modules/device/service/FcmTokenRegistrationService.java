package com.alcoholfactory.api.modules.device.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.device.domain.FcmDeviceToken;
import com.alcoholfactory.api.modules.device.repository.FcmDeviceTokenRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenRegistrationService {

  private final FcmDeviceTokenRepository fcmDeviceTokenRepository;
  private final UserRepository userRepository;

  @Transactional
  public void register(Long userId, String token, String platform) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
    fcmDeviceTokenRepository
        .findByToken(token)
        .ifPresentOrElse(
            existing -> {
              existing.setUser(user);
              existing.setPlatform(platform.trim());
            },
            () ->
                fcmDeviceTokenRepository.save(
                    FcmDeviceToken.builder()
                        .user(user)
                        .token(token.trim())
                        .platform(platform.trim())
                        .build()));
  }
}
