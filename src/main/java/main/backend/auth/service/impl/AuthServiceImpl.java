package main.backend.auth.service.impl;

import lombok.RequiredArgsConstructor;
import main.backend.auth.dto.UserResponse;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.auth.security.UserPrincipal;
import main.backend.auth.service.AuthService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;

  @Override
  public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
    User user = userRepository
      .findById(userPrincipal.getUserId())
      .orElseThrow(() -> new RuntimeException("User not found"));

    return UserResponse.builder()
      .userId(user.getUserId())
      .email(user.getEmail())
      .fullName(user.getFullName())
      .avatarUrl(user.getAvatarUrl())
      .role(user.getRole().getRoleName())
      .isActive(user.getIsActive())
      .build();
  }
}
