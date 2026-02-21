# 🔐 HƯỚNG DẪN TÍCH HỢP API ĐĂNG NHẬP - TỪ BACKEND TEAM

> **Từ**: Backend Development Team  
> **Đến**: Frontend Development Team  
> **Ngày cập nhật**: 2026-02-21  
> **Version**: 2.0 (Updated to match actual implementation)

> **⚠️ QUAN TRỌNG**: Document này đã được cập nhật để khớp với implementation thực tế của Backend.  
> **📦 API Response Format**: Xem [API-RESPONSE-FORMAT.md](./API-RESPONSE-FORMAT.md) để biết chi tiết về format response và error handling.

---

## 📋 MỤC LỤC

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Chi tiết API endpoints](#2-chi-tiết-api-endpoints)
3. [Luồng xác thực Google OAuth2](#3-luồng-xác-thực-google-oauth2)
4. [JWT Token Management](#4-jwt-token-management)
5. [Code examples cho Vue 3](#5-code-examples-cho-vue-3)
6. [Error handling](#6-error-handling)
7. [Testing guide](#7-testing-guide)
8. [Security best practices](#8-security-best-practices)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. TỔNG QUAN KIẾN TRÚC

### 1.1. Authentication Flow Overview (OAuth2 Redirect Flow)

```
┌─────────────┐                                   ┌─────────────┐
│             │  1. Redirect to /oauth2/          │             │
│  Frontend   │     authorization/google          │   Google    │
│  (Vue 3)    │──────────────────────────────────▶│   OAuth2    │
│             │                                   │             │
│             │  2. User logs in with Google      │             │
│             │◀──────────────────────────────────│             │
└─────────────┘  3. Redirect with auth code       └─────────────┘
       │
       │ 4. Backend receives auth code
       ▼
┌─────────────────────────────────────────────────────────────┐
│                      Backend API                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 1. Exchange code for Google tokens                   │   │
│  │ 2. Get user info from Google                         │   │
│  │ 3. Find or Create user trong database                │   │
│  │ 4. Generate JWT token (HS256)                        │   │
│  │ 5. Redirect to FE với token trong URL                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
       │
       │ 5. GET /oauth2/redirect?token=xxx
       │
       ▼
┌──────────────────────────────────────────────────────────────┐
│  Frontend extracts token from URL:                          │
│  • Parse token from query params                             │
│  • localStorage.setItem('authToken', token)                  │
│  • Decode JWT to get user info                               │
│  • Set axios default header: Authorization: Bearer <token>   │
│  • Redirect to dashboard based on role                       │
└──────────────────────────────────────────────────────────────┘
       │
       │ 6. Subsequent API calls
       │    Header: Authorization: Bearer <token>
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│  Backend validates JWT:                                     │
│  • Verify signature                                         │
│  • Check expiration                                         │
│  • Extract user info from token                             │
│  • Allow/Deny request                                       │
└─────────────────────────────────────────────────────────────┘
```

### 1.2. Technology Stack

**Backend:**

- Spring Boot 4.0.2
- Spring Security 7.0.3
- JWT (jjwt 0.12.x)
- MySQL 8.0
- Google OAuth2 Client Library

**Frontend (Vue 3):**

- Vue 3 + Composition API
- Axios (HTTP client)
- Google Sign-In Button Library
- Vue Router 4

---

## 2. CHI TIẾT API ENDPOINTS

### 2.1. Authentication Endpoints

#### 📌 OAuth2 Login Flow - Đăng nhập với Google

**⚠️ LƯU Ý:** Backend hiện sử dụng **OAuth2 redirect flow**, KHÔNG phải REST API endpoint.

**Bước 1: Frontend redirect user đến Google login**

**URL:** `http://localhost:8080/oauth2/authorization/google`

**Method:** Browser redirect (window.location.href hoặc <a> tag)

```javascript
// Redirect to Google login
window.location.href = "http://localhost:8080/oauth2/authorization/google";
```

**Bước 2: User login với Google**

- User chọn tài khoản Google
- Google xác thực user
- Google redirect về backend với authorization code

**Bước 3: Backend xử lý và redirect về frontend**

**Backend sẽ redirect đến:**

```
http://localhost:3000/oauth2/redirect?token=<JWT_TOKEN>
```

**Frontend cần:**

1. Tạo route handler cho `/oauth2/redirect`
2. Extract token từ URL query parameter
3. Lưu token vào localStorage
4. Redirect đến dashboard

**Token Format (JWT):**

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJlbWFpbCI6ImFubnZAZnB0LmVkdS52biIsInJvbGUiOiJTVFVERU5UIiwiaWF0IjoxNzA5MTIzNDU2LCJleHAiOjE3MDkyMDk4NTZ9.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk
```

**JWT Payload:**

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000", // User ID
  "email": "annv@fpt.edu.vn",
  "role": "STUDENT", // UPPERCASE: STUDENT, TEACHER, ADMIN
  "iat": 1709123456, // Issued at
  "exp": 1709209856 // Expires at (24h)
}
```

**Configuration:**

Backend redirect URI có thể config qua environment variable:

```properties
# application.properties
app.oauth2.redirect-uri=${FRONTEND_URL:http://localhost:3000}/oauth2/redirect
```

---

#### 📌 ~~POST `/api/auth/google`~~ (CHƯA TRIỂN KHAI)

**⚠️ ENDPOINT NÀY CHƯA ĐƯỢC TRIỂN KHAI**

Backend team đang xem xét thêm REST API endpoint để hỗ trợ frontend dễ dàng hơn:

```http
POST /api/auth/google
Content-Type: application/json

{
  "credential": "<GOOGLE_ID_TOKEN>"
}
```

**Nếu cần endpoint này, vui lòng liên hệ Backend team.**

---

#### 📌 GET `/api/auth/me` - Lấy thông tin user hiện tại

**Mục đích:** Lấy thông tin của user đang đăng nhập (từ JWT token).

**HTTP Method:** `GET`

**URL:** `http://localhost:8080/api/auth/me`

**Headers:**

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request Body:** Không có

**Success Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "Nguyễn Văn An",
    "email": "annv@fpt.edu.vn",
    "role": "STUDENT",
    "avatarUrl": "https://lh3.googleusercontent.com/a/ACg8ocKXYZ...",
    "isActive": true
  },
  "message": null,
  "error": null
}
```

**Response Schema:**

```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
  error: string | null;
}

interface UserResponse {
  userId: string; // UUID, VD: "550e8400-e29b-41d4-a716-446655440000"
  fullName: string; // Tên đầy đủ
  email: string; // Email từ Google
  role: "STUDENT" | "TEACHER" | "ADMIN"; // UPPERCASE
  avatarUrl: string; // URL ảnh đại diện từ Google
  isActive: boolean; // Tài khoản còn active không
}
```

**Error Responses:**

```json
// 401 Unauthorized - Token missing hoặc invalid
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Invalid credentials"
}

// 403 Forbidden - Không có quyền
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Access denied"
}

// 500 Internal Server Error
{
  "success": false,
  "data": null,
  "message": null,
  "error": "An unexpected error occurred"
}
```

**Curl Example:**

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

#### 📌 POST `/api/auth/logout` - Đăng xuất

**Mục đích:** Đăng xuất user (FE xóa token, BE không làm gì vì JWT là stateless).

**HTTP Method:** `POST`

**URL:** `http://localhost:8080/api/auth/logout`

**Headers:**

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request Body:** Không có

**Success Response (200 OK):**

```json
{
  "success": true,
  "data": null,
  "message": "Logged out successfully",
  "error": null
}
```

**Lưu ý:**

- JWT là stateless, BE không track sessions
- FE cần xóa token khỏi localStorage sau khi nhận response
- Token cũ vẫn valid cho đến khi hết hạn (24h)

---

#### 📌 GET `/api/auth/check-role` - Kiểm tra role

**Mục đích:** Kiểm tra user đã login và lấy role.

**HTTP Method:** `GET`

**URL:** `http://localhost:8080/api/auth/check-role`

**Headers:**

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**

```json
{
  "success": true,
  "data": null,
  "message": "You are logged in as: STUDENT",
  "error": null
}
```

---

### 2.2. Protected Endpoints

Tất cả endpoints sau yêu cầu JWT token trong header `Authorization`:

```http
Authorization: Bearer <token>
```

Backend sẽ:

1. Extract token từ header
2. Verify signature với secret key
3. Check expiration time
4. Extract user info (id, role) từ token payload
5. Check authorization (role-based)
6. Allow/Deny request

**Ví dụ token payload:**

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000", // User ID (UUID)
  "email": "annv@fpt.edu.vn",
  "role": "STUDENT", // UPPERCASE: STUDENT, TEACHER, ADMIN
  "iat": 1709123456, // Issued at (Unix timestamp)
  "exp": 1709209856 // Expiration (Unix timestamp)
}
```

---

## 3. LUỒNG XÁC THỰC GOOGLE OAUTH2

### 3.1. Bước 1: Frontend - Redirect đến Google Login

**Backend Google OAuth2 Configuration:**

Backend đã cấu hình sẵn Google OAuth2 Client.

**Frontend chỉ cần:**

```vue
<script setup>
const handleGoogleLogin = () => {
  // Redirect to backend OAuth2 endpoint
  window.location.href = "http://localhost:8080/oauth2/authorization/google";
};
</script>

<template>
  <div class="login-container">
    <h1>Đăng nhập LMS</h1>

    <button @click="handleGoogleLogin" class="google-btn">
      <img src="/google-icon.svg" alt="Google" />
      Đăng nhập với Google
    </button>
  </div>
</template>

<style scoped>
.google-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  background: white;
  border: 1px solid #dadce0;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.google-btn:hover {
  background: #f8f9fa;
}

