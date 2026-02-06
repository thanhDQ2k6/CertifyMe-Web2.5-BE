package main.backend.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
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

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");

    User user = userRepository
      .findByEmail(email)
      .orElseThrow(() -> new RuntimeException("User not found"));

    UserPrincipal userPrincipal = UserPrincipal.create(user);
    Authentication newAuth =
      new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
        userPrincipal,
        null,
        userPrincipal.getAuthorities()
      );

    String token = tokenProvider.generateToken(newAuth);

    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
      .queryParam("token", token)
      .build()
      .toUriString();

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}
