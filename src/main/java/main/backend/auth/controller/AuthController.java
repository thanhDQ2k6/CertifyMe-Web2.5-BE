package main.backend.auth.controller;

import lombok.RequiredArgsConstructor;
import main.backend.auth.dto.UserResponse;
import main.backend.auth.security.UserPrincipal;
import main.backend.auth.service.AuthService;
import main.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
    @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    UserResponse user = authService.getCurrentUser(userPrincipal);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  @PostMapping("/logout")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<String>> logout() {
    // JWT là stateless, client sẽ xóa token
    return ResponseEntity.ok(
      ApiResponse.success("Logged out successfully", null)
    );
  }

  // Endpoint để frontend kiểm tra role-based access
  @GetMapping("/check-role")
  @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
  public ResponseEntity<ApiResponse<String>> checkRole(
    @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(
        "You are logged in as: " + userPrincipal.getRole(),
        null
      )
    );
  }
}