.google-btn img {
  width: 18px;
  height: 18px;
}
</style>
```

**Hoặc dùng simple link:**

```vue
<template>
  <a
    href="http://localhost:8080/oauth2/authorization/google"
    class="google-btn"
  >
    <img src="/google-icon.svg" alt="Google" />
    Đăng nhập với Google
  </a>
</template>
```

---

### 3.2. Bước 2: Backend - Xử lý OAuth2 Flow

**Backend tự động xử lý:**

1. **Redirect user đến Google** (`/oauth2/authorization/google`)
2. **Google xác thực user** và trả về authorization code
3. **Backend exchange code** để lấy access token từ Google
4. **Backend lấy user info** từ Google API
5. **Backend tìm hoặc tạo user** trong database
6. **Backend generate JWT token**
7. **Backend redirect về frontend** với token trong URL

**Backend code (đã triển khai):**

```java
// OAuth2AuthenticationSuccessHandler.java
@Override
public void onAuthenticationSuccess(
    HttpServletRequest request,
    HttpServletResponse response,
    Authentication authentication
) throws IOException, ServletException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");

    // Find user by email
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    UserPrincipal userPrincipal = UserPrincipal.create(user);

    // Generate JWT token
    String token = tokenProvider.generateToken(
        new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        )
    );

    // Redirect to frontend with token
    String targetUrl = UriComponentsBuilder
        .fromUriString(redirectUri)  // http://localhost:3000/oauth2/redirect
        .queryParam("token", token)
        .build()
        .toUriString();

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
}
```

---

### 3.3. Bước 3: Frontend - Handle OAuth2 Redirect

**Tạo route handler cho `/oauth2/redirect`:**

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import OAuth2RedirectHandler from "@/views/auth/OAuth2RedirectHandler.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/oauth2/redirect",
      name: "OAuth2Redirect",
      component: OAuth2RedirectHandler,
    },
    // ... other routes
  ],
});

export default router;
```

