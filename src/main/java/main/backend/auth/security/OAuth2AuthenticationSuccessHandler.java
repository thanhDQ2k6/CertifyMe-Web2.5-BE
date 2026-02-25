package main.backend.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.backend.auth.entity.Role;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import main.backend.auth.repository.RoleRepository;
import main.backend.auth.repository.UserRepository;
import main.backend.common.util.IdGenerator;
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
  private final RoleRepository roleRepository;

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
      // Get OAuth2 user info from Google
      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

      // Find or create user in database
      User user = findOrCreateUser(oAuth2User);

      if (user == null) {
        handleAuthenticationError(response, "Failed to process user");
        return;
      }

      // Create UserPrincipal and Authentication for JWT
      UserPrincipal userPrincipal = UserPrincipal.create(user);
      Authentication newAuth =
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
          userPrincipal,
          null,
          userPrincipal.getAuthorities()
        );

      // Generate JWT token with claims: sub (userId), email, role (UPPERCASE)
      String token = tokenProvider.generateToken(newAuth);

      // Build redirect URL with token
      // Frontend will extract token and redirect based on role
      String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("token", token)
        .build()
        .toUriString();

      log.info(
        "OAuth2 authentication successful for user: {} ({}), redirecting to: {}",
        user.getEmail(),
        user.getRole().getRoleName(),
        redirectUri
      );

      getRedirectStrategy().sendRedirect(request, response, targetUrl);
    } catch (Exception ex) {
      log.error("OAuth2 authentication handler error", ex);
      handleAuthenticationError(response, "Authentication failed");
    }
  }

  /**
   * Find existing user by email or create new user with default STUDENT role
   * Implements find-or-create pattern as specified in FE-REDIRECT-STRATEGY.md
   */
  private User findOrCreateUser(OAuth2User oAuth2User) {
    String email = oAuth2User.getAttribute("email");
    String googleId = oAuth2User.getAttribute("sub");
    String name = oAuth2User.getAttribute("name");
    String picture = oAuth2User.getAttribute("picture");

    if (email == null || email.isEmpty()) {
      log.error("Email not provided by Google OAuth2");
      return null;
    }

    return userRepository
      .findByEmail(email)
      .map(existingUser ->
        updateExistingUser(existingUser, name, picture, googleId)
      )
      .orElseGet(() -> createNewUser(email, googleId, name, picture));
  }

  /**
   * Update existing user with latest info from Google
   */
  private User updateExistingUser(
    User user,
    String name,
    String picture,
    String googleId
  ) {
    boolean updated = false;

    if (name != null && !name.equals(user.getFullName())) {
      user.setFullName(name);
      updated = true;
    }

    if (picture != null && !picture.equals(user.getAvatarUrl())) {
      user.setAvatarUrl(picture);
      updated = true;
    }

    if (googleId != null && !googleId.equals(user.getGoogleId())) {
      user.setGoogleId(googleId);
      updated = true;
    }

    if (updated) {
      log.info("Updating user info for: {}", user.getEmail());
      return userRepository.save(user);
    }

    return user;
  }

  /**
   * Create new user with default STUDENT role
   * As specified in FE-REDIRECT-STRATEGY.md section 4.1
   */
  private User createNewUser(
    String email,
    String googleId,
    String name,
    String picture
  ) {
    // Get default STUDENT role
    Role studentRole = roleRepository
      .findByRoleName(RoleType.STUDENT)
      .orElseThrow(() ->
        new RuntimeException("STUDENT role not found in database")
      );

    // Build new user
    User newUser = User.builder()
      .userId(IdGenerator.generateUserId()) // Generate UUID
      .email(email)
      .googleId(googleId)
      .fullName(name != null ? name : "User")
      .avatarUrl(picture)
      .role(studentRole)
      .isActive(true)
      .build();

    log.info("Creating new user: {} with role: STUDENT", email);
    return userRepository.save(newUser);
  }

  /**
   * Handle authentication errors by redirecting to frontend with error parameter
   */
  private void handleAuthenticationError(
    HttpServletResponse response,
    String errorMessage
  ) throws IOException {
    String errorRedirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
      .queryParam("error", "authentication_failed")
      .queryParam("message", errorMessage)
      .build()
      .toUriString();

    log.error(
      "Authentication error: {}, redirecting to: {}",
      errorMessage,
      errorRedirectUrl
    );
    getRedirectStrategy().sendRedirect(null, response, errorRedirectUrl);
  }
}
