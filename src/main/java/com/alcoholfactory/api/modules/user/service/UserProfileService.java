package com.alcoholfactory.api.modules.user.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.user.dto.UserMeResponse;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserMeResponse getFor(long userId) {
        var u = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserMeResponse(
                u.getId(),
                u.getEmail(),
                u.getRole(),
                u.getFirstName(),
                u.getLastName(),
                u.getPhone(),
                u.isCourier(),
                u.isActive(),
                u.getAgeConfirmedAt()
        );
    }
}