**Tạo OAuth2RedirectHandler.vue:**

```vue
<!-- src/views/auth/OAuth2RedirectHandler.vue -->
<script setup>
import { onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

onMounted(() => {
  // Extract token from URL
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");

  if (!token) {
    alert("Đăng nhập thất bại: Không nhận được token");
    router.push("/auth/login");
    return;
  }

  try {
    // Save token to localStorage
    localStorage.setItem("authToken", token);

    // Decode JWT to get user info
    const payload = JSON.parse(atob(token.split(".")[1]));

    console.log("JWT Payload:", payload);
    // payload.sub = userId
    // payload.email = email
    // payload.role = "STUDENT" | "TEACHER" | "ADMIN"

    // Set axios default header
    import("@/lib/apiFetcher/axiosInstance").then(({ default: axios }) => {
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    });

    // Redirect based on role
    const role = payload.role;
    if (role === "STUDENT") {
      router.push("/student/dashboard");
    } else if (role === "TEACHER") {
      router.push("/teacher/dashboard");
    } else if (role === "ADMIN") {
      router.push("/admin/dashboard");
    } else {
      router.push("/");
    }
  } catch (error) {
    console.error("Failed to process token:", error);
    alert("Đăng nhập thất bại: Token không hợp lệ");
    router.push("/auth/login");
  }
});
</script>

<template>
  <div class="redirect-handler">
    <div class="loading">
      <div class="spinner"></div>
      <p>Đang xử lý đăng nhập...</p>
    </div>
  </div>
</template>

<style scoped>
.redirect-handler {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading {
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 16px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
```

