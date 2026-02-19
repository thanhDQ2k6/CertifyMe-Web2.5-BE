# 🔐 KẾ HOẠCH TRIỂN KHAI ĐĂNG NHẬP & BẢO MẬT

> **Dự án**: LMS Backend - Google OAuth2 Authentication
> **Ngày bắt đầu**: 2026-02-05
> **Phương pháp**: Bottom-up Implementation (Từ foundation → Feature)

---

## 📋 TỔNG QUAN CÔNG VIỆC

**Mục tiêu**: Xây dựng hệ thống đăng nhập Google OAuth2 với phân quyền role (STUDENT, TEACHER, ADMIN)

**Thời gian dự kiến**: 3-4 ngày

**Checklist tổng thể**:

- [ ] Phase 1: Nền tảng (Foundation) - 1 ngày
- [ ] Phase 2: OAuth2 Core - 1 ngày
- [ ] Phase 3: JWT & Security - 1 ngày
- [ ] Phase 4: Testing & Polish - 0.5 ngày

---

## 🏗️ PHASE 1: NỀN TẢNG (FOUNDATION)

### 1.1. Cập nhật Dependencies ✅

**File**: `pom.xml`

**Thêm dependencies**:

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- OAuth2 Resource Server (JWT) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Checklist**:

- [ ] Thêm dependencies vào pom.xml
- [ ] Run `mvn clean install` để download
- [ ] Verify không có conflict

---

### 1.2. Tạo cấu trúc thư mục Module

**Checklist**:

- [ ] Tạo package `common/` và các sub-packages
- [ ] Tạo package `auth/` và các sub-packages

**Cấu trúc cần tạo**:

```
src/main/java/main/backend/
├── common/
│   ├── config/          ✓ Tạo folder
│   ├── exception/       ✓ Tạo folder
│   ├── dto/            ✓ Tạo folder
│   └── util/           ✓ Tạo folder
│
└── auth/
    ├── controller/      ✓ Tạo folder
    ├── service/
    │   └── impl/       ✓ Tạo folder
    ├── repository/      ✓ Tạo folder
    ├── entity/          ✓ Tạo folder
    ├── dto/            ✓ Tạo folder
    ├── enums/          ✓ Tạo folder
    └── security/        ✓ Tạo folder
```

---

### 1.3. Tạo Common DTOs

#### ✅ File 1: `ApiResponse.java`

**Path**: `src/main/java/main/backend/common/dto/ApiResponse.java`

```java
package main.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }
}
```

**Checklist**:

- [ ] Tạo file ApiResponse.java
- [ ] Test compile: `mvn compile`

---

### 1.4. Tạo Enum & Entities

#### ✅ File 2: `RoleType.java`

**Path**: `src/main/java/main/backend/auth/enums/RoleType.java`

```java
package main.backend.auth.enums;

public enum RoleType {
    STUDENT,
    TEACHER,
    ADMIN
}
```

**Checklist**:

- [ ] Tạo RoleType enum
- [ ] Verify tên enum khớp với DB (STUDENT, TEACHER, ADMIN)

---

#### ✅ File 3: `Role.java` (Entity)

**Path**: `src/main/java/main/backend/auth/entity/Role.java`

```java
package main.backend.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.backend.auth.enums.RoleType;

import java.time.Instant;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, unique = true, length = 20)
    private RoleType roleName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
```

**Checklist**:

- [ ] Tạo Role entity
- [ ] Verify mapping với bảng `roles`
- [ ] Check @Enumerated sử dụng STRING

---

#### ✅ File 4: `User.java` (Entity)

**Path**: `src/main/java/main/backend/auth/entity/User.java`

```java
package main.backend.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

**Checklist**:

- [ ] Tạo User entity
- [ ] Verify relationship với Role (ManyToOne)
- [ ] Check các constraint (unique, nullable)

---

### 1.5. Tạo Repositories

#### ✅ File 5: `RoleRepository.java`

**Path**: `src/main/java/main/backend/auth/repository/RoleRepository.java`

```java
package main.backend.auth.repository;

import main.backend.auth.entity.Role;
import main.backend.auth.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleType roleName);
}
```

---

#### ✅ File 6: `UserRepository.java`

**Path**: `src/main/java/main/backend/auth/repository/UserRepository.java`

```java
package main.backend.auth.repository;

