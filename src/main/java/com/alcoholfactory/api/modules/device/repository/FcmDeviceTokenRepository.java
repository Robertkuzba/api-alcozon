package com.alcoholfactory.api.modules.device.repository;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.device.domain.FcmDeviceToken;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FcmDeviceTokenRepository extends JpaRepository<FcmDeviceToken, Long> {

  Optional<FcmDeviceToken> findByToken(String token);

  @Query(
      "select distinct t.token from FcmDeviceToken t join t.user u where u.role in :roles and"
          + " u.active = true")
  List<String> findDistinctTokensByUserRoles(@Param("roles") Collection<UserRole> roles);

  @Query(
      "select distinct t.token from FcmDeviceToken t join t.user u where u.id = :userId and"
          + " u.active = true")
  List<String> findDistinctTokensByUserId(@Param("userId") long userId);
}