**JWT Token Structure:**

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",  // User ID (UUID)
  "email": "annv@fpt.edu.vn",
  "role": "STUDENT",  // UPPERCASE: STUDENT, TEACHER, ADMIN
  "iat": 1709123456,  // Issued at (Unix timestamp)
  "exp": 1709209856   // Expires at (24 hours later)
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  SECRET_KEY
)
```

---

## 4. JWT TOKEN MANAGEMENT

### 4.1. Frontend - Store Token

**Lưu token vào localStorage:**

```javascript
// Sau khi login thành công
const { token, user } = response.data;

// Lưu token
localStorage.setItem("authToken", token);

// Lưu user info
localStorage.setItem("authUser", JSON.stringify(user));

// Set axios default header
import axios from "axios";
axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
```

**⚠️ Security Note:**

- localStorage dễ bị XSS attacks
- Nếu cần bảo mật cao hơn, dùng httpOnly cookies (cần BE support)
- Không lưu sensitive data (password, credit card) vào localStorage

---

### 4.2. Frontend - Send Token với mọi Request

**Option 1: Axios Interceptor (RECOMMENDED):**

```javascript
// src/lib/apiFetcher/axiosInstance.js
import axios from "axios";

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor - Tự động gắn token
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("authToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// Response interceptor - Handle 401
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired hoặc invalid
      localStorage.removeItem("authToken");
      localStorage.removeItem("authUser");
      window.location.href = "/auth/login";
    }
    return Promise.reject(error);
  },
);

export default axiosInstance;
```

**Option 2: Manual header (NOT RECOMMENDED):**

```javascript
// Phải nhớ thêm header mỗi lần gọi API
const token = localStorage.getItem("authToken");
const response = await axios.get("/api/student/courses", {
  headers: {
    Authorization: `Bearer ${token}`,
  },
});
```

---

### 4.3. Frontend - Check Token Expiration

**Decode JWT để check expiration:**

```javascript
// src/utils/jwtHelper.js
export function isTokenExpired(token) {
  if (!token) return true;

  try {
    // Decode payload (phần giữa của JWT)
    const payload = JSON.parse(atob(token.split(".")[1]));

    // Check expiration
    const now = Date.now() / 1000; // Convert to seconds
    return payload.exp < now;
  } catch (error) {
    console.error("Failed to decode token:", error);
    return true;
  }
}

export function getTokenExpirationTime(token) {
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return new Date(payload.exp * 1000); // Convert to milliseconds
  } catch (error) {
    console.error("Failed to decode token:", error);
    return null;
  }
}
```

**Usage:**

```javascript
import { isTokenExpired } from "@/utils/jwtHelper";

// Check trước khi gọi API
const token = localStorage.getItem("authToken");

if (isTokenExpired(token)) {
  // Redirect to login
  localStorage.removeItem("authToken");
  localStorage.removeItem("authUser");
  window.location.href = "/auth/login";
} else {
  // Safe to call API
  await fetchData();
}
```

---

### 4.4. Backend - Validate Token

**Backend tự động validate token qua JwtAuthenticationFilter:**

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extract token from header
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 2. Get user ID from token
                String userId = tokenProvider.getUserIdFromToken(jwt);

                // 3. Load user from database
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

                // 4. Set authentication
                UserPrincipal userPrincipal = UserPrincipal.create(user);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                    );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication", ex);
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

---

## 5. CODE EXAMPLES CHO VUE 3

### 5.1. AuthService.js - Complete Implementation

```javascript
// src/services/AuthService.js
import apiFetcher from "@/lib/apiFetcher/axiosInstance";

class AuthService {
  /**
   * Lấy user hiện tại từ API
   * @returns {Promise<UserResponse>}
   */
  async getCurrentUser() {
    const response = await apiFetcher.get("/auth/me");

    // Extract data from ApiResponse wrapper
    if (response.data.success) {
      return response.data.data; // Returns UserResponse
    } else {
      throw new Error(response.data.error);
    }
  }

  /**
   * Đăng xuất
   */
  async logout() {
    try {
      const response = await apiFetcher.post("/auth/logout");

      if (response.data.success) {
        console.log(response.data.message); // "Logged out successfully"
      }
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      this.clearAuthData();
    }
  }

  /**
   * Lưu auth token vào localStorage (called from OAuth2RedirectHandler)
   */
  setAuthToken(token) {
    localStorage.setItem("authToken", token);

    // Set axios default header
    apiFetcher.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  }

  /**
   * Xóa auth data khỏi localStorage
   */
  clearAuthData() {
    localStorage.removeItem("authToken");
    delete apiFetcher.defaults.headers.common["Authorization"];
  }

  /**
   * Lấy token từ localStorage
   */
  getToken() {
    return localStorage.getItem("authToken");
  }