import main.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    boolean existsByEmail(String email);
}
```

**Checklist**:

- [ ] Tạo RoleRepository
- [ ] Tạo UserRepository
- [ ] Verify các query methods

---

### 1.6. Database Migration - Seed Roles

#### ✅ File 7: `V2__Seed_roles.sql`

**Path**: `src/main/resources/db/migration/V2__Seed_roles.sql`

```sql
-- Seed initial roles
INSERT INTO roles (role_name, created_at) VALUES
    ('STUDENT', NOW()),
    ('TEACHER', NOW()),
    ('ADMIN', NOW());
```

**Checklist**:

- [ ] Tạo migration file V2
- [ ] Run `mvn flyway:migrate` hoặc khởi động app
- [ ] Verify data trong DB: `SELECT * FROM roles;`

---

## 🔐 PHASE 2: OAUTH2 CORE

### 2.1. Cấu hình Google OAuth2 Credentials

#### ✅ Bước 1: Lấy credentials từ Google Cloud Console

**Hướng dẫn**:

1. Truy cập: https://console.cloud.google.com/
2. Tạo project mới hoặc chọn project
3. APIs & Services → Credentials
4. Create Credentials → OAuth 2.0 Client ID
5. Application type: Web application
6. Authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google`
   - `http://localhost:8080/api/auth/oauth2/callback/google`

**Checklist**:

- [ ] Đã có Google Client ID
- [ ] Đã có Google Client Secret
- [ ] Đã config redirect URIs

---

#### ✅ File 8: Cập nhật `application.properties`

**Path**: `src/main/resources/application.properties`

**Thêm vào cuối file**:

```properties
# ==========================================
# Google OAuth2 Configuration
# ==========================================
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:your-client-id}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:your-client-secret}
spring.security.oauth2.client.registration.google.scope=email,profile
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# OAuth2 Provider
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=sub

# JWT Configuration
jwt.secret=${JWT_SECRET:your-secret-key-at-least-256-bits-long-for-hs256-algorithm}
jwt.expiration=86400000
# 24 hours in milliseconds
```

**Checklist**:

- [ ] Thêm OAuth2 config
- [ ] Thêm JWT config
- [ ] Tạo file `.env` hoặc set environment variables
- [ ] Thêm `.env` vào `.gitignore`

---

### 2.2. Tạo DTOs cho Auth

#### ✅ File 9: `GoogleUserInfo.java`

**Path**: `src/main/java/main/backend/auth/dto/GoogleUserInfo.java`

```java
package main.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {
    private String sub;          // Google ID
    private String email;
    private String name;
    private String picture;
    private Boolean emailVerified;
}
```

---

#### ✅ File 10: `UserResponse.java`

**Path**: `src/main/java/main/backend/auth/dto/UserResponse.java`

```java
package main.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.backend.auth.enums.RoleType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String email;
    private String fullName;
    private String avatarUrl;
    private RoleType role;
    private Boolean isActive;
}
```

---

#### ✅ File 11: `AuthResponse.java`

**Path**: `src/main/java/main/backend/auth/dto/AuthResponse.java`

```java
package main.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponse user;
}
```

**Checklist**:

- [ ] Tạo GoogleUserInfo DTO
- [ ] Tạo UserResponse DTO
- [ ] Tạo AuthResponse DTO

---

## 🔑 PHASE 3: JWT & SECURITY

### 3.1. JWT Token Provider

#### ✅ File 12: `JwtTokenProvider.java`

**Path**: `src/main/java/main/backend/auth/security/JwtTokenProvider.java`

```java
package main.backend.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getUserId()))
                .claim("email", userPrincipal.getEmail())
                .claim("role", userPrincipal.getRole())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}
```

**Checklist**:

- [ ] Tạo JwtTokenProvider
- [ ] Verify secret key length (>=256 bits)
- [ ] Test token generation

---

### 3.2. Custom UserDetails

#### ✅ File 13: `UserPrincipal.java`

**Path**: `src/main/java/main/backend/auth/security/UserPrincipal.java`

```java
package main.backend.auth.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private Long userId;
    private String email;
    private String fullName;
    private RoleType role;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getRoleName(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().name())
                )
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return null; // OAuth2 không cần password
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

**Checklist**:

- [ ] Tạo UserPrincipal
- [ ] Implement UserDetails interface
- [ ] Verify authorities format: ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN

---

### 3.3. JWT Authentication Filter

#### ✅ File 14: `JwtAuthenticationFilter.java`

**Path**: `src/main/java/main/backend/auth/security/JwtAuthenticationFilter.java`

```java
package main.backend.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Long userId = tokenProvider.getUserIdFromToken(jwt);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                UserPrincipal userPrincipal = UserPrincipal.create(user);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                userPrincipal.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**Checklist**:

