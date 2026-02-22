package main.backend.auth.service;

import main.backend.auth.dto.UserResponse;
import main.backend.auth.security.UserPrincipal;

public interface AuthService {
  UserResponse getCurrentUser(UserPrincipal userPrincipal);
}