  /**
   * Check xem user đã login chưa
   */
  isAuthenticated() {
    const token = this.getToken();
    if (!token) return false;

    // Check token expiration
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      const now = Date.now() / 1000;
      return payload.exp > now;
    } catch {
      return false;
    }
  }

  /**
   * Decode JWT to get user info
   */
  getUserFromToken() {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      return {
        userId: payload.sub,
        email: payload.email,
        role: payload.role, // "STUDENT", "TEACHER", or "ADMIN"
      };
    } catch {
      return null;
    }
  }
}

export default new AuthService();
```

---

### 5.2. useAuth.js - Composable for Vue 3

```javascript
// src/composables/useAuth.js
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import AuthService from "@/services/AuthService";

const currentUser = ref(null);
const isAuthenticated = ref(AuthService.isAuthenticated());

export function useAuth() {
  const router = useRouter();

  /**
   * Initialize user from token
   */
  onMounted(async () => {
    if (isAuthenticated.value) {
      try {
        await refreshUser();
      } catch (error) {
        console.error("Failed to load user:", error);
        logout();
      }
    }
  });

  /**
   * Redirect to Google OAuth2 login
   */
  const redirectToLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  /**
   * Đăng xuất
   */
  const logout = async () => {
    await AuthService.logout();

    currentUser.value = null;
    isAuthenticated.value = false;

    router.push("/auth/login");
  };

  /**
   * Refresh user info from API
   */
  const refreshUser = async () => {
    try {
      const user = await AuthService.getCurrentUser();
      currentUser.value = user;
      isAuthenticated.value = true;

      return user;
    } catch (error) {
      console.error("Failed to refresh user:", error);
      logout();
    }
  };

  /**
   * Check user có role cụ thể không
   * @param {string} role - "STUDENT", "TEACHER", or "ADMIN"
   */
  const hasRole = (role) => {
    return currentUser.value?.role === role;
  };

  /**
   * Get user info from JWT token (without API call)
   */
  const getUserFromToken = () => {
    return AuthService.getUserFromToken();
  };

  return {
    // State
    currentUser,
    isAuthenticated,

    // Actions
    redirectToLogin,
    logout,
    refreshUser,
    hasRole,
    getUserFromToken,

    // Getters
    userRole: computed(() => currentUser.value?.role),
    userName: computed(() => currentUser.value?.fullName),
    userEmail: computed(() => currentUser.value?.email),
    userAvatar: computed(() => currentUser.value?.avatarUrl),
  };
}
```

---

### 5.3. LoginGoogle.vue - Complete Component

```vue
<!-- src/views/auth/LoginGoogle.vue -->
<script setup>
import { ref } from "vue";
import { useAuth } from "@/composables/useAuth";

const { login } = useAuth();
const error = ref("");
const loading = ref(false);

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;

const handleGoogleLogin = async (response) => {
  loading.value = true;
  error.value = "";

  try {
    await login(response.credential);
    // Router sẽ tự redirect trong useAuth().login()
  } catch (err) {
    error.value = err.response?.data?.error?.message || "Đăng nhập thất bại";
    console.error("Login error:", err);
  } finally {
    loading.value = false;
  }
};

const handleGoogleError = (err) => {
  error.value = "Lỗi Google Sign-In";
  console.error("Google error:", err);
};
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo">
        <img src="/logo.png" alt="LMS Logo" />
      </div>

      <h1>Chào mừng đến LMS</h1>
      <p class="subtitle">Đăng nhập để tiếp tục</p>

      <!-- Google Sign-In Button -->
      <GoogleLogin
        :client-id="GOOGLE_CLIENT_ID"
        @success="handleGoogleLogin"
        @error="handleGoogleError"
      >
        <template #default="{ onClick }">
          <button @click="onClick" class="google-btn" :disabled="loading">
            <img src="/google-icon.svg" alt="Google" />
            <span v-if="!loading">Đăng nhập với Google</span>
            <span v-else>Đang đăng nhập...</span>
          </button>
        </template>
      </GoogleLogin>

      <!-- Error message -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <!-- Info -->
      <div class="info">
        <p>Sử dụng tài khoản Google FPT Education (@fpt.edu.vn)</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.logo {
  text-align: center;
  margin-bottom: 24px;
}

.logo img {
  width: 80px;
  height: 80px;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 32px;
}

.google-btn {
  width: 100%;
  padding: 12px 24px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
}

.google-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #4285f4;
}

.google-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.google-btn img {
  width: 20px;
  height: 20px;
}

.error-message {
  margin-top: 16px;
  padding: 12px;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 4px;
  color: #c33;
  text-align: center;
}

