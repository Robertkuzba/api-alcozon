package com.alcoholfactory.api.modules.auth.repository;

import com.alcoholfactory.api.modules.auth.domain.DeviceVerificationChallenge;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DeviceVerificationChallengeRepository
    extends JpaRepository<DeviceVerificationChallenge, UUID> {

  Optional<DeviceVerificationChallenge> findByIdAndConsumedAtIsNull(UUID id);

  @Modifying
  @Transactional
  @Query(
      """
      UPDATE DeviceVerificationChallenge c SET c.consumedAt = :now
      WHERE c.user.id = :userId AND c.deviceId = :deviceId AND c.consumedAt IS NULL
      """)
  void invalidatePendingForDevice(
      @Param("userId") Long userId, @Param("deviceId") String deviceId, @Param("now") Instant now);
}