- [ ] Tạo JwtAuthenticationFilter
- [ ] Verify Bearer token extraction
- [ ] Test với Postman

---

### 3.4. OAuth2 User Service

#### ✅ File 15: `CustomOAuth2UserService.java`

**Path**: `src/main/java/main/backend/auth/security/CustomOAuth2UserService.java`

```java
package main.backend.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.backend.auth.entity.Role;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import main.backend.auth.repository.RoleRepository;
import main.backend.auth.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Lấy role từ OAuth2 state (sẽ implement trong handler)
        String roleParam = userRequest.getAdditionalParameters().get("role").toString();
        RoleType roleType = RoleType.valueOf(roleParam != null ? roleParam : "STUDENT");

        // Xử lý user
        return processOAuth2User(oAuth2User, roleType);
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User, RoleType roleType) {
        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, name, picture, roleType))
                .orElseGet(() -> createNewUser(email, googleId, name, picture, roleType));

        return oAuth2User;
    }

    private User updateExistingUser(User user, String name, String picture, RoleType roleType) {
        user.setFullName(name);
        user.setAvatarUrl(picture);

        // Cập nhật role nếu user chọn role mới
        Role role = roleRepository.findByRoleName(roleType)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));
        user.setRole(role);

        return userRepository.save(user);
    }

    private User createNewUser(String email, String googleId, String name, String picture, RoleType roleType) {
        Role role = roleRepository.findByRoleName(roleType)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));

        User user = User.builder()
                .email(email)
                .googleId(googleId)
                .fullName(name)
                .avatarUrl(picture)
                .role(role)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }
}
```

**Checklist**:

- [ ] Tạo CustomOAuth2UserService
- [ ] Verify logic tạo/cập nhật user
- [ ] Check role assignment

---

### 3.5. OAuth2 Success Handler

#### ✅ File 16: `OAuth2AuthenticationSuccessHandler.java`

**Path**: `src/main/java/main/backend/auth/security/OAuth2AuthenticationSuccessHandler.java`

```java
package main.backend.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (response.isCommitted()) {
            log.debug("Response has already been committed");
            return;
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        String token = tokenProvider.generateToken(newAuth);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

**Checklist**:

- [ ] Tạo OAuth2AuthenticationSuccessHandler
- [ ] Config redirect URI trong application.properties
- [ ] Test redirect flow

---

### 3.6. Security Configuration

#### ✅ File 17: `SecurityConfig.java`

**Path**: `src/main/java/main/backend/common/config/SecurityConfig.java`

```java
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
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/favicon.ico", "/**/*.png", "/**/*.gif",
                                "/**/*.svg", "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js")
                        .permitAll()
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

**Checklist**:

- [ ] Tạo SecurityConfig
- [ ] Verify public endpoints
- [ ] Verify OAuth2 configuration
- [ ] Test security filter chain

---

### 3.7. Exception Handling

#### ✅ File 18: `GlobalExceptionHandler.java`

**Path**: `src/main/java/main/backend/common/exception/GlobalExceptionHandler.java`

```java
package main.backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import main.backend.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid credentials"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}
```

**Checklist**:

- [ ] Tạo GlobalExceptionHandler
- [ ] Test error responses

---

## 🎯 PHASE 4: CONTROLLERS & TESTING

### 4.1. Auth Service

#### ✅ File 19: `AuthService.java` (Interface)

**Path**: `src/main/java/main/backend/auth/service/AuthService.java`

```java
package main.backend.auth.service;

import main.backend.auth.dto.AuthResponse;
import main.backend.auth.dto.UserResponse;
import main.backend.auth.security.UserPrincipal;

public interface AuthService {
    UserResponse getCurrentUser(UserPrincipal userPrincipal);
}
```

---

#### ✅ File 20: `AuthServiceImpl.java`

**Path**: `src/main/java/main/backend/auth/service/impl/AuthServiceImpl.java`

```java
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
        User user = userRepository.findById(userPrincipal.getUserId())
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
```

**Checklist**:

- [ ] Tạo AuthService interface
- [ ] Tạo AuthServiceImpl
- [ ] Implement getCurrentUser method

---

### 4.2. Auth Controller

#### ✅ File 21: `AuthController.java`