.info {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #666;
}
</style>
```

---

### 5.4. Router Guard - Protect Routes

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import AuthService from "@/services/AuthService";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/auth/login",
      name: "Login",
      component: () => import("@/views/auth/LoginGoogle.vue"),
    },
    {
      path: "/student/dashboard",
      name: "StudentDashboard",
      component: () => import("@/views/student/StudentDashboard.vue"),
      meta: { requiresAuth: true, roles: ["student"] },
    },
    {
      path: "/teacher/dashboard",
      name: "TeacherDashboard",
      component: () => import("@/views/teacher/TeacherDashboard.vue"),
      meta: { requiresAuth: true, roles: ["teacher"] },
    },
    {
      path: "/admin/dashboard",
      name: "AdminDashboard",
      component: () => import("@/views/admin/AdminDashboard.vue"),
      meta: { requiresAuth: true, roles: ["admin"] },
    },
  ],
});

// Navigation guard
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth);
  const isAuthenticated = AuthService.isAuthenticated();
  const user = AuthService.getUser();

  if (requiresAuth && !isAuthenticated) {
    // Cần login nhưng chưa login
    next("/auth/login");
    return;
  }

  if (requiresAuth && to.meta.roles) {
    // Check role
    const hasRequiredRole = to.meta.roles.includes(user?.role);

    if (!hasRequiredRole) {
      // Không có quyền truy cập
      alert("Bạn không có quyền truy cập trang này");
      next("/");
      return;
    }
  }

  next();
});

export default router;
```

---

## 6. ERROR HANDLING

### 6.1. Backend Error Response Format

**⚠️ Backend sử dụng ApiResponse wrapper cho TẤT CẢ responses (cả success và error).**

**Success Response:**

```json
{
  "success": true,
  "data": {
    /* actual data */
  },
  "message": "optional message",
  "error": null
}
```

**Error Response:**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Error message description"
}
```

**Common HTTP Status Codes:**

| Status | Meaning        | ApiResponse Example                                             |
| ------ | -------------- | --------------------------------------------------------------- |
| 200    | Success        | `{ "success": true, "data": {...} }`                            |
| 400    | Bad Request    | `{ "success": false, "error": "Bad request" }`                  |
| 401    | Unauthorized   | `{ "success": false, "error": "Invalid credentials" }`          |
| 403    | Forbidden      | `{ "success": false, "error": "Access denied" }`                |
| 404    | Not Found      | `{ "success": false, "error": "Resource not found" }`           |
| 500    | Internal Error | `{ "success": false, "error": "An unexpected error occurred" }` |

**Backend Exception Handler (GlobalExceptionHandler):**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 401 - Bad Credentials
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(ApiResponse.error("Invalid credentials"));
  }

  // 403 - Access Denied
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
      .body(ApiResponse.error("Access denied"));
  }

  // 500 - Runtime Exception
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiResponse.error(ex.getMessage()));
  }

  // 500 - Generic Exception
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiResponse.error("An unexpected error occurred"));
  }
}
```

---

### 6.2. Frontend Error Handling

**Axios interceptor (UPDATED for ApiResponse):**

```javascript
// src/lib/apiFetcher/axiosInstance.js
import axios from "axios";

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor - Auto attach token
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("authToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// Response interceptor - Handle ApiResponse wrapper
axiosInstance.interceptors.response.use(
  (response) => {
    // Check if response has ApiResponse wrapper
    if (response.data && typeof response.data.success === "boolean") {
      // Has ApiResponse wrapper
      if (!response.data.success) {
        // Success = false, but HTTP status is 2xx (shouldn't happen normally)
        console.warn("Response marked as failed:", response.data.error);
      }
    }
    return response;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;

      // Extract error message from ApiResponse wrapper
      const errorMessage = data.error || data.message || "Unknown error";

      console.error(`[${status}] ${errorMessage}`);

      // Handle specific status codes
      switch (status) {
        case 401:
          // Unauthorized - clear auth and redirect
          console.warn("Unauthorized - redirecting to login");
          localStorage.removeItem("authToken");
          localStorage.removeItem("authUser");
          window.location.href = "/auth/login";
          break;

        case 403:
          // Forbidden
          console.error("Access denied");
          alert("Bạn không có quyền thực hiện hành động này");
          break;

        case 404:
          // Not found
          console.error("Resource not found");
          break;

        case 500:
          // Server error
          console.error("Server error:", errorMessage);
          alert("Lỗi server, vui lòng thử lại sau");
          break;
      }
    } else if (error.request) {
      // No response from server
      console.error("No response from server");
      alert("Không thể kết nối đến server");
    } else {
      // Request setup error
      console.error("Request Error:", error.message);
    }

    return Promise.reject(error);
  },
);

export default axiosInstance;
```

