# ⚡ QUICK START GUIDE - Frontend Integration

> **5 phút setup authentication cho Frontend**

---

## 📋 TÓM TẮT

Backend sử dụng **OAuth2 Redirect Flow**, không phải REST API endpoint.

**Flow:**
1. FE redirect → `/oauth2/authorization/google`
2. User login Google
3. BE redirect về → `http://localhost:3000/oauth2/redirect?token=<JWT>`
4. FE lưu token → call APIs với header `Authorization: Bearer <token>`

---

## 🚀 SETUP (3 BƯỚC)

### Bước 1: Tạo nút đăng nhập

```vue
<!-- LoginPage.vue -->
<script setup>
const handleLogin = () => {
  window.location.href = 'http://localhost:8080/oauth2/authorization/google';
};
</script>

<template>
  <button @click="handleLogin">
    Đăng nhập với Google
  </button>
</template>
```

### Bước 2: Tạo OAuth2 redirect handler

```vue
<!-- OAuth2RedirectHandler.vue -->
<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');

  if (token) {
    // Lưu token
    localStorage.setItem('authToken', token);

    // Decode JWT để lấy role
    const payload = JSON.parse(atob(token.split('.')[1]));
    
    // Redirect dựa trên role
    if (payload.role === 'STUDENT') {
      router.push('/student/dashboard');
    } else if (payload.role === 'TEACHER') {
      router.push('/teacher/dashboard');
    } else if (payload.role === 'ADMIN') {
      router.push('/admin/dashboard');
    }
  } else {
    router.push('/auth/login');
  }
});
</script>

<template>
  <div>Đang xử lý đăng nhập...</div>
</template>
```

### Bước 3: Setup axios interceptor

```javascript
// axiosInstance.js
import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
});

// Auto attach token
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/auth/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
```

---

## 📡 API CALLS

### Get current user

```javascript
const response = await axiosInstance.get('/auth/me');

if (response.data.success) {
  const user = response.data.data;
  console.log(user.userId);    // "550e8400-e29b-41d4-..."
  console.log(user.fullName);  // "Nguyễn Văn An"
  console.log(user.email);     // "annv@fpt.edu.vn"
  console.log(user.role);      // "STUDENT"
  console.log(user.avatarUrl); // "https://..."
}
```

### Logout

```javascript
await axiosInstance.post('/auth/logout');
localStorage.removeItem('authToken');
window.location.href = '/auth/login';
```

---

## ⚠️ QUAN TRỌNG

### ApiResponse Wrapper

**Tất cả API responses có format:**

```json
{
  "success": true,
  "data": { /* actual data */ },
  "message": null,
  "error": null
}
```

**Always check:**
```javascript
if (response.data.success) {
  const actualData = response.data.data;  // ← YOUR DATA HERE
} else {
  const errorMsg = response.data.error;
}
```

### Field Names

**UserResponse fields:**
- ✅ `userId` (NOT `id`)
- ✅ `fullName` (NOT `name`)
- ✅ `avatarUrl` (NOT `avatar`)
- ✅ `role` is UPPERCASE: `"STUDENT"`, `"TEACHER"`, `"ADMIN"`

### JWT Token Payload

```json
{
  "sub": "550e8400-e29b-41d4-...",  // userId
  "email": "annv@fpt.edu.vn",
  "role": "STUDENT",                // UPPERCASE
  "iat": 1709123456,
  "exp": 1709209856                 // Expires after 24h
}
```

---

## 🛠 ENVIRONMENT CONFIG

```bash
# .env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_BACKEND_URL=http://localhost:8080
```

**Backend cần config:**
```properties
# application.properties
app.oauth2.redirect-uri=http://localhost:3000/oauth2/redirect
```

---

## 🧪 TESTING

### Test OAuth2 Flow

1. Open `http://localhost:5173/auth/login`
2. Click "Đăng nhập với Google"
3. Login with Google account
4. Should redirect to `http://localhost:3000/oauth2/redirect?token=...`
5. Check localStorage: `localStorage.getItem('authToken')`

### Test API Call

```javascript
// In browser console
const token = localStorage.getItem('authToken');

fetch('http://localhost:8080/api/auth/me', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
.then(res => res.json())
.then(data => console.log(data));

// Should return:
// {
//   "success": true,
//   "data": { userId, fullName, email, role, avatarUrl, isActive }
// }
```

---

## ❌ COMMON ERRORS

### "401 Unauthorized"
- Token không có hoặc sai format
- Token đã expired (>24h)
- **Fix:** Login lại

### "403 Forbidden"
- User không có quyền access endpoint
- **Fix:** Check role requirements

### "CORS Error"
- Backend chưa enable CORS cho frontend origin
- **Contact Backend team**

### Cannot get user info
- Check `response.data.success` first
- Extract data from `response.data.data`
- Use correct field names: `userId`, `fullName`, `avatarUrl`

---

## 📚 MORE INFO

- **Full Guide**: [FE-AUTH-INTEGRATION-GUIDE.md](./FE-AUTH-INTEGRATION-GUIDE.md)
- **API Format**: [API-RESPONSE-FORMAT.md](./API-RESPONSE-FORMAT.md)
- **Testing**: [TESTING_GUIDE.md](../TESTING_GUIDE.md)

---

## 📞 SUPPORT

**Backend Team:**
- Email: backend-team@fpt.edu.vn
- Slack: #backend-support

**API Health Check:**
- http://localhost:8080/actuator/health
