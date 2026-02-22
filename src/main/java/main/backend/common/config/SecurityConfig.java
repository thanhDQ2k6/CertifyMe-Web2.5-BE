package main.backend.common.config;

import lombok.RequiredArgsConstructor;
import main.backend.auth.security.CustomOAuth2UserService;
import main.backend.auth.security.JwtAuthenticationFilter;
import main.backend.auth.security.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configure(http))
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(
            "/",
            "/error",
            "/favicon.ico",
            "/**/*.png",
            "/**/*.gif",
            "/**/*.svg",
            "/**/*.jpg",
            "/**/*.html",
            "/**/*.css",
            "/**/*.js"
          )
          .permitAll()
          .requestMatchers("/api/auth/**", "/oauth2/**", "/login/**")
          .permitAll()
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