**Component-level error handling:**

```vue
<script setup>
import { ref } from "vue";
import apiFetcher from "@/lib/apiFetcher/axiosInstance";

const user = ref(null);
const error = ref("");
const loading = ref(false);

const fetchCurrentUser = async () => {
  loading.value = true;
  error.value = "";

  try {
    const response = await apiFetcher.get("/auth/me");

    // Check ApiResponse wrapper
    if (response.data.success) {
      user.value = response.data.data; // Extract actual data
    } else {
      error.value = response.data.error;
    }
  } catch (err) {
    // HTTP error (4xx, 5xx)
    error.value = err.response?.data?.error || "Đã xảy ra lỗi không xác định";
    console.error("Fetch error:", err);
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div v-if="loading">Loading...</div>
  <div v-else-if="error" class="error">{{ error }}</div>
  <div v-else-if="user">
    <h1>Welcome, {{ user.fullName }}</h1>
    <p>Role: {{ user.role }}</p>
  </div>
</template>
```

---

## 7. TESTING GUIDE

### 7.1. Test Login Flow - Manual

**Bước 1:** Mở Chrome DevTools → Network tab

**Bước 2:** Click "Login with Google"

**Bước 3:** Kiểm tra request/response:

```
POST http://localhost:8080/api/auth/google

Request:
{
  "credential": "eyJhbGciOiJSUzI1NiIs..."
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "550e8400...",
    "name": "Nguyễn Văn An",
    "email": "annv@fpt.edu.vn",
    "role": "student",
    "avatar": "https://..."
  }
}
```

**Bước 4:** Kiểm tra localStorage:

```javascript
// Console
localStorage.getItem("authToken");
// → "eyJhbGciOiJIUzI1NiIs..."

localStorage.getItem("authUser");
// → JSON string chứa user info
```

**Bước 5:** Test authenticated request:

```javascript
// Console
fetch("http://localhost:8080/api/auth/me", {
  headers: {
    Authorization: `Bearer ${localStorage.getItem("authToken")}`,
  },
})
  .then((res) => res.json())
  .then((data) => console.log(data));
```

---

### 7.2. Test với Postman

**Test 1: POST /api/auth/google**

1. Method: `POST`
2. URL: `http://localhost:8080/api/auth/google`
3. Headers:
   ```
   Content-Type: application/json
   ```
4. Body (raw JSON):
   ```json
   {
     "credential": "YOUR_GOOGLE_CREDENTIAL_HERE"
   }
   ```
5. Send → Kiểm tra response

**Lấy Google credential:**

- Mở https://developers.google.com/oauthplayground
- Chọn Google OAuth2 API v2
- Scope: `email`, `profile`
- Authorize → Exchange authorization code → Copy `id_token`

---

**Test 2: GET /api/auth/me**

1. Method: `GET`
2. URL: `http://localhost:8080/api/auth/me`
3. Headers:
   ```
   Authorization: Bearer <TOKEN_FROM_TEST_1>
   ```
4. Send → Kiểm tra response

---

### 7.3. Test Token Expiration

**Mock expired token:**

```javascript
// Tạo token đã expire (backend test only)
public String generateExpiredToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() - 1000); // 1 second ago

    return Jwts.builder()
        .subject(user.getUserId())
        .claim("email", user.getEmail())
        .claim("role", user.getRole().getRoleName().name().toLowerCase())
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
}
```

**Test FE handle expired token:**

1. Set expired token vào localStorage
2. Gọi bất kỳ API nào
3. Verify: FE tự động redirect đến login page
4. Verify: localStorage đã bị xóa

---

## 8. SECURITY BEST PRACTICES

### 8.1. Frontend Security

✅ **DO:**

- Lưu token vào localStorage (acceptable cho non-critical apps)
- Luôn gửi token qua HTTPS trong production
- Check token expiration trước khi gọi API
- Clear token khi logout
- Validate user input trước khi gửi lên BE
- Use environment variables cho sensitive config

❌ **DON'T:**

- Lưu token hoặc sensitive data vào URL query params
- Log token ra console trong production
- Share token giữa các tabs/windows (có thể bị steal)
- Hardcode credentials trong code
- Disable CORS trong production

---

### 8.2. Backend Security

✅ **DO:**

