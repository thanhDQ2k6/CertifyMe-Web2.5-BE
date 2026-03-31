# Frontend: Fix API Response Structure

> **Issue**: Gọi API bị `undefined` (vd: `/api/teacher/undefined/classes`)
> **Nguyên nhân**: FE đọc sai cấu trúc response từ Backend

---

## Backend Response Structure

Tất cả API trả về dạng `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },      // <-- Data thực sự ở đây
  "error": null
}
```

---

## Vấn đề: Double `.data`

Khi dùng Axios, response có cấu trúc:

```
axios response
└── data (axios wrapper)
    └── success: true
    └── data (ApiResponse.data) <-- Data thực sự
        └── userId
        └── email
        └── ...
```

**SAI:**

```typescript
const response = await apiClient.get("/api/auth/me");
const userId = response.data.userId; // ❌ undefined
```

**ĐÚNG:**

```typescript
const response = await apiClient.get("/api/auth/me");
const userId = response.data.data.userId; // ✅ OK
```

---

## Fix 1: Update useAuth.ts

```typescript
// src/composables/useAuth.ts

async function fetchCurrentUser() {
  if (!token.value) return null;

  try {
    const response = await apiClient.get("/api/auth/me");

    // ⚠️ QUAN TRỌNG: response.data.data (không phải response.data)
    user.value = response.data.data;

    return user.value;
  } catch (error) {
    logout();
    return null;
  }
}
```

---

## Fix 2: Update Teacher Dashboard

```typescript
// Teacher Dashboard - Load classes

async function loadClasses() {
  const response = await apiClient.get("/api/auth/me");
  const currentUser = response.data.data; // ✅ Fix here

  if (!currentUser?.userId) {
    console.error("User not found");
    return;
  }

  const classesResponse = await apiClient.get(
    `/api/teacher/${currentUser.userId}/classes`,
  );

  classes.value = classesResponse.data.data; // ✅ Fix here too
}
```

---

## Fix 3: Update Student Dashboard

```typescript
// Student Dashboard - Load courses

async function loadCourses() {
  const response = await apiClient.get("/api/auth/me");
  const currentUser = response.data.data; // ✅ Fix

  const coursesResponse = await apiClient.get(
    `/api/student/${currentUser.userId}/courses`,
  );

  courses.value = coursesResponse.data.data; // ✅ Fix
}
```

---

## Fix 4: Tạo API Wrapper (Recommended)

Để tránh lặp `.data.data`, tạo wrapper:

```typescript
// src/api/wrapper.ts

import apiClient from "./client";
import type { AxiosResponse } from "axios";

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  error?: string;
}

/**
 * Unwrap ApiResponse và trả về data trực tiếp
 */
export async function api<T>(
  request: Promise<AxiosResponse<ApiResponse<T>>>,
): Promise<T> {
  const response = await request;

  if (!response.data.success) {
    throw new Error(response.data.error || "API Error");
  }

  return response.data.data;
}

// Usage:
// const user = await api(apiClient.get('/api/auth/me'));
// console.log(user.userId);  // ✅ Trực tiếp, không cần .data.data
```

**Sử dụng wrapper:**

```typescript
import { api } from "@/api/wrapper";
import apiClient from "@/api/client";

// Before (verbose)
const response = await apiClient.get("/api/auth/me");
const user = response.data.data;

// After (clean)
const user = await api(apiClient.get("/api/auth/me"));
```

---

## Fix 5: Update AuthService.js

Nếu đang dùng AuthService class:

```typescript
// src/services/AuthService.js

class AuthService {
  async getCurrentUser() {
    try {
      const response = await apiClient.get("/api/auth/me");

      // ❌ OLD: return response.data;
      // ✅ NEW:
      return response.data.data;
    } catch (error) {
      console.error("Error fetching current user:", error);
      throw error;
    }
  }
}
```

---

## Checklist

- [ ] `useAuth.ts` - `fetchCurrentUser()` đọc `response.data.data`
- [ ] `AuthService.js` - `getCurrentUser()` return `response.data.data`
- [ ] Teacher Dashboard - Load classes với đúng `userId`
- [ ] Student Dashboard - Load courses với đúng `userId`
- [ ] Tất cả API calls đều đọc `response.data.data`

---

## Debug Tips

### Log để kiểm tra structure:

```typescript
const response = await apiClient.get("/api/auth/me");
console.log("Full response:", response);
console.log("Axios data:", response.data);
console.log("API data:", response.data.data);
console.log("User ID:", response.data.data?.userId);
```

### Expected console output:

```
Full response: { status: 200, data: {...}, ... }
Axios data: { success: true, data: {...} }
API data: { userId: "xxx", email: "...", role: "TEACHER", ... }
User ID: "c167d02c-260e-464a-9443-ebfbc212f300"
```

---

## API Response Examples

### GET /api/auth/me

```json
{
  "success": true,
  "data": {
    "userId": "c167d02c-260e-464a-9443-ebfbc212f300",
    "email": "thanhdqts00628@fpt.edu.vn",
    "fullName": "Duong Quang Thanh (PTCD HCM)",
    "avatarUrl": "https://lh3.googleusercontent.com/...",
    "role": "TEACHER",
    "isActive": true
  }
}
```

### GET /api/teacher/{teacherId}/classes

```json
{
  "success": true,
  "data": [
    {
      "classId": "CLS-005",
      "classCode": "DS18302",
      "courseName": "Lập trình Python",
      "studentCount": 1,
      "status": "ACTIVE"
    }
  ]
}
```

### GET /api/student/{studentId}/courses

```json
{
  "success": true,
  "data": [
    {
      "courseId": "CRS-001",
      "courseName": "Lập trình Java 6",
      "progress": 75,
      "totalQuizzes": 4,
      "completedQuizzes": 3
    }
  ]
}
```
