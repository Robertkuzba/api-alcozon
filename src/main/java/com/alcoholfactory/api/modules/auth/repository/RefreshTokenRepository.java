package com.alcoholfactory.api.modules.auth.repository;

import com.alcoholfactory.api.modules.auth.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
}
