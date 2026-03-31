package main.backend.common.config;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.backend.auth.security.CustomOAuth2UserService;
import main.backend.auth.security.JwtAuthenticationFilter;
import main.backend.auth.security.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Value("${FRONTEND_URL:http://localhost:3000}")
  private String frontendUrl;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(frontendUrl));
    configuration.setAllowedMethods(
      Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    );
    configuration.setAllowedHeaders(
      Arrays.asList("Authorization", "Content-Type", "X-Requested-With")
    );
    configuration.setExposedHeaders(List.of("Authorization"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(auth ->
        auth
          // Static resources
          .requestMatchers(
            "/",
            "/error",
            "/favicon.ico",
            "/*/*.png",
            "/*/*.gif",
            "/*/*.svg",
            "/*/*.jpg",
            "/*/*.html",
            "/*/*.css",
            "/*/*.js"
          )
          .permitAll()
          // OAuth2 + Auth endpoints (public)
          .requestMatchers("/oauth2/**", "/login/**")
          .permitAll()
          .requestMatchers("/api/auth/**")
          .permitAll()
          // Student endpoints
          .requestMatchers("/api/student/**")
          .hasRole("STUDENT")
          .requestMatchers(HttpMethod.GET, "/api/courses/*")
          .hasRole("STUDENT")
          .requestMatchers(HttpMethod.POST, "/api/quizzes/*/submit")
          .hasRole("STUDENT")
          // Teacher endpoints
          .requestMatchers("/api/teacher/**")
          .hasRole("TEACHER")
          .requestMatchers("/api/classes/**")
          .hasRole("TEACHER")
          .requestMatchers(HttpMethod.GET, "/api/quizzes/*")
          .hasAnyRole("STUDENT", "TEACHER")
          .requestMatchers(HttpMethod.POST, "/api/quizzes")
          .hasRole("TEACHER")
          .requestMatchers(HttpMethod.PUT, "/api/quizzes/*")
          .hasRole("TEACHER")
          .requestMatchers(HttpMethod.DELETE, "/api/quizzes/*")
          .hasRole("TEACHER")
          .requestMatchers(HttpMethod.GET, "/api/quizzes/*/submissions")
          .hasRole("TEACHER")
          // Admin endpoints
          .requestMatchers("/api/admin/**")
          .hasRole("ADMIN")
          .requestMatchers("/api/certificates/**")
          .hasRole("ADMIN")
          // Everything else requires authentication
          .anyRequest()
          .authenticated()
      )
      .oauth2Login(oauth2 ->
        oauth2
          .userInfoEndpoint(userInfo ->
            userInfo.userService(customOAuth2UserService)
          )
          .successHandler(oAuth2AuthenticationSuccessHandler)
      )
      .addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
      );

    return http.build();
  }
}
