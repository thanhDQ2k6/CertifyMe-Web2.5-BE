# 🔄 Redirect Strategy - Frontend vs Backend

> **Ngày**: 2026-02-21  
> **Version**: 1.0  
> **Mục đích**: Chi tiết kỹ lưỡng về cách xử lý redirect sau OAuth2 login - Backend xử lý hay Frontend xử lý? Cách tốt nhất cho từng trường hợp.

---

## 📑 MỤC LỤC

1. [Tổng quan hai cách tiếp cận](#1-tổng-quan-hai-cách-tiếp-cận)
2. [Phân tích chi tiết](#2-phân-tích-chi-tiết)
3. [Quyết định cuối cùng for CertifyMe](#3-quyết-định-cuối-cùng-for-certifyme)
4. [Cách triển khai chi tiết](#4-cách-triển-khai-chi-tiết)
5. [Xử lý các tình huống đặc biệt](#5-xử-lý-các-tình-huống-đặc-biệt)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. TỔNG QUAN HAI CÁCH TIẾP CẬN

### Cách 1: Backend Xử Lý Redirect

```
┌───────────────┐
│   Frontend    │
│  Click Login  │
│      │        │
│      ▼        │
│  Redirect to  │
│ BE OAuth2 URL │
└───────┬───────┘
        │ http://localhost:8080/oauth2/authorization/google
        │
        ▼
┌──────────────────────────┐
│   Backend (Xử lý chính)  │
│                          │
│ 1. Authenticate Google   │
│ 2. Get user info         │
│ 3. Find/Create user in   │
│    DB                    │
│ 4. Generate JWT          │
│ 5. Save JWT to cookie    │
│    (HttpOnly)            │
│ 6. Redirect to FE        │
│    (/dashboard)          │
└───────┬──────────────────┘
        │ Redirect: /dashboard
        │ Set-Cookie: jwt=...
        │
        ▼
┌──────────────────┐
│   Frontend       │
│ - Cookie tự gửi │
│   mỗi request   │
│ - Kiểm tra user │
│   role          │
│ - Redirect đến  │
│   role-specific │
│   dashboard     │
└──────────────────┘
```

**Ưu điểm:**

- ✅ Cookie HttpOnly an toàn hơn (XSS proof)
- ✅ Backend kiểm soát logic redirect (bảo mật hơn)
- ✅ Đơn giản cho frontend (không cần xử lý token)
- ✅ Session management dễ dàng

**Nhược điểm:**

- ❌ Phụ thuộc vào cookies (CORS cần config)
- ❌ Backend cần biết URL của mỗi role dashboard
- ❌ Khó maintain nếu URL thay đổi
- ❌ SPA không tận dụng hết routing capabilities

---

### Cách 2: Frontend Xử Lý Redirect (Recommended cho SPA)

```
┌───────────────┐
│   Frontend    │
│  Click Login  │
│      │        │
│      ▼        │
│  Redirect to  │
│ BE OAuth2 URL │
└───────┬───────┘
        │ http://localhost:8080/oauth2/authorization/google
        │
        ▼
┌──────────────────────────┐
│   Backend (Minimal)      │
│                          │
│ 1. Authenticate Google   │
│ 2. Get user info         │
│ 3. Find/Create user      │
│ 4. Generate JWT          │
│ 5. Redirect to FE        │
│    /oauth2/redirect?     │
│    token=<JWT>           │
└───────┬──────────────────┘
        │ Redirect: /oauth2/redirect?token=...
        │
        ▼
┌───────────────────────────┐
│   Frontend (Xử lý chính)  │
│                           │
│ OAuth2RedirectHandler:    │
│ 1. Extract token          │
│ 2. Save to localStorage   │
│ 3. Decode JWT             │
│ 4. Get user role          │
│ 5. Redirect based on role │
│    - STUDENT →            │
│      /student/dashboard   │
│    - TEACHER →            │
│      /teacher/dashboard   │
│    - ADMIN →              │
│      /admin/dashboard     │
└───────────────────────────┘
```

**Ưu điểm:**

- ✅ Frontend kiểm soát routing (SPA best practice)
- ✅ JWT rõ ràng (dễ debug, dễ testing)
- ✅ Dễ scale (thêm role chỉ cần update FE)
- ✅ Không phụ thuộc cookies
- ✅ Token refresh dễ hơn

**Nhược điểm:**

- ❌ Token lưu localStorage (XSS risk nếu không cẩn thận)
- ❌ Frontend phải handle token management
- ❌ Backend cần expose token trên URL (temporary, acceptable)

---

## 2. PHÂN TÍCH CHI TIẾT

### 2.1 Bảo Mật So Sánh

| Khía cạnh      | Backend Redirect  | Frontend Redirect |
| -------------- | ----------------- | ----------------- |
| Token Storage  | HttpOnly Cookie   | localStorage      |
| XSS Attack     | 🟢 Miễn nhiễm     | 🟡 Có rủi ro      |
| Token Theft    | 🟢 Khó (httpOnly) | 🟡 Dễ nếu XSS     |
| CSRF           | 🔴 Cần CSRF token | 🟢 Không affected |
| Token Exposure | 🟢 Hidden         | 🔴 Visible in URL |
| Phạm vi        | 🟢 Server-side    | 🟡 Client-side    |

**Mặc dù localStorage có XSS risk, là thực tiễn phổ biến cho SPA nếu:**

- Implement CSP (Content Security Policy)
- Sanitize user inputs
- Không inject external scripts
- Use secure dependencies

---

### 2.2 User Experience So Sánh

| Kịch bản           | Backend                  | Frontend                          |
| ------------------ | ------------------------ | --------------------------------- |
| Redirect sau login | Tự động (BE decide)      | Dựa trên role (FE decide)         |
| Route protection   | FE kiểm tra session      | FE kiểm tra token + role          |
| Role mismatch      | Không xảy ra             | Frontend xử lý (faster)           |
| Logout             | Delete session/cookie    | Clear localStorage                |
| Refresh page       | Session persist (cookie) | Token persist (localStorage)      |
| Back button        | Không quay lại login     | Có thể quay lại nếu token expired |

---

### 2.3 Code Complexity So Sánh

**Backend Redirect:**

```java
// Spring Boot
@GetMapping("/login/oauth2/code/google")
public String handleOAuth2Callback(@RequestParam String code) {
    User user = googleOAuth.authenticate(code);
    // Save JWT to HttpOnly cookie
    response.addCookie(createHttpOnlyCookie("jwt", token));

    // Backend decides redirect URL
    String redirectUrl = "/student/dashboard"; // or /teacher/...
    if (user.getRole().equals("ADMIN")) {
        redirectUrl = "/admin/dashboard";
    }
    return "redirect:" + redirectUrl;
}
```

**Frontend Redirect:**

```javascript
// Vue 3 + OAuth2RedirectHandler.vue
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');
const user = AuthService.getUserFromToken(token);
const role = user.role;

const dashboardUrls = {
    STUDENT: '/student/dashboard',
    TEACHER: '/teacher/dashboard',
    ADMIN: '/admin/dashboard'
};
router.push(dashboardUrls[role]);
```

**Frontend (FE Redirect) dễ maintain hơn vì:**

- Dashboard URL nằm trong FE config
- Thêm role mới không cần sửa BE
- Redirect logic centralized

---

## 3. QUYẾT ĐỊNH CUỐI CÙNG FOR CERTIFYME

### ✅ RECOMMENDED: Frontend Redirect (Cách 2)

**Lý do chọn:**

1. **SPA Architecture**: Vue 3 là single-page app, FE should control routing
2. **Role-Based Dashboard**: 3 dashboards khác nhau (student/teacher/admin) → logic nên ở FE
3. **Maintainability**: URL config nằm ở FE router, dễ thay đổi
4. **Scalability**: Thêm role mới chỉ cần update FE
5. **Industry Standard**: OAuth2 + JWT + localStorage là pattern phổ biến cho modern SPA
6. **Flexibility**: FE có full control over redirect logic (có thể implement A/B testing, analytics, etc.)

### Implementation Summary

```
User clicks Login
    ↓
FE redirects to: http://localhost:8080/oauth2/authorization/google
    ↓
BE handles Google OAuth2
    ↓
BE redirects to: http://localhost:3000/oauth2/redirect?token=<JWT>
    ↓
FE OAuth2RedirectHandler component:
  - Extracts token
  - Saves to localStorage
  - Decodes JWT to get role
  - Redirects to role-specific dashboard
    ↓
Dashboard loads with Authorization header containing JWT
```

---

## 4. CÁCH TRIỂN KHAI CHI TIẾT

### 4.1 Backend Implementation

**File:** `src/main/java/com/certifyme/config/OAuth2SuccessHandler.java`

```java
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // Get user from authentication
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = getUserFromOAuth2(oAuth2User);

        // Generate JWT token
        String jwtToken = jwtTokenProvider.generateToken(user);

        // ✅ IMPORTANT: Pass token in URL, not cookie
        // Frontend will extract and save to localStorage
        String redirectUrl = "http://localhost:3000/oauth2/redirect?token=" + jwtToken;

        // OR in production:
        // String redirectUrl = environment.getProperty("app.frontend-url") + "/oauth2/redirect?token=" + jwtToken;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private User getUserFromOAuth2(OAuth2User oAuth2User) {
        // Logic to find/create user from Google info
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName(name);
                newUser.setRole("STUDENT"); // Default role
                return userRepository.save(newUser);
            });

        return user;
    }
}
```

**Key Points:**

- ✅ Token passed in URL query parameter
- ✅ NO HttpOnly cookie (FE needs to access token)
- ✅ NO server-side session
- ✅ Backend responsibility: Generate valid JWT
- ❌ Backend does NOT decide redirect URL

---

### 4.2 Frontend OAuth2RedirectHandler Component

**File:** `src/views/auth/OAuth2RedirectHandler.vue`

```vue
<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/composables/useAuth';

const router = useRouter();
const { handleOAuth2Callback, error: authError } = useAuth();

onMounted(async () => {
    try {
        // Step 1: Extract token from URL query parameter
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');

        if (!token) {
            console.error('No token in OAuth2 redirect URL');
            router.push({ name: 'login' });
            return;
        }

        // Step 2: Process token via composable
        // - Save to localStorage
        // - Decode JWT
        // - Update auth state
        const userInfo = handleOAuth2Callback(token);

        // Step 3: Frontend decides redirect based on role
        // This is the key difference! Frontend has full control
        const roleRedirects = {
            STUDENT: { name: 'studentDashboard' },
            TEACHER: { name: 'teacherDashboard' },
            ADMIN: { name: 'adminDashboard' }
        };

        const redirectRoute = roleRedirects[userInfo.role];

        if (!redirectRoute) {
            console.error('Unknown role:', userInfo.role);
            router.push({ name: 'login' });
            return;
        }

        // Step 4: Redirect to role-specific dashboard
        router.push(redirectRoute);
    } catch (error) {
        console.error('OAuth2 callback error:', error);
        router.push({ name: 'login' });
    }
});
</script>

<template>
    <div class="flex items-center justify-center min-h-screen bg-surface-50 dark:bg-surface-950">
        <div class="text-center">
            <div class="flex justify-center mb-4">
                <div class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-primary-100 dark:bg-primary-900">
                    <i class="pi pi-spin pi-spinner text-primary-500 text-2xl"></i>
                </div>
            </div>
            <h2 class="text-2xl font-semibold mb-2">Đang xử lý đăng nhập...</h2>
            <p class="text-muted-color">Vui lòng chờ trong giây lát</p>
        </div>
    </div>
</template>
```

**Key Responsibilities:**

1. ✅ Extract token from URL
2. ✅ Save token to localStorage (via AuthService)
3. ✅ Decode JWT to get user role
4. ✅ **Decide redirect based on role** ← Frontend handles this!
5. ✅ Show loading spinner

---

### 4.3 AuthService Token Management

**File:** `src/services/AuthService.js`

```javascript
const TOKEN_KEY = 'authToken';
const USER_KEY = 'authUser';

const AuthService = {
    /**
     * Set JWT token from OAuth2 callback
     * Called by OAuth2RedirectHandler after backend redirects
     */
    setAuthToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
        // Set axios default Authorization header
        axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    },

    /**
     * Get token from localStorage
     */
    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },

    /**
     * Decode JWT payload WITHOUT verifying signature
     * (Backend will verify when using token)
     *
     * JWT structure: header.payload.signature
     * We only need the payload (part 2)
     */
    getUserFromToken() {
        const token = this.getToken();
        if (!token) return null;

        try {
            // Split JWT and decode payload
            const payload = JSON.parse(
                atob(token.split('.')[1]) // Decode base64url
            );

            return {
                userId: payload.sub, // User ID from 'sub' claim
                email: payload.email,
                role: payload.role // UPPERCASE: STUDENT, TEACHER, ADMIN
            };
        } catch (error) {
            console.error('Failed to decode JWT:', error);
            return null;
        }
    },

    /**
     * Check if token is valid and not expired
     */
    isAuthenticated() {
        const token = this.getToken();
        if (!token) return false;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const now = Math.floor(Date.now() / 1000); // Unix timestamp in seconds
            return payload.exp > now;
        } catch {
            return false;
        }
    },

    /**
     * Clear auth state (logout)
     */
    clearAuthData() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        delete axiosInstance.defaults.headers.common['Authorization'];
    }
};
```

**Why Frontend Decodes JWT:**

- ✅ No backend call needed (faster)
- ✅ Get role immediately for client-side routing
- ✅ Backend will verify when accessing protected endpoints
- ✅ Standard practice for SPA

---

### 4.4 useAuth Composable

**File:** `src/composables/useAuth.js`

```javascript
import { ref } from 'vue';
import AuthService from '@/services/AuthService';

const user = ref(null);
const isAuthenticated = ref(false);
const userRole = ref(null);

export function useAuth() {
    /**
     * Handle OAuth2 callback from OAuth2RedirectHandler
     * @param {string} token - JWT token from URL parameter
     */
    const handleOAuth2Callback = (token) => {
        try {
            if (!token) throw new Error('No token provided');

            // Step 1: Save token to localStorage and set axios header
            AuthService.setAuthToken(token);

            // Step 2: Decode token to get user info
            const userInfo = AuthService.getUserFromToken();
            if (!userInfo) throw new Error('Invalid token');

            // Step 3: Update composable state
            user.value = userInfo;
            isAuthenticated.value = true;
            userRole.value = userInfo.role;

            return userInfo;
        } catch (error) {
            console.error('OAuth2 callback error:', error);
            isAuthenticated.value = false;
            user.value = null;
            userRole.value = null;
            throw error;
        }
    };

    return {
        user,
        isAuthenticated,
        userRole,
        handleOAuth2Callback
    };
}
```

---

### 4.5 Router Configuration

**File:** `src/router/index.js` (relevant parts)

```javascript
import { useAuth } from '@/composables/useAuth';

const routes = [
    // OAuth2 callback handler
    {
        path: '/oauth2/redirect',
        name: 'oauth2Redirect',
        component: () => import('@/views/auth/OAuth2RedirectHandler.vue'),
        meta: { requiresAuth: false } // No guard, public route
    },

    // Student routes
    {
        path: '/student/dashboard',
        name: 'studentDashboard',
        component: () => import('@/views/student/StudentDashboard.vue'),
        meta: { requiresAuth: true, roles: ['STUDENT'] }
    },

    // Teacher routes
    {
        path: '/teacher/dashboard',
        name: 'teacherDashboard',
        component: () => import('@/views/teacher/TeacherDashboard.vue'),
        meta: { requiresAuth: true, roles: ['TEACHER'] }
    },

    // Admin routes
    {
        path: '/admin/dashboard',
        name: 'adminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] }
    }
];

// Navigation guard for protected routes
router.beforeEach((to, from, next) => {
    const auth = useAuth();
    const requiresAuth = to.meta.requiresAuth ?? false;
    const requiredRoles = to.meta.roles || [];

    if (requiresAuth) {
        if (!auth.isAuthenticated.value) {
            next({ name: 'login' });
            return;
        }

        if (requiredRoles.length && !requiredRoles.includes(auth.userRole.value)) {
            next({ name: 'accessDenied' });
            return;
        }
    }

    next();
});
```

**Key Points:**

- ✅ `/oauth2/redirect` is public route (no auth guard)
- ✅ Role dashboards are protected (requiresAuth: true)
- ✅ Role values are UPPERCASE (matching JWT)
- ✅ Navigation guard checks both auth AND role

---

## 5. XỬ LÝ CÁC TÌNH HUỐNG ĐẶC BIỆT

### 5.1 Người dùng reload page khi đang ở dashboard

**Vấn đề**: Token sẽ vẫn trong localStorage, không cần login lại

**Giải pháp**:

```javascript
// useAuth.js - initAuth() method
const initAuth = () => {
    const token = AuthService.getToken();

    if (token && AuthService.isAuthenticated()) {
        // Token exists and is valid
        const userInfo = AuthService.getUserFromToken();
        if (userInfo) {
            user.value = userInfo;
            isAuthenticated.value = true;
            userRole.value = userInfo.role;
        }
    } else {
        // Token expired or missing
        isAuthenticated.value = false;
        user.value = null;
        userRole.value = null;
    }
};

// Call on app startup
onMounted(() => {
    initAuth();
});
```

---

### 5.2 Token hết hạn trong lúc dùng

**Vấn đề**: API call trả về 401

**Giải pháp** (trong axios interceptor):

```javascript
// axiosInstance.js - response interceptor
axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Token expired
            localStorage.removeItem('authToken');
            window.location.href = '/auth/login';
        }
        return Promise.reject(error);
    }
);
```

---

### 5.3 User quay lại OAuth2 callback page

**Vấn đề**: URL vẫn chứa `?token=...`, token sẽ được process lại

**Giải pháp**: Clean URL sau redirect

```javascript
// OAuth2RedirectHandler.vue
const userInfo = handleOAuth2Callback(token);
const redirectRoute = roleRedirects[userInfo.role];

// Replace URL history (remove ?token=...)
window.history.replaceState({}, document.title, window.location.pathname);

// Then redirect
router.push(redirectRoute);
```

---

### 5.4 Backend error khi authenticate

**Vấn đề**: Backend không trả token, trả error thay vì

**Giải pháp**: Backend should redirect với error parameter

```java
// Backend
if (authenticationFailed) {
    String errorRedirect = "http://localhost:3000/oauth2/redirect?error=authentication_failed";
    getRedirectStrategy().sendRedirect(request, response, errorRedirect);
}
```

```javascript
// Frontend
const urlParams = new URLSearchParams(window.location.search);
const error = urlParams.get('error');
const token = urlParams.get('token');

if (error) {
    console.error('OAuth2 error:', error);
    router.push({ name: 'login' });
    return;
}

if (!token) {
    console.error('No token in redirect');
    router.push({ name: 'login' });
    return;
}
```

---

### 5.5 User không thuộc bất kỳ role nào

**Vấn đề**: JWT có role không hợp lệ

**Giải pháp**:

```javascript
// OAuth2RedirectHandler.vue
const roleRedirects = {
    STUDENT: { name: 'studentDashboard' },
    TEACHER: { name: 'teacherDashboard' },
    ADMIN: { name: 'adminDashboard' }
};

const redirectRoute = roleRedirects[userInfo.role];

if (!redirectRoute) {
    // Unknown role
    console.error('Unknown user role:', userInfo.role);
    AuthService.clearAuthData();
    router.push({ name: 'login' });
    return;
}
```

---

### 5.6 CORS issue với token in URL

**Vấn đề**: Redirect từ BE (8080) tới FE (3000) có CORS

**Giải pháp**: Browser tự handle redirect, không cần CORS

```
Backend redirect (302): http://localhost:3000/oauth2/redirect?token=...
↓
Browser tự theo redirect
↓
Không cần CORS vì không phải XMLHttpRequest/fetch
```

---

## 6. TROUBLESHOOTING

### Issue 1: "Cannot read property 'role' of null"

**Nguyên nhân**: JWT decode thất bại, userInfo returns null

**Debug**:

```javascript
const token = localStorage.getItem('authToken');
console.log('Token:', token);

const payload = JSON.parse(atob(token.split('.')[1]));
console.log('Payload:', payload);
console.log('Role:', payload.role);
```

**Giải pháp**:

- Check token format (must have 3 parts: header.payload.signature)
- Check backend generating valid JWT
- Check charset encoding (base64url)

---

### Issue 2: "Redirect loop - stuck on /oauth2/redirect"

**Nguyên nhân**: Component không redirect sau processing token

**Debug**:

```javascript
// OAuth2RedirectHandler.vue
console.log('Token:', token);
console.log('User info:', userInfo);
console.log('Role:', userInfo.role);
console.log('Redirect route:', redirectRoute);
```

**Giải pháp**:

- Check role in JWT matches route meta roles
- Check router has named route defined
- Check no middleware preventing navigation

---

### Issue 3: "Authorization header not sent to API"

**Nguyên nhân**: Token không save hoặc axios header không set

**Debug**:

```javascript
console.log('localStorage token:', localStorage.getItem('authToken'));
console.log('Axios header:', axiosInstance.defaults.headers.common.Authorization);
```

**Giải pháp**:

- Call `AuthService.setAuthToken(token)` in `handleOAuth2Callback`
- Check axios interceptor setup
- Check CORS credentials: 'include'

---

### Issue 4: "Token expired immediately"

**Nguyên nhân**: Backend JWT exp claim sai, hoặc client time mismatch

**Debug**:

```javascript
const payload = JSON.parse(atob(token.split('.')[1]));
const expiryDate = new Date(payload.exp * 1000);
console.log('Token expires at:', expiryDate);
console.log('Current time:', new Date());
```

**Giải pháp**:

- Check backend JWT generation uses correct expiration time
- Sync server and client time (NTP)
- Use reasonable expiration (24-48 hours)

---

### Issue 5: "Role not recognized in router"

**Nguyên nhân**: Role value case mismatch (lowercase vs uppercase)

**Debug**:

```javascript
console.log('JWT role:', userInfo.role); // Should be STUDENT, TEACHER, ADMIN
console.log('Route meta roles:', to.meta.roles); // Check case
```

**Giải pháp**:

- Backend JWT: use UPPERCASE role values
- Frontend router: use UPPERCASE in meta.roles
- Consistent throughout application

---

## 📋 IMPLEMENTATION CHECKLIST

### Backend (Spring Boot)

- [ ] Configure OAuth2 controller to handle Google auth
- [ ] Generate valid JWT token with:
    - [ ] `sub` (user ID)
    - [ ] `email`
    - [ ] `role` (UPPERCASE: STUDENT, TEACHER, ADMIN)
    - [ ] `iat` (issued at)
    - [ ] `exp` (expiration time, usually 24h later)
- [ ] Redirect to: `http://localhost:3000/oauth2/redirect?token=<JWT>`
- [ ] NO HttpOnly cookie set
- [ ] NO server-side session
- [ ] Error handling with error parameter in redirect URL

### Frontend (Vue 3)

- [ ] Create `OAuth2RedirectHandler.vue` component
    - [ ] Extract token from URL query parameter
    - [ ] Call `AuthService.setAuthToken(token)`
    - [ ] Decode JWT to get user role
    - [ ] Redirect to role-specific dashboard
- [ ] Update `AuthService.js`
    - [ ] Add `setAuthToken(token)` method
    - [ ] Add `getUserFromToken()` method (decode JWT)
    - [ ] Add `isAuthenticated()` method (check exp)
- [ ] Update `useAuth.js` composable
    - [ ] Add `handleOAuth2Callback(token)` method
    - [ ] Add `initAuth()` for resume session
- [ ] Update `router/index.js`
    - [ ] Add `/oauth2/redirect` public route
    - [ ] Add role-specific dashboard routes with guards
    - [ ] Use UPPERCASE role values in meta.roles

### Testing

- [ ] Test successful login flow
    - [ ] [ ] Click login button
    - [ ] [ ] Authenticate with Google
    - [ ] [ ] Backend redirects with token
    - [ ] [ ] FE extracts token
    - [ ] [ ] FE redirects to correct dashboard
- [ ] Test role-based redirect
    - [ ] [ ] STUDENT → /student/dashboard
    - [ ] [ ] TEACHER → /teacher/dashboard
    - [ ] [ ] ADMIN → /admin/dashboard
- [ ] Test token persist
    - [ ] [ ] Reload page, token still in localStorage
    - [ ] [ ] Authorization header still set
- [ ] Test token expiration
    - [ ] [ ] 401 error redirects to login
    - [ ] [ ] localStorage cleared

---

## 🎯 SUMMARY

| Aspect             | Frontend Redirect (CHOSEN)  | Backend Redirect    |
| ------------------ | --------------------------- | ------------------- |
| Token Location     | localStorage                | HttpOnly cookie     |
| Redirect Decision  | Frontend (useOAuth2Handler) | Backend             |
| Route URL          | In FE router config         | In BE properties    |
| Role Detection     | Decode JWT on FE            | N/A (HTTP redirect) |
| SPA Control        | Full (✅)                   | Limited (❌)        |
| Maintenance        | Easy (✅)                   | Hard (❌)           |
| Scalability        | Good (✅)                   | Poor (❌)           |
| **Recommendation** | **✅ RECOMMENDED**          | ❌ Not for SPA      |

---

## 📚 RELATED DOCS

- [FE-LOGIN-IMPLEMENTATION-GUIDE.md](./FE-LOGIN-IMPLEMENTATION-GUIDE.md) - Detailed implementation steps
- [BE-Integration-Guide.md](./BE-Integration-Guide.md) - Backend OAuth2 setup
- [uiPlan.md](./uiPlan.md) - UI component structure

---

## 🚀 NEXT STEPS

1. **Backend Team**:
    - Implement OAuth2 controller with JWT generation
    - Redirect to FE with token in query parameter
    - Ensure JWT has correct role (UPPERCASE)

2. **Frontend Team**:
    - Create `OAuth2RedirectHandler.vue`
    - Update `AuthService.js` with token management
    - Update router with role guards
    - Test full OAuth2 flow

3. **Both Teams**:
    - Sync on URL endpoints
    - Sync on JWT token structure
    - Test authentication flow E2E

---

**Last Updated**: 2026-02-21  
**Status**: Ready for Implementation  
**Decision**: Frontend Redirect (SPA Best Practice)
