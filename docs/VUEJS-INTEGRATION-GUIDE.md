# VueJS Integration Guide - LMS Frontend

> **Dự án**: LMS Backend Integration
> **Ngày cập nhật**: 19/03/2026
> **Base URL**: `http://localhost:8080`
> **Frontend Framework**: Vue 3 + Composition API + TypeScript

---

## Mục lục

1. [Cài đặt & Cấu hình cơ bản](#1-cài-đặt--cấu-hình-cơ-bản)
2. [Cấu trúc thư mục Frontend đề xuất](#2-cấu-trúc-thư-mục-frontend-đề-xuất)
3. [API Client & Interceptors](#3-api-client--interceptors)
4. [Xử lý Response Format](#4-xử-lý-response-format)
5. [Module: Authentication & Authorization](#5-module-authentication--authorization)
6. [Module: Student Dashboard](#6-module-student-dashboard)
7. [Module: Student Course Detail](#7-module-student-course-detail)
8. [Module: Teacher Classroom](#8-module-teacher-classroom)
9. [Module: Teacher Quiz Management](#9-module-teacher-quiz-management)
10. [Module: Admin Certificates](#10-module-admin-certificates)
11. [Module: Admin Users](#11-module-admin-users)
12. [Router Guards & Permission Matrix](#12-router-guards--permission-matrix)
13. [Error Handling Strategy](#13-error-handling-strategy)
14. [Placeholder & TODO Checklist](#14-placeholder--todo-checklist)

---

## 1. Cài đặt & Cấu hình cơ bản

### 1.1. Dependencies cần thiết

```bash
npm install axios vue-router pinia @vueuse/core
npm install -D @types/node
```

### 1.2. Environment Variables

Tạo file `.env` và `.env.production`:

```env
# .env (development)
VITE_API_BASE_URL=http://localhost:8080
VITE_OAUTH_GOOGLE_URL=http://localhost:8080/oauth2/authorization/google

# .env.production
VITE_API_BASE_URL=https://your-production-domain.com
VITE_OAUTH_GOOGLE_URL=https://your-production-domain.com/oauth2/authorization/google
```

### 1.3. TypeScript Config

```typescript
// src/env.d.ts
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
  readonly VITE_OAUTH_GOOGLE_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
```

---

## 2. Cấu trúc thư mục Frontend đề xuất

```
src/
├── api/                          # API layer
│   ├── client.ts                 # Axios instance + interceptors
│   ├── auth.api.ts               # Auth endpoints
│   ├── student.api.ts            # Student endpoints
│   ├── teacher.api.ts            # Teacher endpoints
│   └── admin.api.ts              # Admin endpoints
│
├── composables/                  # Reusable logic
│   ├── useAuth.ts                # Auth state & methods
│   ├── usePermission.ts          # Role-based permission checks
│   └── useApiError.ts            # Error handling
│
├── stores/                       # Pinia stores
│   ├── auth.store.ts             # User & token state
│   └── ui.store.ts               # UI state (loading, toast)
│
├── router/
│   ├── index.ts                  # Router setup
│   ├── guards.ts                 # Navigation guards
│   └── routes/
│       ├── auth.routes.ts
│       ├── student.routes.ts
│       ├── teacher.routes.ts
│       └── admin.routes.ts
│
├── types/                        # TypeScript interfaces
│   ├── api.types.ts              # API response types
│   ├── auth.types.ts
│   ├── student.types.ts
│   ├── teacher.types.ts
│   └── admin.types.ts
│
├── views/
│   ├── auth/
│   │   ├── LoginPage.vue
│   │   └── OAuthCallback.vue
│   ├── student/
│   │   ├── DashboardPage.vue
│   │   ├── CourseDetailPage.vue
│   │   ├── QuizPage.vue
│   │   └── CertificatesPage.vue
│   ├── teacher/
│   │   ├── ClassListPage.vue
│   │   ├── ClassDetailPage.vue
│   │   ├── QuizListPage.vue
│   │   └── QuizFormPage.vue
│   └── admin/
│       ├── CertificateDashboard.vue
│       ├── CertificateDetailPage.vue
│       └── UserManagementPage.vue
│
└── components/                   # Shared components
    ├── common/
    │   ├── LoadingSpinner.vue
    │   ├── ErrorMessage.vue
    │   └── Pagination.vue
    └── layout/
        ├── AppHeader.vue
        ├── AppSidebar.vue
        └── RoleBasedNav.vue
```

---

## 3. API Client & Interceptors

### 3.1. Axios Instance

```typescript
// src/api/client.ts
import axios, {
  AxiosInstance,
  AxiosError,
  InternalAxiosRequestConfig,
} from "axios";
import { useAuthStore } from "@/stores/auth.store";
import router from "@/router";

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});

// ============================================
// REQUEST INTERCEPTOR: Attach JWT token
// ============================================
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore();
    const token = authStore.token;

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

// ============================================
// RESPONSE INTERCEPTOR: Handle errors globally
// ============================================
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorResponse>) => {
    const authStore = useAuthStore();

    // ------------------------------------------
    // [REDIRECT] 401 Unauthorized → Login page
    // Token hết hạn hoặc không hợp lệ
    // ------------------------------------------
    if (error.response?.status === 401) {
      authStore.clearAuth();
      router.push({
        name: "Login",
        query: {
          redirect: router.currentRoute.value.fullPath,
          reason: "session_expired",
        },
      });
      return Promise.reject(error);
    }

    // ------------------------------------------
    // [REDIRECT] 403 Forbidden → Access Denied page
    // User không có quyền truy cập resource
    // ------------------------------------------
    if (error.response?.status === 403) {
      router.push({
        name: "AccessDenied",
        query: {
          attemptedPath: router.currentRoute.value.fullPath,
        },
      });
      return Promise.reject(error);
    }

    return Promise.reject(error);
  },
);

export default apiClient;

// Type for API error response
interface ApiErrorResponse {
  success: false;
  data: null;
  error: string;
}
```

### 3.2. Request/Response Types

```typescript
// src/types/api.types.ts

// ============================================
// STANDARD API RESPONSE FORMAT
// Backend luôn trả về cùng cấu trúc này
// ============================================
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T | null;
  error: string | null;
}

// ============================================
// PAGINATION RESPONSE
// Dùng cho: /api/certificates/recent, /api/admin/users, etc.
// ============================================
export interface PaginationResponse<T> {
  items: T[];
  pagination: PaginationInfo;
}

export interface PaginationInfo {
  page: number;
  limit: number;
  total: number;
  totalPages: number;
}

// ============================================
// COMMON QUERY PARAMS
// ============================================
export interface PaginationParams {
  page?: number;
  limit?: number;
}

export interface SearchParams extends PaginationParams {
  keyword?: string;
  status?: string;
  sort?: "asc" | "desc";
}
```

---

## 4. Xử lý Response Format

### 4.1. Helper Functions

```typescript
// src/api/helpers.ts
import type { ApiResponse } from "@/types/api.types";
import type { AxiosResponse } from "axios";

/**
 * Extract data from ApiResponse
 * Throws error if success is false
 */
export function extractData<T>(response: AxiosResponse<ApiResponse<T>>): T {
  const { success, data, error } = response.data;

  if (!success || data === null) {
    throw new Error(error || "Unknown error occurred");
  }

  return data;
}

/**
 * Safe extract - returns null instead of throwing
 */
export function safeExtractData<T>(
  response: AxiosResponse<ApiResponse<T>>,
): T | null {
  const { success, data } = response.data;
  return success ? data : null;
}
```

### 4.2. Usage Example

```typescript
// Trong API file
import apiClient from "./client";
import { extractData } from "./helpers";
import type { UserResponse } from "@/types/auth.types";

export async function getCurrentUser(): Promise<UserResponse> {
  const response =
    await apiClient.get<ApiResponse<UserResponse>>("/api/auth/me");
  return extractData(response);
}
```

---

## 5. Module: Authentication & Authorization

### 5.1. Types

```typescript
// src/types/auth.types.ts

export interface UserResponse {
  userId: string;
  email: string;
  name: string;
  avatarUrl: string | null;
  role: RoleType;
  isActive: boolean;
  createdAt: string;
}

export type RoleType = "STUDENT" | "TEACHER" | "ADMIN";

export interface CheckRoleResponse {
  role: RoleType;
  isActive: boolean;
}
```

### 5.2. Auth API

```typescript
// src/api/auth.api.ts
import apiClient from "./client";
import { extractData } from "./helpers";
import type { ApiResponse } from "@/types/api.types";
import type { UserResponse, CheckRoleResponse } from "@/types/auth.types";

// ============================================
// GOOGLE OAUTH LOGIN
// Redirect user to Google OAuth page
// ============================================
export function redirectToGoogleLogin(): void {
  // [PLACEHOLDER] Nếu cần tracking redirect path
  // localStorage.setItem('oauth_redirect', window.location.pathname)

  window.location.href = import.meta.env.VITE_OAUTH_GOOGLE_URL;
}

// ============================================
// GET CURRENT USER
// GET /api/auth/me
// Requires: Bearer token in header
// ============================================
export async function getCurrentUser(): Promise<UserResponse> {
  const response =
    await apiClient.get<ApiResponse<UserResponse>>("/api/auth/me");
  return extractData(response);
}

// ============================================
// CHECK USER ROLE
// GET /api/auth/check-role
// Useful for verifying role after login
// ============================================
export async function checkRole(): Promise<CheckRoleResponse> {
  const response = await apiClient.get<ApiResponse<CheckRoleResponse>>(
    "/api/auth/check-role",
  );
  return extractData(response);
}

// ============================================
// LOGOUT
// POST /api/auth/logout
// Note: Backend hiện tại là no-op, chỉ cần clear token ở FE
// ============================================
export async function logout(): Promise<void> {
  await apiClient.post("/api/auth/logout");
  // Token will be cleared by auth store
}
```

### 5.3. Auth Store

```typescript
// src/stores/auth.store.ts
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import type { UserResponse, RoleType } from "@/types/auth.types";
import * as authApi from "@/api/auth.api";

export const useAuthStore = defineStore("auth", () => {
  // ============================================
  // STATE
  // ============================================
  const token = ref<string | null>(localStorage.getItem("auth_token"));
  const user = ref<UserResponse | null>(null);
  const isInitialized = ref(false);

  // ============================================
  // GETTERS
  // ============================================
  const isAuthenticated = computed(() => !!token.value && !!user.value);
  const userRole = computed<RoleType | null>(() => user.value?.role ?? null);
  const userId = computed(() => user.value?.userId ?? null);

  // Role checks - dùng cho v-if trong template
  const isStudent = computed(() => userRole.value === "STUDENT");
  const isTeacher = computed(() => userRole.value === "TEACHER");
  const isAdmin = computed(() => userRole.value === "ADMIN");

  // ============================================
  // ACTIONS
  // ============================================

  /**
   * Set token after OAuth callback
   * @param newToken JWT token from URL query param
   */
  function setToken(newToken: string): void {
    token.value = newToken;
    localStorage.setItem("auth_token", newToken);
  }

  /**
   * Clear all auth state
   * Called on logout or 401 error
   */
  function clearAuth(): void {
    token.value = null;
    user.value = null;
    localStorage.removeItem("auth_token");
  }

  /**
   * Fetch current user profile
   * Called after OAuth callback & on app init
   */
  async function fetchCurrentUser(): Promise<void> {
    if (!token.value) {
      isInitialized.value = true;
      return;
    }

    try {
      user.value = await authApi.getCurrentUser();
    } catch (error) {
      // Token invalid or expired
      clearAuth();
    } finally {
      isInitialized.value = true;
    }
  }

  /**
   * Full logout flow
   */
  async function logout(): Promise<void> {
    try {
      await authApi.logout();
    } finally {
      clearAuth();
    }
  }

  /**
   * Initialize auth state on app load
   */
  async function initAuth(): Promise<void> {
    if (isInitialized.value) return;
    await fetchCurrentUser();
  }

  return {
    // State
    token,
    user,
    isInitialized,
    // Getters
    isAuthenticated,
    userRole,
    userId,
    isStudent,
    isTeacher,
    isAdmin,
    // Actions
    setToken,
    clearAuth,
    fetchCurrentUser,
    logout,
    initAuth,
  };
});
```

### 5.4. OAuth Callback Component

```vue
<!-- src/views/auth/OAuthCallback.vue -->
<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth.store";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const error = ref<string | null>(null);
const isProcessing = ref(true);

onMounted(async () => {
  // ============================================
  // EXTRACT TOKEN FROM URL
  // Backend redirects to: /oauth/callback?token=xxx
  // ============================================
  const token = route.query.token as string | undefined;

  if (!token) {
    error.value = "Không nhận được token từ server";
    isProcessing.value = false;
    return;
  }

  try {
    // Store token
    authStore.setToken(token);

    // Fetch user info
    await authStore.fetchCurrentUser();

    // ============================================
    // [REDIRECT] Role-based redirect after login
    // ============================================
    const role = authStore.userRole;

    // [PLACEHOLDER] Nếu có saved redirect path
    // const savedPath = localStorage.getItem('oauth_redirect')
    // if (savedPath) {
    //   localStorage.removeItem('oauth_redirect')
    //   router.replace(savedPath)
    //   return
    // }

    switch (role) {
      case "STUDENT":
        router.replace({ name: "StudentDashboard" });
        break;
      case "TEACHER":
        router.replace({ name: "TeacherClasses" });
        break;
      case "ADMIN":
        router.replace({ name: "AdminCertificates" });
        break;
      default:
        // Fallback nếu role không xác định
        router.replace({ name: "Home" });
    }
  } catch (err) {
    error.value = "Đăng nhập thất bại. Vui lòng thử lại.";
    authStore.clearAuth();
    isProcessing.value = false;
  }
});
</script>

<template>
  <div class="oauth-callback">
    <div v-if="isProcessing" class="loading">
      <LoadingSpinner />
      <p>Đang xử lý đăng nhập...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="$router.push({ name: 'Login' })">
        Quay lại đăng nhập
      </button>
    </div>
  </div>
</template>
```

### 5.5. Permission Composable

```typescript
// src/composables/usePermission.ts
import { computed } from "vue";
import { useAuthStore } from "@/stores/auth.store";
import type { RoleType } from "@/types/auth.types";

/**
 * Role hierarchy: ADMIN > TEACHER > STUDENT
 */
const ROLE_HIERARCHY: Record<RoleType, number> = {
  STUDENT: 1,
  TEACHER: 2,
  ADMIN: 3,
};

export function usePermission() {
  const authStore = useAuthStore();

  /**
   * Check if user has exact role
   */
  function hasRole(role: RoleType): boolean {
    return authStore.userRole === role;
  }

  /**
   * Check if user has minimum role level
   * Example: hasMinRole('TEACHER') returns true for TEACHER and ADMIN
   */
  function hasMinRole(minRole: RoleType): boolean {
    const userRole = authStore.userRole;
    if (!userRole) return false;
    return ROLE_HIERARCHY[userRole] >= ROLE_HIERARCHY[minRole];
  }

  /**
   * Check if user can access a resource
   * Used for owner-based access (e.g., student can only see their own courses)
   */
  function canAccessResource(ownerId: string): boolean {
    // Admin can access everything
    if (authStore.isAdmin) return true;
    // Owner can access their own resource
    return authStore.userId === ownerId;
  }

  /**
   * Check if current user is the owner of a resource
   */
  function isOwner(ownerId: string): boolean {
    return authStore.userId === ownerId;
  }

  return {
    hasRole,
    hasMinRole,
    canAccessResource,
    isOwner,
    // Re-export for convenience
    isStudent: computed(() => authStore.isStudent),
    isTeacher: computed(() => authStore.isTeacher),
    isAdmin: computed(() => authStore.isAdmin),
    currentUserId: computed(() => authStore.userId),
  };
}
```

---

## 6. Module: Student Dashboard

### 6.1. Types

```typescript
// src/types/student.types.ts

export interface StudentCourseResponse {
  classId: string;
  courseName: string;
  courseCode: string;
  courseIcon: string | null;
  className: string;
  teacherName: string;
  status: EnrollmentStatus;
  progress: number; // 0-100
  completedQuizzes: number;
  totalQuizzes: number;
  enrolledAt: string;
  certificateId: string | null; // null nếu chưa hoàn thành
}

export type EnrollmentStatus = "ACTIVE" | "COMPLETED" | "DROPPED";

export interface StudentCertificateResponse {
  certificateId: string;
  courseName: string;
  courseCode: string;
  issuedAt: string;
  status: CertificateStatus;
  blockchainVerified: boolean;
  certificateUrl: string | null; // URL để tải certificate PDF
}

export type CertificateStatus = "ISSUED" | "REVOKED";
```

### 6.2. Student API

```typescript
// src/api/student.api.ts
import apiClient from "./client";
import { extractData } from "./helpers";
import type { ApiResponse } from "@/types/api.types";
import type {
  StudentCourseResponse,
  StudentCertificateResponse,
} from "@/types/student.types";

// ============================================
// GET STUDENT COURSES (Dashboard)
// GET /api/student/{studentId}/courses
//
// [IMPORTANT] studentId phải match với user đang login
// Backend sẽ trả 403 nếu không khớp
// ============================================
export async function getStudentCourses(
  studentId: string,
): Promise<StudentCourseResponse[]> {
  const response = await apiClient.get<ApiResponse<StudentCourseResponse[]>>(
    `/api/student/${studentId}/courses`,
  );
  return extractData(response);
}

// ============================================
// GET STUDENT CERTIFICATES
// GET /api/student/{studentId}/certificates
//
// [IMPORTANT] studentId phải match với user đang login
// ============================================
export async function getStudentCertificates(
  studentId: string,
): Promise<StudentCertificateResponse[]> {
  const response = await apiClient.get<
    ApiResponse<StudentCertificateResponse[]>
  >(`/api/student/${studentId}/certificates`);
  return extractData(response);
}
```

### 6.2b. Enrollment Management API (Teacher/Admin)

```typescript
// src/api/enrollment.api.ts

import apiClient from "./client";
import { extractData } from "./helpers";
import type { ApiResponse } from "@/types/api.types";

// ============================================
// Types
// ============================================
export interface EnrollmentRequest {
  studentId: string;
  classId: string;
}

export interface EnrollmentResponse {
  enrollmentId: number;
  studentId: string;
  studentName: string;
  classId: string;
  className: string;
  status: "LEARNING" | "PASSED" | "FAILED" | "DROPPED";
  enrolledAt: string;
}

// ============================================
// CREATE ENROLLMENT
// POST /api/enrollments
//
// Requires: TEACHER or ADMIN role
// [IMPORTANT] Student phải có role STUDENT
// [ERROR] 400 nếu student đã enrolled trong class này
// ============================================
export async function createEnrollment(
  data: EnrollmentRequest,
): Promise<EnrollmentResponse> {
  const response = await apiClient.post<ApiResponse<EnrollmentResponse>>(
    "/api/enrollments",
    data,
  );
  return extractData(response);
}

// ============================================
// DELETE ENROLLMENT (Soft Delete)
// DELETE /api/enrollments/{enrollmentId}
//
// Requires: TEACHER or ADMIN role
// [IMPORTANT] Không xóa thật, chỉ đổi status → DROPPED
// ============================================
export async function deleteEnrollment(enrollmentId: number): Promise<void> {
  await apiClient.delete(`/api/enrollments/${enrollmentId}`);
}
```

### 6.3. Student Dashboard Page

```vue
<!-- src/views/student/DashboardPage.vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useAuthStore } from "@/stores/auth.store";
import { getStudentCourses } from "@/api/student.api";
import type {
  StudentCourseResponse,
  EnrollmentStatus,
} from "@/types/student.types";

const authStore = useAuthStore();
const courses = ref<StudentCourseResponse[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

// Filter state
const statusFilter = ref<EnrollmentStatus | "ALL">("ALL");

// Filtered courses
const filteredCourses = computed(() => {
  if (statusFilter.value === "ALL") return courses.value;
  return courses.value.filter((c) => c.status === statusFilter.value);
});

// Stats
const stats = computed(() => ({
  total: courses.value.length,
  active: courses.value.filter((c) => c.status === "ACTIVE").length,
  completed: courses.value.filter((c) => c.status === "COMPLETED").length,
}));

onMounted(async () => {
  // ============================================
  // [IMPORTANT] Sử dụng userId từ auth store
  // Không lấy từ URL params để tránh truy cập trái phép
  // ============================================
  const studentId = authStore.userId;

  if (!studentId) {
    error.value = "Không tìm thấy thông tin người dùng";
    isLoading.value = false;
    return;
  }

  try {
    courses.value = await getStudentCourses(studentId);
  } catch (err) {
    error.value = "Không thể tải danh sách khóa học";
  } finally {
    isLoading.value = false;
  }
});

function navigateToCourse(classId: string) {
  // [PLACEHOLDER] Navigate to course detail
  // router.push({ name: 'CourseDetail', params: { classId } })
}
</script>

<template>
  <div class="student-dashboard">
    <h1>Khóa học của tôi</h1>

    <!-- Stats Cards -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-value">{{ stats.total }}</span>
        <span class="stat-label">Tổng khóa học</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.active }}</span>
        <span class="stat-label">Đang học</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.completed }}</span>
        <span class="stat-label">Hoàn thành</span>
      </div>
    </div>

    <!-- Filter -->
    <div class="filter-row">
      <select v-model="statusFilter">
        <option value="ALL">Tất cả</option>
        <option value="ACTIVE">Đang học</option>
        <option value="COMPLETED">Hoàn thành</option>
        <option value="DROPPED">Đã hủy</option>
      </select>
    </div>

    <!-- Loading State -->
    <LoadingSpinner v-if="isLoading" />

    <!-- Error State -->
    <ErrorMessage v-else-if="error" :message="error" />

    <!-- Empty State -->
    <div v-else-if="filteredCourses.length === 0" class="empty-state">
      <p>Bạn chưa đăng ký khóa học nào</p>
      <!-- [PLACEHOLDER] Link to course catalog -->
    </div>

    <!-- Course List -->
    <div v-else class="course-grid">
      <div
        v-for="course in filteredCourses"
        :key="course.classId"
        class="course-card"
        @click="navigateToCourse(course.classId)"
      >
        <img
          :src="course.courseIcon || '/placeholder-course.png'"
          :alt="course.courseName"
          class="course-icon"
        />
        <div class="course-info">
          <h3>{{ course.courseName }}</h3>
          <p class="course-code">{{ course.courseCode }}</p>
          <p class="class-name">Lớp: {{ course.className }}</p>
          <p class="teacher">GV: {{ course.teacherName }}</p>

          <!-- Progress Bar -->
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: `${course.progress}%` }"
            ></div>
          </div>
          <p class="progress-text">
            {{ course.completedQuizzes }}/{{ course.totalQuizzes }} bài quiz
          </p>

          <!-- Status Badge -->
          <span
            :class="['status-badge', `status-${course.status.toLowerCase()}`]"
          >
            {{
              course.status === "ACTIVE"
                ? "Đang học"
                : course.status === "COMPLETED"
                  ? "Hoàn thành"
                  : "Đã hủy"
            }}
          </span>

          <!-- Certificate Link (if completed) -->
          <router-link
            v-if="course.certificateId"
            :to="{
              name: 'CertificateDetail',
              params: { id: course.certificateId },
            }"
            class="cert-link"
          >
            Xem chứng chỉ
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
```

---

## 7. Module: Student Course Detail

### 7.1. Types

```typescript
// src/types/student.types.ts (thêm vào)

export interface CourseDetailResponse {
  classId: string;
  courseName: string;
  courseCode: string;
  courseDescription: string;
  className: string;
  teacherId: string;
  teacherName: string;
  status: ClassStatus;
  startDate: string;
  endDate: string;

  // Student's enrollment info
  enrollment: EnrollmentInfo;

  // Quizzes in this class
  quizzes: QuizInfo[];

  // Certificate (if completed)
  certificate: CertificateInfo | null;
}

export interface EnrollmentInfo {
  enrollmentId: string;
  enrolledAt: string;
  status: EnrollmentStatus;
  progress: number;
}

export interface QuizInfo {
  quizId: string;
  title: string;
  description: string | null;
  timeLimit: number | null; // minutes, null = no limit
  maxAttempts: number;
  passingScore: number;
  status: QuizStatus;

  // Student's attempt info
  attemptCount: number;
  bestScore: number | null;
  lastAttemptAt: string | null;
  isPassed: boolean;

  // [IMPORTANT] Trạng thái UI
  // - 'completed': Đã hoàn thành và pass
  // - 'pending': Có thể làm bài
  // - 'locked': Chưa thể làm (quiz trước chưa pass, hoặc quiz chưa publish)
  displayStatus: "completed" | "pending" | "locked";
  lockReason: string | null; // Lý do bị lock, null nếu không locked
}

export type QuizStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";
export type ClassStatus = "ACTIVE" | "COMPLETED" | "CANCELLED";

export interface CertificateInfo {
  certificateId: string;
  issuedAt: string;
  status: CertificateStatus;
}
```

### 7.2. Course API

```typescript
// src/api/course.api.ts

import apiClient from "./client";
import { extractData } from "./helpers";
import type { ApiResponse } from "@/types/api.types";

// ============================================
// GET ALL PUBLIC COURSES
// GET /api/courses
//
// [PUBLIC] Không cần auth
// Returns: Danh sách khóa học active
// ============================================
export interface CourseListItem {
  courseId: string;
  courseCode: string;
  courseName: string;
  description: string | null;
}

export async function getAllCourses(): Promise<CourseListItem[]> {
  const response = await apiClient.get<ApiResponse<CourseListItem[]>>(
    "/api/courses",
  );
  return extractData(response);
}

// ============================================
// GET COURSE DETAIL (with student enrollment)
// GET /api/courses/{courseId}
//
// Requires: STUDENT role + enrolled in this class
// Returns: Course info + quizzes + enrollment status
// ============================================
export async function getCourseDetail(
  courseId: string,
): Promise<CourseDetailResponse> {
  const response = await apiClient.get<ApiResponse<CourseDetailResponse>>(
    `/api/courses/${courseId}`,
  );
  return extractData(response);
}
```

### 7.3. Course Detail Page

```vue
<!-- src/views/student/CourseDetailPage.vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getCourseDetail } from "@/api/student.api";
import type { CourseDetailResponse, QuizInfo } from "@/types/student.types";

const route = useRoute();
const router = useRouter();

const course = ref<CourseDetailResponse | null>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);

// Class ID from URL params
const classId = computed(() => route.params.classId as string);

// Quiz list computed
const quizzes = computed(() => course.value?.quizzes ?? []);
const completedQuizzes = computed(
  () => quizzes.value.filter((q) => q.displayStatus === "completed").length,
);

onMounted(async () => {
  try {
    course.value = await getCourseDetail(classId.value);
  } catch (err: any) {
    // ============================================
    // [ERROR HANDLING]
    // 404: Class không tồn tại
    // 403: Student không enrolled trong class này
    // ============================================
    if (err.response?.status === 404) {
      error.value = "Không tìm thấy khóa học";
    } else if (err.response?.status === 403) {
      error.value = "Bạn không có quyền truy cập khóa học này";
    } else {
      error.value = "Không thể tải thông tin khóa học";
    }
  } finally {
    isLoading.value = false;
  }
});

function startQuiz(quiz: QuizInfo) {
  // ============================================
  // [VALIDATION] Kiểm tra trước khi cho làm quiz
  // ============================================
  if (quiz.displayStatus === "locked") {
    // Show toast: quiz.lockReason
    return;
  }

  if (quiz.displayStatus === "completed") {
    // [PLACEHOLDER] Có thể cho xem lại kết quả?
    // router.push({ name: 'QuizResult', params: { quizId: quiz.quizId } })
    return;
  }

  // Kiểm tra số lần attempt
  if (quiz.attemptCount >= quiz.maxAttempts) {
    // Show toast: "Đã hết lượt làm bài"
    return;
  }

  router.push({ name: "QuizPage", params: { quizId: quiz.quizId } });
}
</script>

<template>
  <div class="course-detail">
    <LoadingSpinner v-if="isLoading" />

    <ErrorMessage v-else-if="error" :message="error" />

    <template v-else-if="course">
      <!-- Header -->
      <div class="course-header">
        <h1>{{ course.courseName }}</h1>
        <p class="course-code">{{ course.courseCode }}</p>
        <p class="course-desc">{{ course.courseDescription }}</p>
      </div>

      <!-- Class Info -->
      <div class="class-info">
        <h2>Thông tin lớp học</h2>
        <p><strong>Tên lớp:</strong> {{ course.className }}</p>
        <p><strong>Giáo viên:</strong> {{ course.teacherName }}</p>
        <p><strong>Bắt đầu:</strong> {{ formatDate(course.startDate) }}</p>
        <p><strong>Kết thúc:</strong> {{ formatDate(course.endDate) }}</p>
        <p>
          <strong>Trạng thái:</strong>
          <span
            :class="['status-badge', `status-${course.status.toLowerCase()}`]"
          >
            {{ course.status }}
          </span>
        </p>
      </div>

      <!-- Progress -->
      <div class="progress-section">
        <h2>Tiến độ học tập</h2>
        <div class="progress-bar large">
          <div
            class="progress-fill"
            :style="{ width: `${course.enrollment.progress}%` }"
          ></div>
        </div>
        <p>{{ completedQuizzes }}/{{ quizzes.length }} bài quiz hoàn thành</p>
      </div>

      <!-- Quiz List -->
      <div class="quiz-section">
        <h2>Danh sách bài quiz</h2>

        <div v-if="quizzes.length === 0" class="empty-state">
          <p>Chưa có bài quiz nào</p>
        </div>

        <div v-else class="quiz-list">
          <div
            v-for="quiz in quizzes"
            :key="quiz.quizId"
            :class="['quiz-card', `quiz-${quiz.displayStatus}`]"
          >
            <div class="quiz-header">
              <h3>{{ quiz.title }}</h3>

              <!-- Status Icons -->
              <span
                v-if="quiz.displayStatus === 'completed'"
                class="status-icon completed"
              >
                ✓ Hoàn thành
              </span>
              <span
                v-else-if="quiz.displayStatus === 'locked'"
                class="status-icon locked"
              >
                🔒 {{ quiz.lockReason }}
              </span>
              <span v-else class="status-icon pending"> Có thể làm </span>
            </div>

            <p v-if="quiz.description" class="quiz-desc">
              {{ quiz.description }}
            </p>

            <div class="quiz-meta">
              <span v-if="quiz.timeLimit">⏱ {{ quiz.timeLimit }} phút</span>
              <span
                >📝 {{ quiz.attemptCount }}/{{ quiz.maxAttempts }} lượt</span
              >
              <span>🎯 Điểm đạt: {{ quiz.passingScore }}</span>
            </div>

            <!-- Best Score (if attempted) -->
            <div v-if="quiz.bestScore !== null" class="quiz-score">
              <p>
                Điểm cao nhất:
                <strong
                  :class="{ passed: quiz.isPassed, failed: !quiz.isPassed }"
                >
                  {{ quiz.bestScore }}
                </strong>
              </p>
              <p class="last-attempt">
                Lần làm gần nhất: {{ formatDate(quiz.lastAttemptAt) }}
              </p>
            </div>

            <!-- Action Button -->
            <button
              :disabled="
                quiz.displayStatus === 'locked' ||
                quiz.attemptCount >= quiz.maxAttempts
              "
              @click="startQuiz(quiz)"
              class="btn-quiz"
            >
              <template v-if="quiz.displayStatus === 'completed'"
                >Xem kết quả</template
              >
              <template v-else-if="quiz.displayStatus === 'locked'"
                >Chưa mở</template
              >
              <template v-else-if="quiz.attemptCount >= quiz.maxAttempts"
                >Hết lượt</template
              >
              <template v-else-if="quiz.attemptCount > 0">Làm lại</template>
              <template v-else>Bắt đầu làm</template>
            </button>
          </div>
        </div>
      </div>

      <!-- Certificate Section -->
      <div v-if="course.certificate" class="certificate-section">
        <h2>Chứng chỉ</h2>
        <div class="certificate-card">
          <p>Đã cấp ngày: {{ formatDate(course.certificate.issuedAt) }}</p>
          <router-link
            :to="{
              name: 'CertificateDetail',
              params: { id: course.certificate.certificateId },
            }"
            class="btn-view-cert"
          >
            Xem chứng chỉ
          </router-link>
        </div>
      </div>
    </template>
  </div>
</template>
```

---

## 8. Module: Teacher Classroom

### 8.1. Types

```typescript
// src/types/teacher.types.ts

export interface TeacherClassResponse {
  classId: string;
  className: string;
  courseId: string;
  courseName: string;
  courseCode: string;
  status: ClassStatus;
  startDate: string;
  endDate: string;
  studentCount: number;
  quizCount: number;
  createdAt: string;
}

export interface ClassDetailResponse {
  classId: string;
  className: string;
  courseId: string;
  courseName: string;
  courseCode: string;
  courseDescription: string;
  status: ClassStatus;
  startDate: string;
  endDate: string;
  studentCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ClassStudentResponse {
  studentId: string;
  studentName: string;
  email: string;
  avatarUrl: string | null;
  enrolledAt: string;
  status: EnrollmentStatus;
  progress: number;
  completedQuizzes: number;
  totalQuizzes: number;
  averageScore: number | null;
}

// ============================================
// REQUEST DTOs
// ============================================

export interface CreateClassRequest {
  courseId: string; // [REQUIRED] ID của course
  className: string; // [REQUIRED] Tên lớp
  startDate: string; // [REQUIRED] Format: YYYY-MM-DD
  endDate: string; // [REQUIRED] Format: YYYY-MM-DD
}

export interface UpdateClassRequest {
  className?: string; // [OPTIONAL]
  status?: ClassStatus; // [OPTIONAL] 'ACTIVE' | 'COMPLETED' | 'CANCELLED'
  startDate?: string; // [OPTIONAL]
  endDate?: string; // [OPTIONAL]
}

// ============================================
// QUERY PARAMS
// ============================================

export interface ClassFilterParams {
  status?: ClassStatus;
  courseId?: string;
  sort?: "newest" | "oldest" | "name";
  page?: number;
  limit?: number;
}
```

### 8.2. Teacher API

```typescript
// src/api/teacher.api.ts
import apiClient from "./client";
import { extractData } from "./helpers";
import type { ApiResponse } from "@/types/api.types";
import type {
  TeacherClassResponse,
  ClassDetailResponse,
  ClassStudentResponse,
  CreateClassRequest,
  UpdateClassRequest,
  ClassFilterParams,
} from "@/types/teacher.types";

// ============================================
// GET TEACHER'S CLASSES
// GET /api/teacher/{teacherId}/classes
//
// [IMPORTANT] teacherId phải match với user đang login
// Backend sẽ trả 403 nếu không khớp
// ============================================
export async function getTeacherClasses(
  teacherId: string,
  params?: ClassFilterParams,
): Promise<TeacherClassResponse[]> {
  const response = await apiClient.get<ApiResponse<TeacherClassResponse[]>>(
    `/api/teacher/${teacherId}/classes`,
    { params },
  );
  return extractData(response);
}

// ============================================
// GET CLASS DETAIL
// GET /api/classes/{classId}
//
// [IMPORTANT] Chỉ teacher của class này mới xem được
// ============================================
export async function getClassDetail(
  classId: string,
): Promise<ClassDetailResponse> {
  const response = await apiClient.get<ApiResponse<ClassDetailResponse>>(
    `/api/classes/${classId}`,
  );
  return extractData(response);
}

// ============================================
// GET STUDENTS IN CLASS
// GET /api/classes/{classId}/students
//
// Returns: List of enrolled students with their progress
// ============================================
export async function getClassStudents(
  classId: string,
): Promise<ClassStudentResponse[]> {
  const response = await apiClient.get<ApiResponse<ClassStudentResponse[]>>(
    `/api/classes/${classId}/students`,
  );
  return extractData(response);
}

// ============================================
// CREATE NEW CLASS
// POST /api/classes
//
// [IMPORTANT] Teacher sẽ tự động được gán làm teacher của class
// ============================================
export async function createClass(
  data: CreateClassRequest,
): Promise<ClassDetailResponse> {
  const response = await apiClient.post<ApiResponse<ClassDetailResponse>>(
    "/api/classes",
    data,
  );
  return extractData(response);
}

// ============================================
// UPDATE CLASS
// PUT /api/classes/{classId}
//
// [IMPORTANT] Chỉ teacher của class mới update được
// ============================================
export async function updateClass(
  classId: string,
  data: UpdateClassRequest,
): Promise<ClassDetailResponse> {
  const response = await apiClient.put<ApiResponse<ClassDetailResponse>>(
    `/api/classes/${classId}`,
    data,
  );
  return extractData(response);
}
```

### 8.3. Teacher Classes Page

```vue
<!-- src/views/teacher/ClassListPage.vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth.store";
import { getTeacherClasses, createClass } from "@/api/teacher.api";
import type {
  TeacherClassResponse,
  CreateClassRequest,
  ClassStatus,
} from "@/types/teacher.types";

const router = useRouter();
const authStore = useAuthStore();

const classes = ref<TeacherClassResponse[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

// Filter state
const statusFilter = ref<ClassStatus | "ALL">("ALL");

// Create class modal
const showCreateModal = ref(false);
const isCreating = ref(false);
const createForm = ref<CreateClassRequest>({
  courseId: "",
  className: "",
  startDate: "",
  endDate: "",
});

// Filtered classes
const filteredClasses = computed(() => {
  if (statusFilter.value === "ALL") return classes.value;
  return classes.value.filter((c) => c.status === statusFilter.value);
});

onMounted(async () => {
  const teacherId = authStore.userId;

  if (!teacherId) {
    error.value = "Không tìm thấy thông tin giáo viên";
    isLoading.value = false;
    return;
  }

  await loadClasses(teacherId);
});

async function loadClasses(teacherId: string) {
  try {
    isLoading.value = true;
    classes.value = await getTeacherClasses(teacherId);
  } catch (err) {
    error.value = "Không thể tải danh sách lớp học";
  } finally {
    isLoading.value = false;
  }
}

async function handleCreateClass() {
  // ============================================
  // [VALIDATION] Form validation
  // ============================================
  if (!createForm.value.courseId) {
    // Show error: "Vui lòng chọn khóa học"
    return;
  }
  if (!createForm.value.className.trim()) {
    // Show error: "Vui lòng nhập tên lớp"
    return;
  }
  if (!createForm.value.startDate || !createForm.value.endDate) {
    // Show error: "Vui lòng chọn ngày bắt đầu và kết thúc"
    return;
  }
  if (
    new Date(createForm.value.startDate) >= new Date(createForm.value.endDate)
  ) {
    // Show error: "Ngày kết thúc phải sau ngày bắt đầu"
    return;
  }

  try {
    isCreating.value = true;
    const newClass = await createClass(createForm.value);
    classes.value.unshift(newClass as any); // Add to top of list
    showCreateModal.value = false;
    // Reset form
    createForm.value = {
      courseId: "",
      className: "",
      startDate: "",
      endDate: "",
    };
  } catch (err: any) {
    // [PLACEHOLDER] Show error toast
    console.error("Failed to create class:", err);
  } finally {
    isCreating.value = false;
  }
}

function viewClassDetail(classId: string) {
  router.push({ name: "ClassDetail", params: { classId } });
}
</script>

<template>
  <div class="teacher-classes">
    <div class="page-header">
      <h1>Quản lý lớp học</h1>
      <button @click="showCreateModal = true" class="btn-primary">
        + Tạo lớp mới
      </button>
    </div>

    <!-- Filter Row -->
    <div class="filter-row">
      <select v-model="statusFilter">
        <option value="ALL">Tất cả trạng thái</option>
        <option value="ACTIVE">Đang hoạt động</option>
        <option value="COMPLETED">Đã kết thúc</option>
        <option value="CANCELLED">Đã hủy</option>
      </select>
    </div>

    <!-- Loading -->
    <LoadingSpinner v-if="isLoading" />

    <!-- Error -->
    <ErrorMessage v-else-if="error" :message="error" />

    <!-- Empty -->
    <div v-else-if="filteredClasses.length === 0" class="empty-state">
      <p>Chưa có lớp học nào</p>
      <button @click="showCreateModal = true">Tạo lớp đầu tiên</button>
    </div>

    <!-- Class List -->
    <div v-else class="class-grid">
      <div
        v-for="classItem in filteredClasses"
        :key="classItem.classId"
        class="class-card"
        @click="viewClassDetail(classItem.classId)"
      >
        <h3>{{ classItem.className }}</h3>
        <p class="course-name">
          {{ classItem.courseName }} ({{ classItem.courseCode }})
        </p>

        <div class="class-stats">
          <span>👥 {{ classItem.studentCount }} học viên</span>
          <span>📝 {{ classItem.quizCount }} bài quiz</span>
        </div>

        <div class="class-dates">
          <span
            >{{ formatDate(classItem.startDate) }} -
            {{ formatDate(classItem.endDate) }}</span
          >
        </div>

        <span
          :class="['status-badge', `status-${classItem.status.toLowerCase()}`]"
        >
          {{ classItem.status }}
        </span>
      </div>
    </div>

    <!-- Create Class Modal -->
    <!-- [PLACEHOLDER] Implement modal component -->
  </div>
</template>
```

---

## 9. Module: Teacher Quiz Management

### 9.1. Types

```typescript
// src/types/teacher.types.ts (thêm vào)

export interface QuizListResponse {
  quizId: string;
  title: string;
  description: string | null;
  classId: string;
  className: string;
  status: QuizStatus;
  timeLimit: number | null;
  maxAttempts: number;
  passingScore: number;
  questionCount: number;
  submissionCount: number;
  createdAt: string;
}

export interface QuizDetailResponse {
  quizId: string;
  title: string;
  description: string | null;
  classId: string;
  className: string;
  status: QuizStatus;
  timeLimit: number | null;
  maxAttempts: number;
  passingScore: number;
  createdAt: string;
  updatedAt: string;
  questions: QuestionResponse[];
}

export interface QuestionResponse {
  questionId: string;
  questionText: string;
  questionType: QuestionType;
  options: string[]; // JSON array cho multiple choice
  correctAnswer: string; // [TEACHER ONLY] Đáp án đúng
  points: number;
  orderIndex: number;
}

export type QuestionType = "MULTIPLE_CHOICE" | "TRUE_FALSE" | "SHORT_ANSWER";
export type QuizStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";

export interface QuizSubmissionListResponse {
  attemptId: string;
  studentId: string;
  studentName: string;
  email: string;
  score: number;
  isPassed: boolean;
  submittedAt: string;
  timeSpent: number; // seconds
}

// ============================================
// REQUEST DTOs
// ============================================

export interface CreateQuizRequest {
  classId: string; // [REQUIRED]
  title: string; // [REQUIRED]
  description?: string; // [OPTIONAL]
  timeLimit?: number; // [OPTIONAL] minutes
  maxAttempts: number; // [REQUIRED] default: 1
  passingScore: number; // [REQUIRED] default: 5
  questions: CreateQuestionRequest[];
}

export interface CreateQuestionRequest {
  questionText: string; // [REQUIRED]
  questionType: QuestionType; // [REQUIRED]
  options: string[]; // [REQUIRED for MULTIPLE_CHOICE]
  correctAnswer: string; // [REQUIRED]
  points: number; // [REQUIRED] default: 1
  orderIndex: number; // [REQUIRED]
}

export interface UpdateQuizRequest {
  title?: string;
  description?: string;
  status?: QuizStatus; // DRAFT → PUBLISHED → ARCHIVED
  timeLimit?: number;
  maxAttempts?: number;
  passingScore?: number;
  questions?: UpdateQuestionRequest[];
}

export interface UpdateQuestionRequest extends CreateQuestionRequest {
  questionId?: string; // null = new question, có = update existing
}
```

### 9.2. Quiz API

```typescript
// src/api/teacher.api.ts (thêm vào)

// ============================================
// GET QUIZZES BY CLASS
// GET /api/classes/{classId}/quizzes
// ============================================
export async function getQuizzesByClass(
  classId: string,
): Promise<QuizListResponse[]> {
  const response = await apiClient.get<ApiResponse<QuizListResponse[]>>(
    `/api/classes/${classId}/quizzes`,
  );
  return extractData(response);
}

// ============================================
// GET QUIZ DETAIL (Teacher view - includes answers)
// GET /api/quizzes/{quizId}
//
// [IMPORTANT] Trả về correctAnswer cho teacher
// ============================================
export async function getQuizDetail(
  quizId: string,
): Promise<QuizDetailResponse> {
  const response = await apiClient.get<ApiResponse<QuizDetailResponse>>(
    `/api/quizzes/${quizId}`,
  );
  return extractData(response);
}

// ============================================
// CREATE QUIZ
// POST /api/quizzes
// ============================================
export async function createQuiz(
  data: CreateQuizRequest,
): Promise<QuizDetailResponse> {
  const response = await apiClient.post<ApiResponse<QuizDetailResponse>>(
    "/api/quizzes",
    data,
  );
  return extractData(response);
}

// ============================================
// UPDATE QUIZ
// PUT /api/quizzes/{quizId}
//
// [IMPORTANT] Chỉ update được khi quiz ở trạng thái DRAFT
// Nếu đã PUBLISHED, phải ARCHIVE trước rồi tạo quiz mới
// ============================================
export async function updateQuiz(
  quizId: string,
  data: UpdateQuizRequest,
): Promise<QuizDetailResponse> {
  const response = await apiClient.put<ApiResponse<QuizDetailResponse>>(
    `/api/quizzes/${quizId}`,
    data,
  );
  return extractData(response);
}

// ============================================
// DELETE QUIZ
// DELETE /api/quizzes/{quizId}
//
// [IMPORTANT] Chỉ xóa được quiz DRAFT chưa có submission
// ============================================
export async function deleteQuiz(quizId: string): Promise<void> {
  await apiClient.delete(`/api/quizzes/${quizId}`);
}

// ============================================
// GET QUIZ SUBMISSIONS
// GET /api/quizzes/{quizId}/submissions
//
// Returns: List of student submissions with scores
// ============================================
export async function getQuizSubmissions(
  quizId: string,
): Promise<QuizSubmissionListResponse[]> {
  const response = await apiClient.get<
    ApiResponse<QuizSubmissionListResponse[]>
  >(`/api/quizzes/${quizId}/submissions`);
  return extractData(response);
}
```

### 9.3. Student Quiz Submit API

```typescript
// src/api/student.api.ts (thêm vào)

export interface QuizForStudentResponse {
  quizId: string;
  title: string;
  description: string | null;
  timeLimit: number | null;
  maxAttempts: number;
  passingScore: number;
  attemptCount: number; // Số lần đã làm
  remainingAttempts: number; // Số lần còn lại
  questions: StudentQuestionResponse[];
}

export interface StudentQuestionResponse {
  questionId: string;
  questionText: string;
  questionType: QuestionType;
  options: string[]; // Không có correctAnswer
  points: number;
  orderIndex: number;
}

export interface SubmitQuizRequest {
  answers: SubmitAnswerRequest[];
}

export interface SubmitAnswerRequest {
  questionId: string;
  answer: string;
}

export interface QuizSubmitResponse {
  attemptId: string;
  score: number;
  totalPoints: number;
  isPassed: boolean;
  timeTaken: number; // seconds
  submittedAt: string;

  // [OPTIONAL] Chi tiết từng câu (nếu BE trả về)
  questionResults?: QuestionResultResponse[];
}

export interface QuestionResultResponse {
  questionId: string;
  isCorrect: boolean;
  pointsEarned: number;
  correctAnswer?: string; // [OPTIONAL] Chỉ show nếu BE cho phép
}

// ============================================
// GET QUIZ FOR STUDENT (No answers)
// GET /api/quizzes/{quizId}
//
// [IMPORTANT] Response khác với teacher view:
// - Không có correctAnswer
// - Chỉ quiz PUBLISHED mới xem được
// ============================================
export async function getQuizForStudent(
  quizId: string,
): Promise<QuizForStudentResponse> {
  const response = await apiClient.get<ApiResponse<QuizForStudentResponse>>(
    `/api/quizzes/${quizId}`,
  );
  return extractData(response);
}

// ============================================
// SUBMIT QUIZ
// POST /api/quizzes/{quizId}/submit
//
// [VALIDATION]
// - Quiz phải PUBLISHED
// - Student phải enrolled trong class của quiz
// - Còn lượt làm bài (attemptCount < maxAttempts)
// ============================================
export async function submitQuiz(
  quizId: string,
  data: SubmitQuizRequest,
): Promise<QuizSubmitResponse> {
  const response = await apiClient.post<ApiResponse<QuizSubmitResponse>>(
    `/api/quizzes/${quizId}/submit`,
    data,
  );
  return extractData(response);
}

// ============================================
// GET QUIZ RESULT (Student's best score)
// GET /api/quizzes/{quizId}/result
//
// Requires: STUDENT role
// [IMPORTANT] Lấy studentId từ JWT token
// Returns: Điểm cao nhất trong các lần làm bài
// [ERROR] 404 nếu student chưa làm bài quiz này
// ============================================
export interface QuizResultResponse {
  quizId: string;
  quizTitle: string;
  score: number;
  maxScore: number;
  passingScore: number;
  status: "PASSED" | "FAILED";
  attemptCount: number;
  submittedAt: string;
}

export async function getQuizResult(quizId: string): Promise<QuizResultResponse> {
  const response = await apiClient.get<ApiResponse<QuizResultResponse>>(
    `/api/quizzes/${quizId}/result`,
  );
  return extractData(response);
}
```

---

## 10. Module: Admin Certificates

### 10.1. Types

```typescript
// src/types/admin.types.ts

export interface CertificateStatsResponse {
  totalIssued: number;
  totalRevoked: number;
  totalPending: number; // [PLACEHOLDER] Nếu có trạng thái pending
  issuedThisMonth: number;
  issuedThisWeek: number;
}

export interface AdminCertificateResponse {
  certificateId: string;
  studentId: string;
  studentName: string;
  studentEmail: string;
  courseId: string;
  courseName: string;
  courseCode: string;
  classId: string;
  className: string;
  issuedAt: string;
  status: CertificateStatus;
  blockchainVerified: boolean;
  blockchainTxHash: string | null;
}

export interface AdminCertificateDetailResponse extends AdminCertificateResponse {
  // Additional detail fields
  teacherId: string;
  teacherName: string;
  enrolledAt: string;
  completedAt: string;
  averageScore: number;
  quizScores: QuizScoreInfo[];
}

export interface QuizScoreInfo {
  quizId: string;
  quizTitle: string;
  score: number;
  maxScore: number;
  attemptCount: number;
  isPassed: boolean;
}

export interface CertificateVerifyResponse {
  isValid: boolean;
  certificateId: string;
  studentName: string;
  courseName: string;
  issuedAt: string;
  blockchainVerified: boolean;
  blockchainTxHash: string | null;
  revokedAt: string | null; // null nếu chưa revoke
  revokeReason: string | null;
}

// ============================================
// REQUEST DTOs
// ============================================

export interface RevokeRequest {
  reason: string; // [REQUIRED] Lý do thu hồi
}

// ============================================
// QUERY PARAMS
// ============================================

export interface CertificateSearchParams {
  keyword?: string; // Search by student name, email, course name
  status?: CertificateStatus;
  courseId?: string;
  startDate?: string; // Filter by issuedAt
  endDate?: string;
  page?: number;
  limit?: number;
  sort?: "newest" | "oldest";
}
```

### 10.2. Admin Certificate API

```typescript
// src/api/admin.api.ts
import apiClient from "./client";
import { extractData } from "./helpers";
import type { ApiResponse, PaginationResponse } from "@/types/api.types";
import type {
  CertificateStatsResponse,
  AdminCertificateResponse,
  AdminCertificateDetailResponse,
  CertificateVerifyResponse,
  CertificateSearchParams,
  RevokeRequest,
} from "@/types/admin.types";

// ============================================
// GET CERTIFICATE STATS
// GET /api/admin/certificates/stats
// ============================================
export async function getCertificateStats(): Promise<CertificateStatsResponse> {
  const response = await apiClient.get<ApiResponse<CertificateStatsResponse>>(
    "/api/admin/certificates/stats",
  );
  return extractData(response);
}

// ============================================
// GET RECENT CERTIFICATES
// GET /api/certificates/recent
//
// Returns: Paginated list of recently issued certificates
// ============================================
export async function getRecentCertificates(
  page = 1,
  limit = 10,
): Promise<PaginationResponse<AdminCertificateResponse>> {
  const response = await apiClient.get<
    ApiResponse<PaginationResponse<AdminCertificateResponse>>
  >("/api/certificates/recent", { params: { page, limit } });
  return extractData(response);
}

// ============================================
// SEARCH CERTIFICATES
// GET /api/certificates/search
// ============================================
export async function searchCertificates(
  params: CertificateSearchParams,
): Promise<PaginationResponse<AdminCertificateResponse>> {
  const response = await apiClient.get<
    ApiResponse<PaginationResponse<AdminCertificateResponse>>
  >("/api/certificates/search", { params });
  return extractData(response);
}

// ============================================
// GET CERTIFICATE DETAIL
// GET /api/certificates/{certificateId}
// ============================================
export async function getCertificateDetail(
  certificateId: string,
): Promise<AdminCertificateDetailResponse> {
  const response = await apiClient.get<
    ApiResponse<AdminCertificateDetailResponse>
  >(`/api/certificates/${certificateId}`);
  return extractData(response);
}

// ============================================
// VERIFY CERTIFICATE
// POST /api/certificates/{certificateId}/verify
//
// [ACTION] Xác minh certificate với blockchain
// ============================================
export async function verifyCertificate(
  certificateId: string,
): Promise<CertificateVerifyResponse> {
  const response = await apiClient.post<ApiResponse<CertificateVerifyResponse>>(
    `/api/certificates/${certificateId}/verify`,
  );
  return extractData(response);
}

// ============================================
// REVOKE CERTIFICATE
// POST /api/certificates/{certificateId}/revoke
//
// [DESTRUCTIVE] Thu hồi certificate
// [IMPORTANT] Không thể hoàn tác
// ============================================
export async function revokeCertificate(
  certificateId: string,
  data: RevokeRequest,
): Promise<void> {
  await apiClient.post(`/api/certificates/${certificateId}/revoke`, data);
}
```

---

## 11. Module: Admin Users

### 11.1. Types

```typescript
// src/types/admin.types.ts (thêm vào)

export interface UserListResponse {
  userId: string;
  email: string;
  name: string;
  avatarUrl: string | null;
  role: RoleType;
  isActive: boolean;
  createdAt: string;
  lastLoginAt: string | null;

  // Stats
  enrollmentCount?: number; // For students
  classCount?: number; // For teachers
}

// ============================================
// REQUEST DTOs
// ============================================

export interface UpdateUserStatusRequest {
  isActive: boolean; // true = activate, false = deactivate
}

export interface UpdateUserRoleRequest {
  role: RoleType; // [ADMIN ONLY] Change user role
}

// ============================================
// QUERY PARAMS
// ============================================

export interface UserSearchParams {
  keyword?: string; // Search by name, email
  role?: RoleType;
  isActive?: boolean;
  page?: number;
  limit?: number;
  sort?: "newest" | "oldest" | "name";
}
```

### 11.2. Admin User API

```typescript
// src/api/admin.api.ts (thêm vào)

// ============================================
// GET USERS LIST
// GET /api/admin/users
// ============================================
export async function getUsers(
  params?: UserSearchParams,
): Promise<PaginationResponse<UserListResponse>> {
  const response = await apiClient.get<
    ApiResponse<PaginationResponse<UserListResponse>>
  >("/api/admin/users", { params });
  return extractData(response);
}

// ============================================
// UPDATE USER STATUS (Activate/Deactivate)
// PUT /api/admin/users/{userId}/status
//
// [IMPORTANT] Không thể deactivate chính mình
// [IMPORTANT] Deactivated user không thể login
// ============================================
export async function updateUserStatus(
  userId: string,
  data: UpdateUserStatusRequest,
): Promise<void> {
  await apiClient.put(`/api/admin/users/${userId}/status`, data);
}

// ============================================
// UPDATE USER ROLE
// PUT /api/admin/users/{userId}/role
//
// [IMPLEMENTED] Đổi role của user (STUDENT → TEACHER, etc.)
// Response trả về previousRole và newRole
// ============================================
export async function updateUserRole(
  userId: string,
  data: UpdateUserRoleRequest,
): Promise<UpdateUserRoleResponse> {
  const response = await apiClient.put<ApiResponse<UpdateUserRoleResponse>>(
    `/api/admin/users/${userId}/role`,
    data,
  );
  return extractData(response);
}

// Types
interface UpdateUserRoleRequest {
  role: "STUDENT" | "TEACHER" | "ADMIN";
}

interface UpdateUserRoleResponse {
  userId: string;
  previousRole: string;
  newRole: string;
  updatedAt: string;
}
```

---

## 12. Router Guards & Permission Matrix

### 12.1. Route Definitions

```typescript
// src/router/routes/index.ts
import type { RouteRecordRaw } from "vue-router";

export const routes: RouteRecordRaw[] = [
  // ============================================
  // PUBLIC ROUTES (No auth required)
  // ============================================
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/auth/LoginPage.vue"),
    meta: { requiresAuth: false },
  },
  {
    path: "/oauth/callback",
    name: "OAuthCallback",
    component: () => import("@/views/auth/OAuthCallback.vue"),
    meta: { requiresAuth: false },
  },
  {
    path: "/access-denied",
    name: "AccessDenied",
    component: () => import("@/views/errors/AccessDenied.vue"),
    meta: { requiresAuth: false },
  },

  // ============================================
  // STUDENT ROUTES
  // Requires: STUDENT role
  // [REDIRECT] Non-students → AccessDenied
  // ============================================
  {
    path: "/student",
    meta: { requiresAuth: true, allowedRoles: ["STUDENT"] },
    children: [
      {
        path: "dashboard",
        name: "StudentDashboard",
        component: () => import("@/views/student/DashboardPage.vue"),
      },
      {
        path: "courses/:classId",
        name: "CourseDetail",
        component: () => import("@/views/student/CourseDetailPage.vue"),
      },
      {
        path: "quizzes/:quizId",
        name: "QuizPage",
        component: () => import("@/views/student/QuizPage.vue"),
      },
      {
        path: "certificates",
        name: "StudentCertificates",
        component: () => import("@/views/student/CertificatesPage.vue"),
      },
    ],
  },

  // ============================================
  // TEACHER ROUTES
  // Requires: TEACHER role
  // [REDIRECT] Non-teachers → AccessDenied
  // ============================================
  {
    path: "/teacher",
    meta: { requiresAuth: true, allowedRoles: ["TEACHER"] },
    children: [
      {
        path: "classes",
        name: "TeacherClasses",
        component: () => import("@/views/teacher/ClassListPage.vue"),
      },
      {
        path: "classes/:classId",
        name: "ClassDetail",
        component: () => import("@/views/teacher/ClassDetailPage.vue"),
      },
      {
        path: "classes/:classId/quizzes",
        name: "QuizList",
        component: () => import("@/views/teacher/QuizListPage.vue"),
      },
      {
        path: "quizzes/create",
        name: "CreateQuiz",
        component: () => import("@/views/teacher/QuizFormPage.vue"),
      },
      {
        path: "quizzes/:quizId/edit",
        name: "EditQuiz",
        component: () => import("@/views/teacher/QuizFormPage.vue"),
      },
      {
        path: "quizzes/:quizId/submissions",
        name: "QuizSubmissions",
        component: () => import("@/views/teacher/QuizSubmissionsPage.vue"),
      },
    ],
  },

  // ============================================
  // ADMIN ROUTES
  // Requires: ADMIN role
  // [REDIRECT] Non-admins → AccessDenied
  // ============================================
  {
    path: "/admin",
    meta: { requiresAuth: true, allowedRoles: ["ADMIN"] },
    children: [
      {
        path: "certificates",
        name: "AdminCertificates",
        component: () => import("@/views/admin/CertificateDashboard.vue"),
      },
      {
        path: "certificates/:certificateId",
        name: "AdminCertificateDetail",
        component: () => import("@/views/admin/CertificateDetailPage.vue"),
      },
      {
        path: "users",
        name: "AdminUsers",
        component: () => import("@/views/admin/UserManagementPage.vue"),
      },
    ],
  },

  // ============================================
  // CATCH-ALL & HOME
  // ============================================
  {
    path: "/",
    name: "Home",
    redirect: (to) => {
      // [PLACEHOLDER] Redirect based on role after implementing auth check
      return { name: "Login" };
    },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/errors/NotFound.vue"),
  },
];
```

### 12.2. Navigation Guards

```typescript
// src/router/guards.ts
import type {
  NavigationGuardWithThis,
  RouteLocationNormalized,
} from "vue-router";
import { useAuthStore } from "@/stores/auth.store";
import type { RoleType } from "@/types/auth.types";

// Extend route meta type
declare module "vue-router" {
  interface RouteMeta {
    requiresAuth?: boolean;
    allowedRoles?: RoleType[];
  }
}

/**
 * Main navigation guard
 * Handles authentication and role-based access
 */
export const authGuard: NavigationGuardWithThis<undefined> = async (
  to,
  from,
  next,
) => {
  const authStore = useAuthStore();

  // ============================================
  // STEP 1: Wait for auth initialization
  // ============================================
  if (!authStore.isInitialized) {
    await authStore.initAuth();
  }

  const requiresAuth = to.meta.requiresAuth !== false;
  const allowedRoles = to.meta.allowedRoles as RoleType[] | undefined;

  // ============================================
  // STEP 2: Check authentication
  // [REDIRECT] Unauthenticated → Login page
  // ============================================
  if (requiresAuth && !authStore.isAuthenticated) {
    return next({
      name: "Login",
      query: {
        redirect: to.fullPath,
        reason: "auth_required",
      },
    });
  }

  // ============================================
  // STEP 3: Check authorization (role-based)
  // [REDIRECT] Unauthorized role → AccessDenied page
  // ============================================
  if (allowedRoles && allowedRoles.length > 0) {
    const userRole = authStore.userRole;

    if (!userRole || !allowedRoles.includes(userRole)) {
      return next({
        name: "AccessDenied",
        query: {
          attemptedPath: to.fullPath,
          requiredRole: allowedRoles.join(","),
          currentRole: userRole || "none",
        },
      });
    }
  }

  // ============================================
  // STEP 4: Check if user is active
  // [REDIRECT] Inactive user → Login with message
  // ============================================
  if (requiresAuth && authStore.user && !authStore.user.isActive) {
    authStore.clearAuth();
    return next({
      name: "Login",
      query: { reason: "account_disabled" },
    });
  }

  // ============================================
  // STEP 5: Redirect authenticated users away from login
  // ============================================
  if (to.name === "Login" && authStore.isAuthenticated) {
    return next(getRoleBasedRedirect(authStore.userRole));
  }

  next();
};

/**
 * Get default route for each role
 */
function getRoleBasedRedirect(role: RoleType | null): { name: string } {
  switch (role) {
    case "STUDENT":
      return { name: "StudentDashboard" };
    case "TEACHER":
      return { name: "TeacherClasses" };
    case "ADMIN":
      return { name: "AdminCertificates" };
    default:
      return { name: "Login" };
  }
}
```

### 12.3. Router Setup

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from "vue-router";
import { routes } from "./routes";
import { authGuard } from "./guards";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// Apply global guard
router.beforeEach(authGuard);

// ============================================
// [OPTIONAL] After each navigation
// Reset scroll position, update page title, etc.
// ============================================
router.afterEach((to) => {
  // Reset scroll
  window.scrollTo(0, 0);

  // [PLACEHOLDER] Update page title
  // document.title = `${to.meta.title || 'LMS'} | Learning Platform`
});

export default router;
```

### 12.4. Permission Matrix Summary

| Route Pattern               | Required Role | Redirect on Fail                   |
| --------------------------- | ------------- | ---------------------------------- |
| `/login`, `/oauth/callback` | None (public) | If authenticated → role-based home |
| `/student/**`               | `STUDENT`     | → `/access-denied`                 |
| `/teacher/**`               | `TEACHER`     | → `/access-denied`                 |
| `/admin/**`                 | `ADMIN`       | → `/access-denied`                 |
| `/**` (catch-all)           | Authenticated | → `/login?redirect=...`            |

---

## 13. Error Handling Strategy

### 13.1. API Error Types

```typescript
// src/types/error.types.ts

export interface ApiError {
  status: number;
  message: string;
  code?: string;
  field?: string; // For validation errors
}

export const ERROR_CODES = {
  // Authentication
  TOKEN_EXPIRED: "TOKEN_EXPIRED",
  TOKEN_INVALID: "TOKEN_INVALID",

  // Authorization
  ACCESS_DENIED: "ACCESS_DENIED",
  NOT_ENROLLED: "NOT_ENROLLED",
  NOT_OWNER: "NOT_OWNER",

  // Resource
  NOT_FOUND: "NOT_FOUND",
  ALREADY_EXISTS: "ALREADY_EXISTS",

  // Business logic
  QUIZ_NOT_PUBLISHED: "QUIZ_NOT_PUBLISHED",
  MAX_ATTEMPTS_REACHED: "MAX_ATTEMPTS_REACHED",
  CERTIFICATE_ALREADY_ISSUED: "CERTIFICATE_ALREADY_ISSUED",
  CERTIFICATE_REVOKED: "CERTIFICATE_REVOKED",

  // Validation
  VALIDATION_ERROR: "VALIDATION_ERROR",
} as const;
```

### 13.2. Error Handling Composable

```typescript
// src/composables/useApiError.ts
import { ref } from "vue";
import type { AxiosError } from "axios";

interface ApiErrorResponse {
  success: false;
  data: null;
  error: string;
}

export function useApiError() {
  const error = ref<string | null>(null);
  const isError = ref(false);

  /**
   * Clear current error
   */
  function clearError() {
    error.value = null;
    isError.value = false;
  }

  /**
   * Handle and extract error message from API response
   */
  function handleError(err: unknown): string {
    isError.value = true;

    if (err instanceof Error && "response" in err) {
      const axiosError = err as AxiosError<ApiErrorResponse>;

      // Extract error message from response
      if (axiosError.response?.data?.error) {
        error.value = axiosError.response.data.error;
        return error.value;
      }

      // HTTP status-based messages
      switch (axiosError.response?.status) {
        case 400:
          error.value = "Dữ liệu không hợp lệ";
          break;
        case 401:
          error.value = "Phiên đăng nhập đã hết hạn";
          break;
        case 403:
          error.value = "Bạn không có quyền thực hiện thao tác này";
          break;
        case 404:
          error.value = "Không tìm thấy dữ liệu";
          break;
        case 500:
          error.value = "Lỗi hệ thống. Vui lòng thử lại sau";
          break;
        default:
          error.value = "Đã có lỗi xảy ra";
      }
    } else if (err instanceof Error) {
      error.value = err.message;
    } else {
      error.value = "Đã có lỗi không xác định";
    }

    return error.value;
  }

  /**
   * Wrap async function with error handling
   */
  async function withErrorHandling<T>(
    fn: () => Promise<T>,
    options?: {
      onError?: (msg: string) => void;
      rethrow?: boolean;
    },
  ): Promise<T | null> {
    clearError();

    try {
      return await fn();
    } catch (err) {
      const msg = handleError(err);
      options?.onError?.(msg);

      if (options?.rethrow) throw err;
      return null;
    }
  }

  return {
    error,
    isError,
    clearError,
    handleError,
    withErrorHandling,
  };
}
```

### 13.3. Usage Example

```vue
<script setup lang="ts">
import { useApiError } from "@/composables/useApiError";
import { getCourseDetail } from "@/api/student.api";

const { error, isError, withErrorHandling } = useApiError();
const course = ref(null);

onMounted(async () => {
  course.value = await withErrorHandling(() => getCourseDetail(classId.value), {
    onError: (msg) => {
      // [PLACEHOLDER] Show toast notification
      console.error("Failed to load course:", msg);
    },
  });
});
</script>

<template>
  <ErrorMessage v-if="isError" :message="error" />
  <CourseContent v-else-if="course" :course="course" />
</template>
```

---

## 14. Placeholder & TODO Checklist

### 14.1. Authentication & Authorization

| #   | Item                         | Status          | Notes                                         |
| --- | ---------------------------- | --------------- | --------------------------------------------- |
| 1   | OAuth redirect path tracking | `[PLACEHOLDER]` | Lưu path trước khi redirect đến Google        |
| 2   | Token refresh mechanism      | `[TODO]`        | BE chưa có endpoint refresh token             |
| 3   | Remember me functionality    | `[TODO]`        | Cân nhắc token expiry khác nhau               |
| 4   | Multi-tab logout sync        | `[TODO]`        | Dùng BroadcastChannel hoặc localStorage event |

### 14.2. Redirects Summary

| Trigger                    | Destination      | Query Params                         |
| -------------------------- | ---------------- | ------------------------------------ |
| 401 Unauthorized           | `/login`         | `redirect`, `reason=session_expired` |
| 403 Forbidden              | `/access-denied` | `attemptedPath`                      |
| After OAuth success        | Role-based home  | None                                 |
| Inactive user              | `/login`         | `reason=account_disabled`            |
| Direct access without auth | `/login`         | `redirect`, `reason=auth_required`   |

### 14.3. Missing Endpoints (Verify with BE)

| #   | Endpoint                         | Purpose                 | Status          |
| --- | -------------------------------- | ----------------------- | --------------- |
| 1   | `PUT /api/admin/users/{id}/role` | Change user role        | `[IMPLEMENTED]` |
| 2   | `GET /api/courses`               | Course catalog (public) | `[IMPLEMENTED]` |
| 3   | `POST /api/enrollments`          | Enroll student          | `[IMPLEMENTED]` |
| 4   | `DELETE /api/enrollments/{id}`   | Drop enrollment         | `[IMPLEMENTED]` |
| 5   | `GET /api/quizzes/{id}/result`   | Get quiz result detail  | `[IMPLEMENTED]` |

> **Note**: Tất cả 5 endpoints đã được implement (19/03/2026). Xem chi tiết tại `API-ENDPOINTS-AND-TEST-STRATEGY.md`.

### 14.4. UI Components to Implement

| #   | Component               | Location             | Priority |
| --- | ----------------------- | -------------------- | -------- |
| 1   | `LoadingSpinner.vue`    | `components/common/` | High     |
| 2   | `ErrorMessage.vue`      | `components/common/` | High     |
| 3   | `Pagination.vue`        | `components/common/` | Medium   |
| 4   | `ConfirmModal.vue`      | `components/common/` | Medium   |
| 5   | `ToastNotification.vue` | `components/common/` | Medium   |
| 6   | `RoleBasedNav.vue`      | `components/layout/` | High     |
| 7   | `QuizTimer.vue`         | `components/quiz/`   | High     |
| 8   | `QuestionForm.vue`      | `components/quiz/`   | Medium   |

### 14.5. Business Logic Validations (Frontend)

| #   | Validation                        | Location                  | Notes                                      |
| --- | --------------------------------- | ------------------------- | ------------------------------------------ |
| 1   | `classId` belongs to current user | Course/Class detail pages | Show error if not owner/enrolled           |
| 2   | Quiz can be started               | Quiz page                 | Check `displayStatus`, `remainingAttempts` |
| 3   | Class dates are valid             | Create/Edit class form    | `endDate > startDate`                      |
| 4   | Quiz has questions                | Quiz form                 | Don't allow publish with 0 questions       |
| 5   | Self-deactivation prevention      | User management           | Don't allow admin to deactivate self       |

### 14.6. Date/Time Handling

```typescript
// src/utils/date.ts

/**
 * Format date from ISO string to Vietnamese locale
 *
 * [IMPORTANT] Backend trả về UTC ISO string
 * Frontend cần convert sang timezone local
 */
export function formatDate(isoString: string | null): string {
  if (!isoString) return "—";

  const date = new Date(isoString);
  return date.toLocaleDateString("vi-VN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}

export function formatDateTime(isoString: string | null): string {
  if (!isoString) return "—";

  const date = new Date(isoString);
  return date.toLocaleString("vi-VN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

/**
 * Format date for API request
 * [IMPORTANT] Gửi format YYYY-MM-DD
 */
export function toApiDate(date: Date): string {
  return date.toISOString().split("T")[0];
}
```

---

## Appendix: Quick Reference

### A. HTTP Status Codes & FE Actions

| Status | Meaning      | Frontend Action                    |
| ------ | ------------ | ---------------------------------- |
| 200    | OK           | Display data                       |
| 201    | Created      | Show success toast, update list    |
| 400    | Bad Request  | Show validation error              |
| 401    | Unauthorized | Clear auth, redirect to login      |
| 403    | Forbidden    | Redirect to access denied          |
| 404    | Not Found    | Show not found message             |
| 500    | Server Error | Show generic error, log to console |

### B. API Response Format

```typescript
// Success
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "error": null
}

// Error
{
  "success": false,
  "data": null,
  "error": "Error description"
}
```

### C. Role-Based Default Routes

| Role    | Default Route     | Path                  |
| ------- | ----------------- | --------------------- |
| STUDENT | StudentDashboard  | `/student/dashboard`  |
| TEACHER | TeacherClasses    | `/teacher/classes`    |
| ADMIN   | AdminCertificates | `/admin/certificates` |

### D. File Naming Conventions

| Type          | Pattern              | Example              |
| ------------- | -------------------- | -------------------- |
| API file      | `{domain}.api.ts`    | `student.api.ts`     |
| Type file     | `{domain}.types.ts`  | `student.types.ts`   |
| Store         | `{domain}.store.ts`  | `auth.store.ts`      |
| Vue page      | `{Name}Page.vue`     | `DashboardPage.vue`  |
| Vue component | `{Name}.vue`         | `LoadingSpinner.vue` |
| Composable    | `use{Name}.ts`       | `useAuth.ts`         |
| Route file    | `{domain}.routes.ts` | `student.routes.ts`  |

---

> **Document Version**: 1.0
> **Last Updated**: 19/03/2026
> **Author**: Backend Team
> **For**: Frontend Team Integration
