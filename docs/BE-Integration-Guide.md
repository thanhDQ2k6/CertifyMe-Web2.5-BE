# Backend Integration Guide

Hướng dẫn từng bước tích hợp Frontend (Vue 3) với Backend API.

---

## Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Cấu hình API Base URL](#2-cấu-hình-api-base-url)
3. [Tích hợp Authentication (Google OAuth)](#3-tích-hợp-authentication-google-oauth)
4. [Tích hợp Student API](#4-tích-hợp-student-api)
5. [Tích hợp Teacher API](#5-tích-hợp-teacher-api)
6. [Tích hợp Admin API](#6-tích-hợp-admin-api)
7. [Xóa mock data](#7-xóa-mock-data)
8. [Bật lại Route Guards](#8-bật-lại-route-guards)
9. [API Response Format](#9-api-response-format)
10. [Xử lý lỗi](#10-xử-lý-lỗi)

---

## 1. Tổng quan kiến trúc

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│   Vue Views  │────▸│   Composables    │────▸│   Services   │
│  (UI Layer)  │     │ (State Manager)  │     │ (API Layer)  │
└──────────────┘     └──────────────────┘     └──────┬───────┘
                                                     │
                                              ┌──────▼───────┐
                                              │  apiFetcher  │
                                              │ (axios inst) │
                                              └──────┬───────┘
                                                     │
                                              ┌──────▼───────┐
                                              │  Backend API │
                                              └──────────────┘
```

**Luồng dữ liệu:**
- **Views** gọi hàm từ **Composables** (vd: `useStudent().fetchCourses()`)
- **Composables** gọi **Services** (vd: `CourseService.getStudentCourses()`)
- **Services** dùng `apiFetcher` (wrapper axios) để gọi REST API
- Axios interceptor tự thêm JWT token vào header `Authorization`

---

## 2. Cấu hình API Base URL

### Bước 1: Tạo file `.env`

```bash
# .env (root project)
VITE_API_BASE_URL=http://localhost:3000/api
```

### Bước 2: Cấu hình cho các môi trường

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:3000/api

# .env.staging
VITE_API_BASE_URL=https://staging-api.certifyme.fpt.edu.vn/api

# .env.production
VITE_API_BASE_URL=https://api.certifyme.fpt.edu.vn/api
```

File `src/lib/apiFetcher/axiosInstance.js` đã đọc biến này:

```js
const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000/api',
    timeout: 10000,
    headers: { 'Content-Type': 'application/json' }
});
```

---

## 3. Tích hợp Authentication (Google OAuth)

### BE cần cung cấp:

| Method | Endpoint | Request Body | Response |
|--------|----------|-------------|----------|
| POST | `/auth/google` | `{ credential: string }` | `{ token, user }` |
| GET | `/auth/refresh` | — | `{ token }` |

### Response format cho `/auth/google`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "u1",
    "name": "Nguyễn Văn A",
    "email": "annv@fpt.edu.vn",
    "role": "student",
    "avatar": "https://..."
  }
}
```

> **Lưu ý:** Trường `role` phải là một trong: `"student"`, `"teacher"`, `"admin"`.

### FE xử lý (đã sẵn):

- `AuthService.signInWithGoogle(credential)` — gọi POST `/auth/google`
- Token được lưu vào `localStorage` key `authToken`
- User info lưu vào `localStorage` key `authUser`
- Axios interceptor tự gắn `Authorization: Bearer <token>` cho mọi request

### Bước tích hợp:

1. BE triển khai endpoint `/auth/google` nhận Google credential, verify, trả JWT + user info
2. FE file `src/views/auth/LoginGoogle.vue` — hàm `handleGoogleLogin()` gọi `useAuth().login(credential)`
3. Sau login, FE redirect dựa trên `user.role`:
   - `student` → `/student/dashboard`
   - `teacher` → `/teacher/dashboard`
   - `admin` → `/admin/dashboard`

---

## 4. Tích hợp Student API

### BE cần cung cấp:

| Method | Endpoint | Mô tả | Response |
|--------|----------|-------|----------|
| GET | `/student/:studentId/courses` | Danh sách khóa học của SV | `Course[]` |
| GET | `/courses/:courseId` | Chi tiết khóa học + quiz | `CourseDetail` |
| GET | `/student/:studentId/certificates` | Danh sách chứng chỉ | `Certificate[]` |
| POST | `/quizzes/:quizId/submit` | Nộp bài quiz | `QuizResult` |

### Response format — `GET /student/:id/courses`:

```json
[
  {
    "courseId": "c1",
    "courseIcon": "☕",
    "courseName": "Lập trình Java 6",
    "courseCode": "SD18301",
    "teacherName": "Thầy Nguyễn Văn A",
    "progress": 80,
    "totalQuizzes": 5,
    "completedQuizzes": 4,
    "isCompleted": false,
    "averageScore": 8.5
  }
]
```

### Response format — `GET /courses/:courseId`:

```json
{
  "courseIcon": "☕",
  "courseName": "Lập trình Java 6",
  "courseCode": "SD18301",
  "teacherName": "Thầy Nguyễn Văn A",
  "startDate": "01/01/2026",
  "endDate": "01/04/2026",
  "progress": 80,
  "totalQuizzes": 5,
  "completedQuizzes": 4,
  "isCompleted": false,
  "studentName": "Trần Văn B",
  "averageScore": 8.5,
  "quizzes": [
    {
      "id": "q1",
      "name": "Lab 1: Biến và kiểu dữ liệu",
      "score": 8.5,
      "maxScore": 10,
      "status": "completed"
    },
    {
      "id": "q5",
      "name": "Lab 5: Collections",
      "score": null,
      "maxScore": 10,
      "status": "pending"
    }
  ],
  "certificate": {
    "verificationHash": "0x7a8b9c...",
    "blockchainInfo": {
      "hash": "0x7a8b9c...",
      "block": "12345678",
      "txHash": "0x999888...",
      "contract": "0xABC123..."
    }
  }
}
```

> `certificate` field chỉ có khi `isCompleted: true`. Quiz `status` là một trong: `"completed"`, `"pending"`, `"locked"`.

### File FE liên quan:

| File | Chức năng |
|------|-----------|
| `src/services/CourseService.js` | Gọi API |
| `src/composables/useStudent.js` | State management |
| `src/views/student/StudentDashboard.vue` | Hiển thị danh sách |
| `src/views/student/CourseDetail.vue` | Chi tiết khóa học |

---

## 5. Tích hợp Teacher API

### BE cần cung cấp:

| Method | Endpoint | Mô tả | Response |
|--------|----------|-------|----------|
| GET | `/teacher/:teacherId/classes` | Danh sách lớp | `Class[]` |
| GET | `/classes/:classId` | Chi tiết lớp | `ClassDetail` |
| GET | `/classes/:classId/students` | DS sinh viên trong lớp | `Student[]` |
| POST | `/classes` | Tạo lớp | `Class` |
| PUT | `/classes/:classId` | Cập nhật lớp | `Class` |
| GET | `/courses/:courseId/quizzes` | DS quiz | `Quiz[]` |
| POST | `/quizzes` | Tạo quiz | `Quiz` |
| PUT | `/quizzes/:quizId` | Cập nhật quiz | `Quiz` |
| DELETE | `/quizzes/:quizId` | Xóa quiz | `{}` |

### Response format — `GET /teacher/:id/classes`:

```json
[
  {
    "id": "cl1",
    "code": "SD18301",
    "courseName": "Lập trình Java 6",
    "courseId": "c1",
    "studentCount": 30,
    "quizCount": 5,
    "status": "active"
  }
]
```

### Response format — `GET /classes/:classId/students`:

```json
[
  {
    "id": "s1",
    "name": "Nguyễn Văn An",
    "email": "annv@fpt.edu.vn",
    "completedQuizzes": 5,
    "totalQuizzes": 5,
    "status": "passed"
  }
]
```

> Student `status` là một trong: `"passed"`, `"learning"`, `"incomplete"`.

### Response format — `POST /quizzes`:

```json
// Request:
{
  "classId": "cl1",
  "name": "Lab 1: Biến và kiểu dữ liệu",
  "duration": 60,
  "passingScore": 5.0,
  "questions": [
    {
      "text": "Java là gì?",
      "options": [
        { "label": "A", "value": "Ngôn ngữ lập trình", "isCorrect": true },
        { "label": "B", "value": "Hệ điều hành", "isCorrect": false },
        { "label": "C", "value": "Cơ sở dữ liệu", "isCorrect": false },
        { "label": "D", "value": "Trình duyệt web", "isCorrect": false }
      ]
    }
  ]
}
```

### File FE liên quan:

| File | Chức năng |
|------|-----------|
| `src/services/CourseService.js` | API lớp học |
| `src/services/QuizService.js` | API quiz |
| `src/composables/useTeacher.js` | State management |
| `src/views/teacher/TeacherDashboard.vue` | Danh sách lớp |
| `src/views/teacher/ClassDetail.vue` | Chi tiết lớp |
| `src/views/teacher/QuizManagement.vue` | Quản lý quiz |

---

## 6. Tích hợp Admin API

### BE cần cung cấp:

| Method | Endpoint | Mô tả | Response |
|--------|----------|-------|----------|
| GET | `/admin/certificates/stats` | Thống kê bằng | `Stats` |
| GET | `/certificates/recent?limit=N` | DS bằng gần đây | `Certificate[]` |
| GET | `/certificates/search?q=...` | Tìm kiếm bằng | `Certificate[]` |
| GET | `/certificates/:certId` | Chi tiết bằng | `CertificateDetail` |
| POST | `/certificates/:certId/verify` | Verify blockchain | `VerifyResult` |
| POST | `/certificates/:certId/revoke` | Thu hồi bằng | `{}` |

### Response format — `GET /admin/certificates/stats`:

```json
{
  "total": 156,
  "issued": 150,
  "revoked": 6
}
```

### Response format — `GET /certificates/recent`:

```json
[
  {
    "id": "CERT-156",
    "studentName": "Nguyễn Văn An",
    "studentEmail": "annv@fpt.edu.vn",
    "className": "SD18301",
    "courseCode": "Java 6",
    "gradeAverage": 8.5,
    "issueDate": "2026-02-15",
    "status": "issued",
    "blockchainHash": "0x7a8b9c..."
  }
]
```

### Response format — `GET /certificates/:certId`:

```json
{
  "id": "CERT-156",
  "studentName": "Nguyễn Văn An",
  "studentEmail": "annv@fpt.edu.vn",
  "className": "SD18301",
  "courseCode": "Lập trình Java 6",
  "gradeAverage": 8.5,
  "issueDate": "2026-02-15",
  "status": "issued",
  "blockchainHash": "0x7a8b9c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b",
  "blockchainInfo": {
    "hash": "0x7a8b9c1d2e3f...",
    "block": "12345678",
    "txHash": "0x999888777666...",
    "contract": "0xABC123DEF456..."
  }
}
```

> Certificate `status` là một trong: `"issued"`, `"revoked"`.

### File FE liên quan:

| File | Chức năng |
|------|-----------|
| `src/services/CertificateService.js` | API certificates |
| `src/composables/useAdmin.js` | State management |
| `src/views/admin/AdminDashboard.vue` | Dashboard admin |
| `src/views/admin/CertificateManagement.vue` | Chi tiết bằng |

---

## 7. Xóa mock data

Khi BE đã sẵn sàng, xóa mock data trong các view:

**Tìm và xóa** tất cả block code có comment `// Mock data for FE-only testing (remove when integrating with BE)` trong các file:

```
src/views/student/StudentDashboard.vue
src/views/student/CourseDetail.vue
src/views/teacher/TeacherDashboard.vue
src/views/teacher/ClassDetail.vue
src/views/teacher/QuizManagement.vue
src/views/admin/AdminDashboard.vue
src/views/admin/CertificateManagement.vue
```

Xóa toàn bộ `const mock...` declarations và các fallback logic (block `if (...length === 0) { ... = mock... }`).

---

## 8. Bật lại Route Guards

Trong file `src/router/index.js`, đổi `requiresAuth: false` thành `requiresAuth: true` cho tất cả route cần bảo vệ:

```js
// TRƯỚC (FE-only testing):
meta: { requiresAuth: false, roles: ['student'] }

// SAU (production):
meta: { requiresAuth: true, roles: ['student'] }
```

Các route cần cập nhật:
- `/student/dashboard`
- `/student/course/:id`
- `/teacher/dashboard`
- `/teacher/class/:id`
- `/teacher/quiz/:classId`
- `/admin/dashboard`
- `/admin/certificate/:id`

---

## 9. API Response Format

BE nên thống nhất response format:

### Thành công:

```json
{
  "data": { ... },
  "message": "Success"
}
```

Hoặc trả trực tiếp data (FE hiện đang dùng `response.data` qua axios).

### Lỗi:

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Token expired"
  }
}
```

### HTTP Status Codes:

| Code | Ý nghĩa |
|------|---------|
| 200 | Thành công |
| 201 | Tạo mới thành công |
| 400 | Request không hợp lệ |
| 401 | Chưa xác thực (token missing/expired) |
| 403 | Không có quyền truy cập |
| 404 | Resource không tìm thấy |
| 500 | Lỗi server |

---

## 10. Xử lý lỗi

FE đã có sẵn error handling qua `ApiError` class (`src/lib/apiFetcher/errors.js`):

```js
// Trong axiosInstance.js — interceptor xử lý 401:
if (error.response?.status === 401) {
    console.warn('Unauthorized access detected');
    // TODO: Redirect to login hoặc refresh token
}
```

### Bước tích hợp:

1. Cập nhật interceptor trong `src/lib/apiFetcher/axiosInstance.js` để tự redirect khi 401:

```js
if (error.response?.status === 401) {
    localStorage.removeItem('authToken');
    localStorage.removeItem('authUser');
    window.location.href = '/auth/login';
}
```

2. Các composable đã có `try/catch` — chỉ cần đảm bảo BE trả đúng HTTP status codes.

---

## Checklist tích hợp

- [ ] BE triển khai `/auth/google` endpoint
- [ ] Cấu hình `VITE_API_BASE_URL` trong `.env`
- [ ] Test login flow end-to-end
- [ ] BE triển khai Student APIs (`/student/:id/courses`, `/courses/:id`)
- [ ] Test Student Dashboard + Course Detail
- [ ] BE triển khai Teacher APIs (`/teacher/:id/classes`, `/classes/:id/students`, `/quizzes`)
- [ ] Test Teacher Dashboard + Class Detail + Quiz Management
- [ ] BE triển khai Admin APIs (`/admin/certificates/stats`, `/certificates/*`)
- [ ] Test Admin Dashboard + Certificate Management
- [ ] Xóa mock data khỏi views (Bước 7)
- [ ] Bật `requiresAuth: true` cho route guards (Bước 8)
- [ ] Test full flow production
