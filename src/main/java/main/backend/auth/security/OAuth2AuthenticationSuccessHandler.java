package main.backend.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import main.backend.auth.repository.UserRepository;
import main.backend.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
  extends SimpleUrlAuthenticationSuccessHandler
{

  private final JwtTokenProvider tokenProvider;
  private final UserRepository userRepository;

  @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
  private String redirectUri;

  @Override
  public void onAuthenticationSuccess(
    HttpServletRequest request,
    HttpServletResponse response,
    Authentication authentication
  ) throws IOException, ServletException {
    if (response.isCommitted()) {
      log.debug("Response has already been committed");
      return;
    }

    try {
      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
      String email = oAuth2User.getAttribute("email");

      // User đã được tạo/cập nhật bởi CustomOAuth2UserService.loadUser()
      // Chỉ cần lấy ra để tạo JWT
      User user = userRepository
        .findByEmail(email)
        .orElseThrow(() ->
          new ResourceNotFoundException("User not found after OAuth2 login")
        );

      UserPrincipal userPrincipal = UserPrincipal.create(user);
      Authentication newAuth =
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
          userPrincipal,
          null,
          userPrincipal.getAuthorities()
        );

      String token = tokenProvider.generateToken(newAuth);
      RoleType roleType = user.getRole().getRoleName();
      String redirectPath = getRedirectPathByRole(roleType);

      String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("token", token)
        .queryParam("role", roleType.name())
        .queryParam("redirect", redirectPath)
        .build()
        .toUriString();

      log.info(
        "OAuth2 login successful: {} ({}), redirecting to {}",
        user.getEmail(),
        roleType.name(),
        redirectPath
      );

      getRedirectStrategy().sendRedirect(request, response, targetUrl);
    } catch (Exception ex) {
      log.error("OAuth2 authentication handler error", ex);
      handleAuthenticationError(response);
    }
  }

  /**
   * Xác định đường dẫn redirect dựa trên role của user
   * - STUDENT → /student/dashboard
   * - TEACHER → /teacher/dashboard
   * - ADMIN   → /admin/dashboard
   */
  private String getRedirectPathByRole(RoleType roleType) {
    return switch (roleType) {
      case STUDENT -> "/student/dashboard";
      case TEACHER -> "/teacher/dashboard";
      case ADMIN -> "/admin/dashboard";
    };
  }

  private void handleAuthenticationError(HttpServletResponse response)
    throws IOException {
    String errorRedirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
      .queryParam("error", "authentication_failed")
      .build()
      .toUriString();
    getRedirectStrategy().sendRedirect(null, response, errorRedirectUrl);
  }
}
