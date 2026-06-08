package com.alcoholfactory.api.modules.user.repository;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  List<User> findByRoleAndActiveTrue(UserRole role);
}
