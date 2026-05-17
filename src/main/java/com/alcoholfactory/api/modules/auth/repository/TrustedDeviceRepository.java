package com.alcoholfactory.api.modules.auth.repository;

import com.alcoholfactory.api.modules.auth.domain.TrustedDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {

    Optional<TrustedDevice> findByUserIdAndDeviceId(Long userId, String deviceId);
}
