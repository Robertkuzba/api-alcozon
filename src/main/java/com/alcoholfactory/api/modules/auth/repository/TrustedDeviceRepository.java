package com.alcoholfactory.api.modules.auth.repository;

import com.alcoholfactory.api.modules.auth.domain.TrustedDevice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {

  Optional<TrustedDevice> findByUserIdAndDeviceId(Long userId, String deviceId);
}
