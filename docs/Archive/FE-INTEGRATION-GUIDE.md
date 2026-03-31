# Frontend Integration Guide

> **Backend API**: `http://localhost:8080`
> **Ngày cập nhật**: 20/03/2026

---

## Mục lục

1. [Quick Start](#1-quick-start)
2. [Authentication Flow](#2-authentication-flow)
3. [API Response Format](#3-api-response-format)
4. [API Endpoints theo Role](#4-api-endpoints-theo-role)
5. [Error Handling](#5-error-handling)
6. [TypeScript Interfaces](#6-typescript-interfaces)
7. [Code Examples](#7-code-examples)

---

## 1. Quick Start

### Backend đang chạy?

```bash
curl http://localhost:8080/api/courses
```

Nếu trả về JSON → Backend OK.

### Cài đặt Axios (khuyến nghị)

```bash
npm install axios
```

### Base config

```typescript
// src/api/client.ts
import axios from "axios";

const apiClient = axios.create({
  baseURL: "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

// Auto attach token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 → redirect to login
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/auth/login";
    }
    return Promise.reject(error);
  },
);

export default apiClient;
```

---

## 2. Authentication Flow

### 2.1. Login với Google OAuth

```
┌─────────────────────────────────────────────────────────────┐
│  1. User click "Login with Google"                          │
│     → window.location.href = 'http://localhost:8080/oauth2/authorization/google'
│                                                             │
│  2. Google xác thực → redirect về Frontend                  │
│     → http://localhost:3000/oauth2/redirect?token=...&role=...&redirect=...
│                                                             │
│  3. Frontend xử lý callback                                 │
│     → Lưu token, role                                       │
│     → Redirect theo path                                    │
└─────────────────────────────────────────────────────────────┘
```

### 2.2. OAuth2 Redirect Handler

```vue
<!-- src/views/auth/OAuth2RedirectHandler.vue -->
<script setup lang="ts">
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuth } from "@/composables/useAuth";

const route = useRoute();
const router = useRouter();
const auth = useAuth();

onMounted(() => {
  const token = route.query.token as string;
  const role = route.query.role as string;
  const redirect = route.query.redirect as string;
  const error = route.query.error as string;

  if (error) {
    console.error("OAuth error:", error);
    router.push("/auth/login?error=" + error);
    return;
  }

  if (token && role && redirect) {
    // Lưu token và role
    auth.setToken(token);
    auth.setRole(role);

    // Redirect theo path từ backend
    router.push(redirect);
  } else {
    router.push("/auth/login?error=missing_params");
  }
});
</script>
```

### 2.3. useAuth composable

```typescript
// src/composables/useAuth.ts
import { ref, computed } from "vue";
import apiClient from "@/api/client";

const token = ref<string | null>(localStorage.getItem("token"));
const role = ref<string | null>(localStorage.getItem("role"));
const user = ref<User | null>(null);

export function useAuth() {
  const isAuthenticated = computed(() => !!token.value);
  const userRole = computed(() => role.value);

  function setToken(newToken: string) {
    token.value = newToken;
    localStorage.setItem("token", newToken);
  }

  function setRole(newRole: string) {
    role.value = newRole;
    localStorage.setItem("role", newRole);
  }

  async function fetchCurrentUser() {
    if (!token.value) return null;
    try {
      const { data } = await apiClient.get("/api/auth/me");
      user.value = data.data;
      return user.value;
    } catch {
      logout();
      return null;
    }
  }

  function logout() {
    token.value = null;
    role.value = null;
    user.value = null;
    localStorage.removeItem("token");
    localStorage.removeItem("role");
  }

  function loginWithGoogle() {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  }

  return {
    token,
    user,
    isAuthenticated,
    userRole,
    setToken,
    setRole,
    fetchCurrentUser,
    logout,
    loginWithGoogle,
  };
}
```

### 2.4. Route Guards

```typescript
// src/router/index.ts
router.beforeEach(async (to, from, next) => {
  const auth = useAuth();
  const requiresAuth = to.meta.requiresAuth ?? false;
  const requiredRoles = (to.meta.roles as string[]) || [];

  // Route yêu cầu đăng nhập
  if (requiresAuth) {
    if (!auth.isAuthenticated.value) {
      next({ name: "login" });
      return;
    }

    // Kiểm tra role
    if (requiredRoles.length > 0) {
      if (!requiredRoles.includes(auth.userRole.value!)) {
        next({ name: "accessDenied" });
        return;
      }
    }
  }

  next();
});
```

---

## 3. API Response Format

### Success Response

```typescript
interface ApiResponse<T> {
  success: true;
  message?: string;
  data: T;
  error: null;
}
```

### Error Response

```typescript
interface ApiErrorResponse {
  success: false;
  message?: string;
  data: null;
  error: string;
}
```

### Wrapper utility

```typescript
// src/api/wrapper.ts
import apiClient from "./client";
import type { AxiosError } from "axios";

interface ApiResult<T> {
  data: T | null;
  error: string | null;
}

export async function apiCall<T>(
  request: () => Promise<{ data: { data: T } }>,
): Promise<ApiResult<T>> {
  try {
    const response = await request();
    return { data: response.data.data, error: null };
  } catch (err) {
    const axiosError = err as AxiosError<{ error: string }>;
    const errorMessage = axiosError.response?.data?.error || "Unknown error";
    return { data: null, error: errorMessage };
  }
}

// Usage:
// const { data, error } = await apiCall(() => apiClient.get('/api/auth/me'));
```

---

## 4. API Endpoints theo Role

### PUBLIC (không cần token)

| Method | Endpoint                       | Mô tả                |
| ------ | ------------------------------ | -------------------- |
| GET    | `/api/courses`                 | Danh sách khóa học   |
| GET    | `/oauth2/authorization/google` | Bắt đầu login Google |

### STUDENT

| Method | Endpoint                                | Mô tả                        |
| ------ | --------------------------------------- | ---------------------------- |
| GET    | `/api/auth/me`                          | Thông tin user hiện tại      |
| GET    | `/api/student/{studentId}/courses`      | Khóa học đã đăng ký          |
| GET    | `/api/student/{studentId}/certificates` | Chứng chỉ của student        |
| GET    | `/api/courses/{courseId}`               | Chi tiết khóa học + progress |
| GET    | `/api/quizzes/{quizId}`                 | Chi tiết quiz (có câu hỏi)   |
| POST   | `/api/quizzes/{quizId}/submit`          | Nộp bài quiz                 |
| GET    | `/api/quizzes/{quizId}/result`          | Xem kết quả quiz             |

### TEACHER

| Method | Endpoint                            | Mô tả                        |
| ------ | ----------------------------------- | ---------------------------- |
| GET    | `/api/teacher/{teacherId}/classes`  | Danh sách lớp của teacher    |
| GET    | `/api/classes/{classId}`            | Chi tiết lớp                 |
| GET    | `/api/classes/{classId}/students`   | Danh sách học sinh trong lớp |
| POST   | `/api/classes`                      | Tạo lớp mới                  |
| PUT    | `/api/classes/{classId}`            | Cập nhật lớp                 |
| GET    | `/api/classes/{classId}/quizzes`    | Danh sách quiz của lớp       |
| POST   | `/api/quizzes`                      | Tạo quiz mới                 |
| PUT    | `/api/quizzes/{quizId}`             | Cập nhật quiz                |
| DELETE | `/api/quizzes/{quizId}`             | Xóa quiz (soft delete)       |
| GET    | `/api/quizzes/{quizId}/submissions` | Xem bài nộp của học sinh     |
| POST   | `/api/enrollments`                  | Đăng ký học sinh vào lớp     |
| DELETE | `/api/enrollments/{id}`             | Hủy đăng ký                  |

### ADMIN

| Method | Endpoint                         | Mô tả                    |
| ------ | -------------------------------- | ------------------------ |
| GET    | `/api/admin/certificates/stats`  | Thống kê chứng chỉ       |
| GET    | `/api/certificates/recent`       | Chứng chỉ gần đây        |
| GET    | `/api/certificates/search?q=...` | Tìm kiếm chứng chỉ       |
| GET    | `/api/certificates/{id}`         | Chi tiết chứng chỉ       |
| POST   | `/api/certificates/{id}/verify`  | Xác minh trên blockchain |
| POST   | `/api/certificates/{id}/revoke`  | Thu hồi chứng chỉ        |
| GET    | `/api/admin/users`               | Danh sách users          |
| PUT    | `/api/admin/users/{id}/status`   | Khóa/mở tài khoản        |
| PUT    | `/api/admin/users/{id}/role`     | Thay đổi role            |

---

## 5. Error Handling

### HTTP Status Codes

| Code | Ý nghĩa      | Xử lý Frontend                          |
| ---- | ------------ | --------------------------------------- |
| 200  | OK           | Hiển thị data                           |
| 201  | Created      | Hiển thị success message + refresh list |
| 400  | Bad Request  | Hiển thị validation errors              |
| 401  | Unauthorized | Redirect → login                        |
| 403  | Forbidden    | Hiển thị "Không có quyền"               |
| 404  | Not Found    | Hiển thị "Không tìm thấy"               |
| 500  | Server Error | Hiển thị "Lỗi hệ thống"                 |

### Error handling example

```typescript
async function submitQuiz(quizId: string, answers: Answer[]) {
  try {
    const { data } = await apiClient.post(`/api/quizzes/${quizId}/submit`, {
      studentId: currentUser.value.userId,
      answers,
    });

    toast.success(`Điểm: ${data.data.score}/${data.data.maxScore}`);
    return data.data;
  } catch (err) {
    const error = err as AxiosError<{ error: string }>;

    switch (error.response?.status) {
      case 400:
        toast.error(error.response.data.error || "Dữ liệu không hợp lệ");
        break;
      case 403:
        toast.error("Bạn không có quyền làm bài này");
        break;
      case 404:
        toast.error("Quiz không tồn tại");
        break;
      default:
        toast.error("Có lỗi xảy ra, vui lòng thử lại");
    }
    throw err;
  }
}
```

---

## 6. TypeScript Interfaces

```typescript
// src/types/api.ts

// ============ AUTH ============
interface User {
  userId: string;
  email: string;
  fullName: string;
  avatarUrl: string | null;
  role: "STUDENT" | "TEACHER" | "ADMIN";
  isActive: boolean;
}

// ============ COURSE ============
interface Course {
  courseId: string;
  courseCode: string;
  courseName: string;
  description?: string;
}

interface EnrolledCourse extends Course {
  courseIcon?: string;
  teacherName: string;
  progress: number;
  totalQuizzes: number;
  completedQuizzes: number;
  averageScore: number;
  isCompleted: boolean;
}

interface CourseDetail extends EnrolledCourse {
  startDate: string;
  endDate: string;
  studentName: string;
  quizzes: QuizSummary[];
  certificate: Certificate | null;
}

// ============ QUIZ ============
interface QuizSummary {
  id: string;
  name: string;
  score: number | null;
  maxScore: number;
  status: "completed" | "pending" | "locked";
}

interface Quiz {
  quizId: string;
  classId: string;
  quizName: string;
  duration: number;
  passingScore: number;
  maxScore: number;
  questionCount: number;
  completionRate?: number;
  averageScore?: number;
  status: "DRAFT" | "PUBLISHED" | "CLOSED";
  questions?: Question[];
}

interface Question {
  questionId: number;
  questionText: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
}

interface QuizSubmission {
  studentId: string;
  answers: {
    questionId: string;
    selectedOption: "A" | "B" | "C" | "D";
  }[];
}

interface QuizResult {
  score: number;
  maxScore: number;
  status: "passed" | "failed";
  submittedAt: string;
}

// ============ CLASS ============
interface ClassInfo {
  classId: string;
  classCode: string;
  courseName: string;
  courseId: string;
  teacherName: string;
  studentCount: number;
  quizCount: number;
  status: "ACTIVE" | "COMPLETED" | "CANCELED";
  startDate: string;
  endDate: string;
  description?: string;
}

interface StudentInClass {
  studentId: string;
  fullName: string;
  email: string;
  avatarUrl?: string;
  completedQuizzes: number;
  totalQuizzes: number;
  averageScore: number;
  status: "LEARNING" | "PASSED" | "FAILED" | "DROPPED";
  enrolledAt: string;
}

// ============ CERTIFICATE ============
interface Certificate {
  certificateId: string;
  studentName: string;
  studentEmail: string;
  className: string;
  courseCode: string;
  averageScore: number;
  issuedAt: string;
  status: "PENDING" | "ISSUED" | "REVOKED";
  verificationHash: string;
  blockchainInfo?: BlockchainInfo;
}

interface BlockchainInfo {
  transactionHash: string;
  blockNumber: string;
  contractAddress: string;
  networkName?: string;
  explorerUrl?: string;
}

// ============ PAGINATION ============
interface Pagination {
  page: number;
  limit: number;
  total: number;
  totalPages: number;
}

interface PaginatedResponse<T> {
  items: T[];
  pagination: Pagination;
}
```

---

## 7. Code Examples

### 7.1. Student Dashboard

```vue
<script setup lang="ts">
import { ref, onMounted } from "vue";
import apiClient from "@/api/client";
import { useAuth } from "@/composables/useAuth";

const auth = useAuth();
const courses = ref<EnrolledCourse[]>([]);
const certificates = ref<Certificate[]>([]);
const loading = ref(true);

onMounted(async () => {
  const user = await auth.fetchCurrentUser();
  if (!user) return;

  const [coursesRes, certsRes] = await Promise.all([
    apiClient.get(`/api/student/${user.userId}/courses`),
    apiClient.get(`/api/student/${user.userId}/certificates`),
  ]);

  courses.value = coursesRes.data.data;
  certificates.value = certsRes.data.data;
  loading.value = false;
});
</script>
```

### 7.2. Teacher - Tạo Quiz

```typescript
async function createQuiz(classId: string, quizData: CreateQuizDTO) {
  const payload = {
    classId,
    quizName: quizData.title,
    duration: quizData.duration,
    passingScore: quizData.passingScore,
    maxScore: 10,
    questions: quizData.questions.map((q) => ({
      questionText: q.text,
      questionType: "SINGLE_CHOICE",
      options: [
        { optionText: q.optionA, isCorrect: q.correctAnswer === "A" },
        { optionText: q.optionB, isCorrect: q.correctAnswer === "B" },
        { optionText: q.optionC, isCorrect: q.correctAnswer === "C" },
        { optionText: q.optionD, isCorrect: q.correctAnswer === "D" },
      ],
    })),
  };

  const { data } = await apiClient.post("/api/quizzes", payload);
  return data.data;
}
```

### 7.3. Student - Làm Quiz

```vue
<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import apiClient from "@/api/client";
import { useAuth } from "@/composables/useAuth";

const route = useRoute();
const router = useRouter();
const auth = useAuth();

const quiz = ref<Quiz | null>(null);
const answers = ref<Record<string, string>>({});
const submitting = ref(false);

onMounted(async () => {
  const quizId = route.params.quizId as string;
  const { data } = await apiClient.get(`/api/quizzes/${quizId}`);
  quiz.value = data.data;
});

async function submitQuiz() {
  if (!quiz.value) return;

  submitting.value = true;
  const user = auth.user.value!;

  try {
    const { data } = await apiClient.post(
      `/api/quizzes/${quiz.value.quizId}/submit`,
      {
        studentId: user.userId,
        answers: Object.entries(answers.value).map(([qId, option]) => ({
          questionId: qId,
          selectedOption: option,
        })),
      },
    );

    const result = data.data as QuizResult;

    if (result.status === "passed") {
      toast.success(
        `Chúc mừng! Bạn đạt ${result.score}/${result.maxScore} điểm`,
      );
    } else {
      toast.warning(
        `Bạn đạt ${result.score}/${result.maxScore} điểm. Chưa đạt yêu cầu.`,
      );
    }

    router.push(`/student/course/${route.query.courseId}`);
  } finally {
    submitting.value = false;
  }
}
</script>
```

### 7.4. Admin - Quản lý Users

```vue
<script setup lang="ts">
import { ref, onMounted } from "vue";
import apiClient from "@/api/client";

const users = ref<User[]>([]);
const pagination = ref<Pagination | null>(null);
const filters = ref({ role: "", status: "" });

async function fetchUsers(page = 1) {
  const params = new URLSearchParams({
    page: String(page),
    limit: "20",
  });

  if (filters.value.role) params.append("role", filters.value.role);
  if (filters.value.status) params.append("status", filters.value.status);

  const { data } = await apiClient.get(`/api/admin/users?${params}`);
  users.value = data.data.items;
  pagination.value = data.data.pagination;
}

async function toggleUserStatus(user: User) {
  await apiClient.put(`/api/admin/users/${user.userId}/status`, {
    isActive: !user.isActive,
    reason: user.isActive ? "Admin disabled" : undefined,
  });

  user.isActive = !user.isActive;
  toast.success(`Đã ${user.isActive ? "mở" : "khóa"} tài khoản`);
}

async function changeRole(userId: string, newRole: string) {
  await apiClient.put(`/api/admin/users/${userId}/role`, { role: newRole });
  await fetchUsers();
  toast.success("Đã cập nhật role");
}

onMounted(() => fetchUsers());
</script>
```

---

## Checklist Frontend

- [ ] Axios client với interceptors (token, 401 handling)
- [ ] OAuth2 redirect handler
- [ ] useAuth composable
- [ ] Route guards theo role
- [ ] Error handling với toast notifications
- [ ] TypeScript interfaces cho API responses
- [ ] Loading states cho async operations
- [ ] Pagination component cho lists

---

## Liên hệ

- **Backend repo**: [GitHub link]
- **API docs chi tiết**: `docs/API-ENDPOINTS-AND-TEST-STRATEGY.md`
- **Postman collection**: [Link]
