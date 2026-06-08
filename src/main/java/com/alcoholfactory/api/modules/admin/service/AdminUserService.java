package com.alcoholfactory.api.modules.admin.service;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.admin.dto.PatchUserRequest;
import com.alcoholfactory.api.modules.admin.dto.UserAdminResponse;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<UserAdminResponse> list() {
    return userRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional
  public UserAdminResponse update(Long id, PatchUserRequest req) {
    User u =
        userRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
    u.setRole(req.role());
    u.setActive(req.active());
    u.setCourier(req.courier());
    return toResponse(userRepository.save(u));
  }

  @Transactional
  public UserAdminResponse hire(Long id) {
    User u =
        userRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
    u.setRole(UserRole.EMPLOYEE);
    u.setActive(true);
    return toResponse(userRepository.save(u));
  }

  @Transactional
  public UserAdminResponse terminate(Long id) {
    User u =
        userRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
    u.setActive(false);
    return toResponse(userRepository.save(u));
  }

  private UserAdminResponse toResponse(User u) {
    return new UserAdminResponse(
        u.getId(), u.getEmail(), u.getRole(), u.isActive(), u.isCourier(), u.getAgeConfirmedAt());
  }
}