**Path**: `src/main/java/main/backend/auth/controller/AuthController.java`

```java
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
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse user = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> logout() {
        // JWT là stateless, client sẽ xóa token
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    // Endpoint để frontend kiểm tra role-based access
    @GetMapping("/check-role")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> checkRole(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(
                ApiResponse.success("You are logged in as: " + userPrincipal.getRole()));
    }
}
```

**Checklist**:

- [ ] Tạo AuthController
- [ ] Implement /me endpoint
- [ ] Implement /logout endpoint
- [ ] Test với Postman

---

### 4.3. Testing Checklist

#### Manual Testing với Postman

**Checklist**:

1. **Test OAuth2 Login Flow**:
   - [ ] Mở browser: `http://localhost:8080/oauth2/authorization/google?role=TEACHER`
   - [ ] Verify redirect đến Google login
   - [ ] Đăng nhập thành công
   - [ ] Verify redirect về frontend với token
   - [ ] Copy JWT token

2. **Test /api/auth/me**:
   - [ ] Method: GET
   - [ ] URL: `http://localhost:8080/api/auth/me`
   - [ ] Headers: `Authorization: Bearer <JWT_TOKEN>`
   - [ ] Verify response có thông tin user đúng

3. **Test Role-based Access**:
   - [ ] Method: GET
   - [ ] URL: `http://localhost:8080/api/auth/check-role`
   - [ ] Headers: `Authorization: Bearer <JWT_TOKEN>`
   - [ ] Verify response trả về đúng role

4. **Test Unauthorized Access**:
   - [ ] Call /api/auth/me KHÔNG có token
   - [ ] Verify nhận response 401 Unauthorized

5. **Test với các roles khác nhau**:
   - [ ] Login với role=STUDENT
   - [ ] Login với role=TEACHER
   - [ ] Login với role=ADMIN
   - [ ] Verify mỗi user có đúng role trong JWT

---

### 4.4. Environment Variables Setup

#### ✅ File 22: `.env.example`

**Path**: `c:\Users\Admin\Documents\GitHub\backend\.env.example`

```properties
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# JWT
JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long-for-hs256

# Frontend
FRONTEND_URL=http://localhost:3000
```

**Checklist**:

- [ ] Tạo file .env.example
- [ ] Copy thành .env và điền values thật
- [ ] Add .env vào .gitignore
- [ ] Verify environment variables được load

---

## ✅ FINAL CHECKLIST

### Database

- [ ] V1 migration đã chạy (bảng users, roles)
- [ ] V2 migration đã chạy (seed roles)
- [ ] Verify data trong DB bằng SQL:
  ```sql
  SELECT * FROM roles;
  SELECT * FROM users;
  ```

### Code Structure

- [ ] Tất cả packages đã tạo đúng cấu trúc
- [ ] Tất cả files đã tạo (21 files)
- [ ] Code compile không lỗi: `mvn clean compile`

### Configuration

- [ ] Google OAuth2 credentials đã setup
- [ ] application.properties đã cập nhật đầy đủ
- [ ] Environment variables đã set
- [ ] JWT secret key đủ dài (>=256 bits)

### Security

- [ ] SecurityConfig đã enable
- [ ] JWT filter hoạt động
- [ ] OAuth2 login flow hoàn chỉnh
- [ ] Role-based authorization hoạt động

### Testing

- [ ] App khởi động thành công
- [ ] OAuth2 login thành công
- [ ] JWT token được tạo
- [ ] /api/auth/me trả về user info
- [ ] Role được assign đúng
- [ ] Unauthorized access bị chặn

---

## 🚀 LỆNH CHẠY THỬ

```bash
# 1. Clean & compile
mvn clean compile

# 2. Run application
mvn spring-boot:run

# 3. Test OAuth2 (mở browser)
http://localhost:8080/oauth2/authorization/google

# 4. Kiểm tra JWT trong response redirect
# Copy token và test API với Postman

# 5. Test API với JWT
curl -H "Authorization: Bearer <YOUR_TOKEN>" \
     http://localhost:8080/api/auth/me
```

---

## 📚 TÀI LIỆU THAM KHẢO

- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Google OAuth2 Setup](https://developers.google.com/identity/protocols/oauth2)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)

---

**Ghi chú**:

- Đánh dấu ✅ sau khi hoàn thành mỗi item
- Test từng phase trước khi chuyển sang phase tiếp theo
- Commit code sau mỗi phase hoàn thành

---

_Cập nhật: 2026-02-05_
