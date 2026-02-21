# 📦 API RESPONSE FORMAT GUIDE

> **Backend Team** | Last Updated: 2026-02-21

---

## 🎯 TÓM TẮT QUAN TRỌNG

Backend sử dụng **ApiResponse wrapper** cho TẤT CẢ endpoints.

### Success Response Format

```json
{
  "success": true,
  "data": { /* actual data */ },
  "message": "optional message",
  "error": null
}
```

### Error Response Format

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Error message here"
}
```

---

## 📋 CHI TIẾT CÁC ENDPOINTS

### 1. GET /api/auth/me

**Success (200):**
```json
{
  "success": true,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "Nguyễn Văn An",
    "email": "annv@fpt.edu.vn",
    "role": "STUDENT",
    "avatarUrl": "https://lh3.googleusercontent.com/...",
    "isActive": true
  },
  "message": null,
  "error": null
}
```

**Error (401):**
```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Invalid credentials"
}
```

---

### 2. POST /api/auth/logout

**Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Logged out successfully",
  "error": null
}
```

---

### 3. GET /api/auth/check-role

**Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "You are logged in as: STUDENT",
  "error": null
}
```

---

## 🔑 FIELD MAPPINGS

### UserResponse Fields

| Backend Field | Type | Description | Example |
|--------------|------|-------------|---------|
| `userId` | String | UUID | `"550e8400-e29b-41d4-a716-446655440000"` |
| `fullName` | String | Tên đầy đủ | `"Nguyễn Văn An"` |
| `email` | String | Email từ Google | `"annv@fpt.edu.vn"` |
| `role` | String | Role (UPPERCASE) | `"STUDENT"`, `"TEACHER"`, `"ADMIN"` |
| `avatarUrl` | String | URL ảnh đại diện | `"https://lh3.googleusercontent.com/..."` |
| `isActive` | Boolean | Trạng thái tài khoản | `true` / `false` |

**⚠️ LƯU Ý:**
- Field names: `userId` (NOT `id`), `fullName` (NOT `name`), `avatarUrl` (NOT `avatar`)
- Role values: `STUDENT` (NOT `student`) - UPPERCASE

---

## 🔐 JWT TOKEN FORMAT

### Token Payload

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "annv@fpt.edu.vn",
  "role": "STUDENT",
  "iat": 1709123456,
  "exp": 1709209856
}
```

### Decode JWT trong Frontend

```javascript
// Extract payload from JWT
const token = localStorage.getItem('authToken');
const payload = JSON.parse(atob(token.split('.')[1]));

console.log('User ID:', payload.sub);
console.log('Email:', payload.email);
console.log('Role:', payload.role);  // "STUDENT", "TEACHER", or "ADMIN"
```

---

## ⚠️ ERROR HANDLING

### HTTP Status Codes

| Status | Meaning | ApiResponse |
|--------|---------|-------------|
| 200 | Success | `{ "success": true, "data": {...} }` |
| 400 | Bad Request | `{ "success": false, "error": "..." }` |
| 401 | Unauthorized | `{ "success": false, "error": "Invalid credentials" }` |
| 403 | Forbidden | `{ "success": false, "error": "Access denied" }` |
| 404 | Not Found | `{ "success": false, "error": "..." }` |
| 500 | Internal Error | `{ "success": false, "error": "An unexpected error occurred" }` |

### Frontend Error Handling

```javascript
// Axios interceptor
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      
      // Extract error message from ApiResponse
      const errorMessage = data.error || 'Unknown error';
      
      console.error(`[${status}] ${errorMessage}`);
      
      // Handle specific status codes
      if (status === 401) {
        // Clear token and redirect to login
        localStorage.removeItem('authToken');
        window.location.href = '/auth/login';
      }
    }
    
    return Promise.reject(error);
  }
);
```

---

## 🚀 FRONTEND INTEGRATION EXAMPLE

### AuthService with ApiResponse wrapper

```javascript
// src/services/AuthService.js
import apiFetcher from '@/lib/apiFetcher/axiosInstance';

class AuthService {
  /**
   * Get current user
   * @returns {Promise<UserResponse>}
   */
  async getCurrentUser() {
    const response = await apiFetcher.get('/auth/me');
    
    // Extract data from ApiResponse wrapper
    if (response.data.success) {
      return response.data.data;  // Returns UserResponse object
    } else {
      throw new Error(response.data.error);
    }
  }

  /**
   * Logout
   */
  async logout() {
    const response = await apiFetcher.post('/auth/logout');
    
    if (response.data.success) {
      localStorage.removeItem('authToken');
      console.log(response.data.message);  // "Logged out successfully"
    }
  }
}

export default new AuthService();
```

### Using in Vue Component

```vue
<script setup>
import { ref, onMounted } from 'vue';
import AuthService from '@/services/AuthService';

const user = ref(null);
const loading = ref(true);
const error = ref('');

onMounted(async () => {
  try {
    user.value = await AuthService.getCurrentUser();
    
    console.log('User ID:', user.value.userId);
    console.log('Full Name:', user.value.fullName);
    console.log('Role:', user.value.role);
    
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div v-if="loading">Loading...</div>
  <div v-else-if="error">{{ error }}</div>
  <div v-else>
    <h1>Welcome, {{ user.fullName }}</h1>
    <p>Email: {{ user.email }}</p>
    <p>Role: {{ user.role }}</p>
  </div>
</template>
```

---

## 📝 TYPESCRIPT INTERFACES

```typescript
// src/types/api.ts

/**
 * Generic API Response wrapper
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
  error: string | null;
}

/**
 * User Response (from /api/auth/me)
 */
export interface UserResponse {
  userId: string;
  fullName: string;
  email: string;
  role: 'STUDENT' | 'TEACHER' | 'ADMIN';
  avatarUrl: string;
  isActive: boolean;
}

/**
 * JWT Token Payload
 */
export interface JwtPayload {
  sub: string;      // User ID
  email: string;
  role: 'STUDENT' | 'TEACHER' | 'ADMIN';
  iat: number;      // Issued at (Unix timestamp)
  exp: number;      // Expires at (Unix timestamp)
}
```

### Usage with TypeScript

```typescript
// src/services/AuthService.ts
import type { ApiResponse, UserResponse } from '@/types/api';

class AuthService {
  async getCurrentUser(): Promise<UserResponse> {
    const response = await apiFetcher.get<ApiResponse<UserResponse>>('/auth/me');
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    throw new Error(response.data.error || 'Failed to fetch user');
  }
}
```

---

## ✅ CHECKLIST CHO FRONTEND TEAM

- [ ] Wrap all API responses với ApiResponse type
- [ ] Use correct field names: `userId`, `fullName`, `avatarUrl`
- [ ] Handle role values as UPPERCASE: `STUDENT`, `TEACHER`, `ADMIN`
- [ ] Check `response.data.success` trước khi access data
- [ ] Extract actual data from `response.data.data`
- [ ] Handle errors from `response.data.error`
- [ ] Implement axios interceptor để xử lý 401 errors
- [ ] JWT payload có role UPPERCASE, decode để lấy user info

---

## 📞 LIÊN HỆ

**Nếu có vấn đề:**
- Check `response.data.success` === `true` or `false`
- Log `response.data.error` để xem lỗi
- Verify JWT token chưa expired (check `exp` trong payload)
- Contact Backend team nếu cần support

**Backend Team:**
- Email: backend-team@fpt.edu.vn
- Slack: #backend-support
