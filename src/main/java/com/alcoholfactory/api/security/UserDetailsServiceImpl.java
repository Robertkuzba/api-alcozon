package com.alcoholfactory.api.security;

import com.alcoholfactory.api.modules.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) {
    var user =
        userRepository
            .findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    if (!user.isActive()) {
      throw new UsernameNotFoundException("User disabled");
    }
    return new AppUserDetails(
        user.getId(),
        user.getEmail(),
        user.getPasswordHash(),
        List.of(user.getRole().name()),
        user.isActive());
  }
}