- Verify Google credential với Google API, không tin frontend
- Use strong JWT secret (>= 256 bits)
- Set reasonable token expiration (24h - 1 week)
- Validate all input data
- Use HTTPS in production
- Enable CORS only cho trusted origins
- Rate limit authentication endpoints

❌ **DON'T:**

- Store sensitive data trong JWT payload (token có thể decode)
- Use weak JWT secret
- Skip Google credential verification
- Expose detailed error messages trong production
- Allow unlimited login attempts

---

## 9. TROUBLESHOOTING

### 9.1. Lỗi: "CORS policy: No 'Access-Control-Allow-Origin'"

**Nguyên nhân:** Backend chưa enable CORS cho frontend origin.

**Giải pháp:**

```java
// Backend - SecurityConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

### 9.2. Lỗi: "Invalid Google credential"

**Nguyên nhân:**

- Credential đã expired
- Google Client ID không khớp
- Credential không đúng format

**Giải pháp:**

1. Check Google Client ID đúng
2. Get credential mới từ Google
3. Verify credential chưa expire (< 5 minutes)
4. Check internet connection

---

### 9.3. Lỗi: "Token expired"

**Nguyên nhân:** JWT token đã quá 24 giờ.

**Giải pháp:**

1. Logout và login lại
2. Hoặc implement refresh token mechanism (advanced)

---

### 9.4. Lỗi: "403 Forbidden"

**Nguyên nhân:** User không có quyền truy cập endpoint.

**Ví dụ:** Student user cố truy cập `/api/teacher/classes`

**Giải pháp:**

- Check role trong token
- Verify endpoint có yêu cầu role gì
- Redirect user về đúng dashboard của họ

---

## 📞 SUPPORT

**Backend Team Contact:**

- Email: backend-team@fpt.edu.vn
- Slack: #backend-support
- Issue Tracker: https://github.com/your-org/backend/issues

**API Status Page:**

- Development: http://localhost:8080/actuator/health
- Staging: https://staging-api.certifyme.fpt.edu.vn/actuator/health
- Production: https://api.certifyme.fpt.edu.vn/actuator/health

---

## 📝 CHANGELOG

**Version 2.0 (2026-02-21) - MAJOR UPDATE**

✅ **Updated to match actual backend implementation:**

- **Authentication Flow**: Changed from REST API (POST /api/auth/google) to OAuth2 Redirect Flow
  - FE redirects to `/oauth2/authorization/google`
  - Backend redirects back to `http://localhost:3000/oauth2/redirect?token=xxx`
  - Added OAuth2RedirectHandler.vue component example
- **API Response Format**: All endpoints now use `ApiResponse<T>` wrapper
  - Success: `{ "success": true, "data": {...}, "message": null, "error": null }`
  - Error: `{ "success": false, "data": null, "message": null, "error": "..." }`
- **Field Names**: Updated to match actual backend DTOs
  - `userId` (not `id`)
  - `fullName` (not `name`)
  - `avatarUrl` (not `avatar`)
  - `role`: UPPERCASE values (`"STUDENT"`, `"TEACHER"`, `"ADMIN"`)

- **Error Handling**: Updated to match GlobalExceptionHandler implementation
  - 401: `{ "success": false, "error": "Invalid credentials" }`
  - 403: `{ "success": false, "error": "Access denied" }`
  - 500: `{ "success": false, "error": "An unexpected error occurred" }`

- **Code Examples**: All Vue 3 examples updated
  - Removed signInWithGoogle() method (doesn't exist)
  - Updated AuthService to work with ApiResponse wrapper
  - Updated useAuth composable to use redirectToLogin()
  - Fixed axios interceptor to handle ApiResponse format

- **Documentation**: Added supplementary docs
  - [API-RESPONSE-FORMAT.md](./API-RESPONSE-FORMAT.md) - Detailed API format guide
  - [FE-QUICK-START.md](./FE-QUICK-START.md) - 5-minute setup guide

**Version 1.0 (2026-02-20)**

- Initial release (documented planned implementation, not actual)
- Described REST API approach that was never implemented

---

## 🎯 QUICK LINKS

- **Quick Start**: [FE-QUICK-START.md](./FE-QUICK-START.md)
- **API Format**: [API-RESPONSE-FORMAT.md](./API-RESPONSE-FORMAT.md)
- **Testing Guide**: [TESTING_GUIDE.md](../TESTING_GUIDE.md)
- **OAuth2 Test Page**: [oauth2-test.html](../oauth2-test.html)

---

**Happy Coding! 🚀**

_Documentation maintained by Backend Team_
_Last updated: 2026-02-21_
