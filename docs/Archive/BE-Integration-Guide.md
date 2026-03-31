# 📋 Backend API Specification

> **Mục đích**: Tài liệu thống nhất input/output API giữa Backend và Frontend team  
> **Ngày cập nhật**: 2026-02-24  
> **Base URL**: `http://localhost:8080/api`

Tài liệu này định nghĩa contract cho tất cả API endpoints, bao gồm request format, response format, và error handling.

---

## 📚 Mục lục

1. [Quy chuẩn chung](#1-quy-chuẩn-chung)
2. [Authentication APIs](#2-authentication-apis)
3. [Student APIs](#3-student-apis)
4. [Teacher APIs](#4-teacher-apis)
5. [Admin APIs](#5-admin-apis)
6. [Error Handling](#6-error-handling)
7. [Checklist triển khai](#7-checklist-triển-khai)

---

## 1. QUY CHUẨN CHUNG

### 1.1. API Response Format

**TẤT CẢ endpoints phải sử dụng `ApiResponse<T>` wrapper:**

```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
  error: string | null;
}
```

**Success Response (200 OK):**

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

**Error Response (4xx, 5xx):**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Error message here"
}
```

### 1.2. Authentication

Tất cả protected endpoints yêu cầu JWT token trong header:

```http
Authorization: Bearer <jwt-token>
```

JWT Token có thời hạn 24 giờ, payload chứa:

```json
{
  "sub": "userId",
  "email": "user@email.com",
  "role": "STUDENT|TEACHER|ADMIN",
  "iat": 1708617600,
  "exp": 1708704000
}
```

### 1.3. Field Naming Conventions

**⚠️ QUAN TRỌNG - Tuân thủ nghiêm ngặt các quy tắc sau:**

| ✅ ĐÚNG       | ❌ SAI                      | Mô tả              |
| ------------- | --------------------------- | ------------------ |
| `userId`      | `id`, `user_id`             | User identifier    |
| `fullName`    | `name`, `full_name`         | Tên đầy đủ         |
| `avatarUrl`   | `avatar`, `avatar_url`      | URL ảnh đại diện   |
| `courseId`    | `id`, `course_id`           | Course identifier  |
| `studentId`   | `id`, `student_id`          | Student identifier |
| `isCompleted` | `completed`, `is_completed` | Boolean flags      |
| `createdAt`   | `created_at`, `createDate`  | Timestamps         |

**Role Values - PHẢI UPPERCASE:**

- ✅ `"STUDENT"`, `"TEACHER"`, `"ADMIN"`
- ❌ `"student"`, `"teacher"`, `"admin"`

**Status Values - PHẢI lowercase:**

- ✅ `"active"`, `"inactive"`, `"pending"`, `"completed"`
- ❌ `"ACTIVE"`, `"Active"`

### 1.4. HTTP Status Codes

| Code | Ý nghĩa               | Khi nào dùng                  |
| ---- | --------------------- | ----------------------------- |
| 200  | OK                    | Request thành công            |
| 201  | Created               | Tạo resource thành công       |
| 400  | Bad Request           | Request data không hợp lệ     |
| 401  | Unauthorized          | Token missing/invalid/expired |
| 403  | Forbidden             | Không có quyền truy cập       |
| 404  | Not Found             | Resource không tồn tại        |
| 500  | Internal Server Error | Lỗi server                    |

### 1.5. Pagination & Filtering

Khi endpoint trả về danh sách lớn, sử dụng pagination:

**Query Parameters:**

```
?page=1&limit=20&sort=createdAt&order=desc
```

**Response:**

```json
{
  "success": true,
  "data": {
    "items": [
      /* array of items */
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 100,
      "totalPages": 5
    }
  },
  "message": null,
  "error": null
}
```

---

## 2. AUTHENTICATION APIs

### 📖 Tài liệu chi tiết

Authentication đã được triển khai đầy đủ. Xem tài liệu:

- **[AUTH-ENDPOINTS-REFERENCE.md](./FE%20Tutorial/AUTH-ENDPOINTS-REFERENCE.md)** - Hướng dẫn gọi API auth
- **[API-RESPONSE-FORMAT.md](./FE%20Tutorial/API-RESPONSE-FORMAT.md)** - Format response auth
- **[FE-QUICK-START.md](./FE%20Tutorial/FE-QUICK-START.md)** - Quick start guide

### 📌 Tóm tắt Endpoints

| Method | Endpoint                       | Auth | Mô tả                       |
| ------ | ------------------------------ | ---- | --------------------------- |
| GET    | `/oauth2/authorization/google` | ❌   | Redirect đến Google login   |
| GET    | `/oauth2/redirect?token=xxx`   | ❌   | Nhận JWT token sau login    |
| GET    | `/api/auth/me`                 | ✅   | Lấy thông tin user hiện tại |
| POST   | `/api/auth/logout`             | ✅   | Đăng xuất                   |
| GET    | `/api/auth/check-role`         | ✅   | Kiểm tra role               |

### 📦 Response Format - UserResponse

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

---

## 3. STUDENT APIs

### 3.1. GET `/api/student/:studentId/courses`

**Mô tả**: Lấy danh sách khóa học của sinh viên

**Authentication**: ✅ Required (STUDENT role)

**URL Parameters:**

- `studentId` (string, UUID) - ID của sinh viên

**Success Response (200):**

```json
{
  "success": true,
  "data": [
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
  ],
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface CourseListItem {
  courseId: string;
  courseIcon: string;
  courseName: string;
  courseCode: string;
  teacherName: string;
  progress: number; // 0-100
  totalQuizzes: number;
  completedQuizzes: number;
  isCompleted: boolean;
  averageScore: number; // 0-10
}
```

---

### 3.2. GET `/api/courses/:courseId`

**Mô tả**: Lấy chi tiết khóa học kèm danh sách quiz

**Authentication**: ✅ Required (STUDENT role)

**URL Parameters:**

- `courseId` (string) - ID của khóa học

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "courseId": "c1",
    "courseIcon": "☕",
    "courseName": "Lập trình Java 6",
    "courseCode": "SD18301",
    "teacherName": "Thầy Nguyễn Văn A",
    "startDate": "2026-01-01",
    "endDate": "2026-04-01",
    "progress": 80,
    "totalQuizzes": 5,
    "completedQuizzes": 4,
    "isCompleted": false,
    "studentName": "Trần Văn B",
    "averageScore": 8.5,
    "quizzes": [
      {
        "quizId": "q1",
        "quizName": "Lab 1: Biến và kiểu dữ liệu",
        "score": 8.5,
        "maxScore": 10,
        "status": "completed",
        "completedAt": "2026-01-15T10:30:00Z"
      },
      {
        "quizId": "q2",
        "quizName": "Lab 2: Vòng lặp",
        "score": null,
        "maxScore": 10,
        "status": "pending",
        "completedAt": null
      },
      {
        "quizId": "q3",
        "quizName": "Lab 3: OOP",
        "score": null,
        "maxScore": 10,
        "status": "locked",
        "completedAt": null
      }
    ],
    "certificate": {
      "certificateId": "CERT-123",
      "verificationHash": "0x7a8b9c1d2e3f4a5b6c7d8e9f0a1b2c3d",
      "issuedAt": "2026-04-01T14:00:00Z",
      "blockchainInfo": {
        "transactionHash": "0x999888777666555444333222111000",
        "blockNumber": "12345678",
        "contractAddress": "0xABC123DEF456789"
      }
    }
  },
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface CourseDetail {
  courseId: string;
  courseIcon: string;
  courseName: string;
  courseCode: string;
  teacherName: string;
  startDate: string; // ISO date format
  endDate: string;
  progress: number;
  totalQuizzes: number;
  completedQuizzes: number;
  isCompleted: boolean;
  studentName: string;
  averageScore: number;
  quizzes: Quiz[];
  certificate?: Certificate; // Only present if isCompleted = true
}

interface Quiz {
  quizId: string;
  quizName: string;
  score: number | null;
  maxScore: number;
  status: "completed" | "pending" | "locked";
  completedAt: string | null;
}

interface Certificate {
  certificateId: string;
  verificationHash: string;
  issuedAt: string;
  blockchainInfo: {
    transactionHash: string;
    blockNumber: string;
    contractAddress: string;
  };
}
```

**Notes:**

- `certificate` field chỉ có khi `isCompleted: true`
- `status` values:
  - `"completed"` - Quiz đã hoàn thành
  - `"pending"` - Quiz đang mở, chưa làm
  - `"locked"` - Quiz chưa mở (phụ thuộc quiz trước)

---

### 3.3. GET `/api/student/:studentId/certificates`

**Mô tả**: Lấy danh sách chứng chỉ của sinh viên

**Authentication**: ✅ Required (STUDENT role)

**URL Parameters:**

- `studentId` (string, UUID) - ID của sinh viên

**Success Response (200):**

```json
{
  "success": true,
  "data": [
    {
      "certificateId": "CERT-123",
      "courseName": "Lập trình Java 6",
      "courseCode": "SD18301",
      "averageScore": 8.5,
      "issuedAt": "2026-04-01T14:00:00Z",
      "verificationHash": "0x7a8b9c1d2e3f4a5b6c7d8e9f0a1b2c3d",
      "status": "issued"
    }
  ],
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface CertificateListItem {
  certificateId: string;
  courseName: string;
  courseCode: string;
  averageScore: number;
  issuedAt: string;
  verificationHash: string;
  status: "issued" | "revoked";
}
```

---

### 3.4. POST `/api/quizzes/:quizId/submit`

**Mô tả**: Nộp bài quiz

**Authentication**: ✅ Required (STUDENT role)

**URL Parameters:**

- `quizId` (string) - ID của quiz

**Request Body:**

```json
{
  "studentId": "550e8400-e29b-41d4-a716-446655440000",
  "answers": [
    {
      "questionId": "q1",
      "selectedOption": "A"
    },
    {
      "questionId": "q2",
      "selectedOption": "C"
    }
  ]
}
```

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "quizId": "q1",
    "studentId": "550e8400-e29b-41d4-a716-446655440000",
    "score": 8.5,
    "maxScore": 10,
    "passed": true,
    "submittedAt": "2026-02-24T10:30:00Z",
    "details": [
      {
        "questionId": "q1",
        "isCorrect": true,
        "selectedOption": "A",
        "correctOption": "A"
      },
      {
        "questionId": "q2",
        "isCorrect": false,
        "selectedOption": "C",
        "correctOption": "B"
      }
    ]
  },
  "message": "Quiz submitted successfully",
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface QuizSubmitRequest {
  studentId: string;
  answers: {
    questionId: string;
    selectedOption: string;
  }[];
}

interface QuizSubmitResponse {
  quizId: string;
  studentId: string;
  score: number;
  maxScore: number;
  passed: boolean;
  submittedAt: string;
  details: {
    questionId: string;
    isCorrect: boolean;
    selectedOption: string;
    correctOption: string;
  }[];
}
```

---

### 3.5. GET `/api/quizzes/:quizId`

**Mô tả**: Lấy chi tiết quiz để làm bài

**Authentication**: ✅ Required (STUDENT role)

**URL Parameters:**

- `quizId` (string) - ID của quiz

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "quizId": "q1",
    "quizName": "Lab 1: Biến và kiểu dữ liệu",
    "duration": 60,
    "passingScore": 5.0,
    "maxScore": 10,
    "questions": [
      {
        "questionId": "q1-1",
        "questionText": "Java là gì?",
        "questionType": "multiple_choice",
        "options": [
          {
            "optionId": "A",
            "optionText": "Ngôn ngữ lập trình"
          },
          {
            "optionId": "B",
            "optionText": "Hệ điều hành"
          },
          {
            "optionId": "C",
            "optionText": "Cơ sở dữ liệu"
          },
          {
            "optionId": "D",
            "optionText": "Trình duyệt web"
          }
        ]
      }
    ]
  },
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface QuizDetail {
  quizId: string;
  quizName: string;
  duration: number; // minutes
  passingScore: number;
  maxScore: number;
  questions: Question[];
}

interface Question {
  questionId: string;
  questionText: string;
  questionType: "multiple_choice" | "true_false" | "essay";
  options: {
    optionId: string;
    optionText: string;
  }[];
}
```

---

## 4. TEACHER APIs

### 4.1. GET `/api/teacher/:teacherId/classes`

**Mô tả**: Lấy danh sách lớp học của giáo viên

**Authentication**: ✅ Required (TEACHER role)

**URL Parameters:**

- `teacherId` (string, UUID) - ID của giáo viên

**Success Response (200):**

```json
{
  "success": true,
  "data": [
    {
      "classId": "cl1",
      "classCode": "SD18301",
      "courseName": "Lập trình Java 6",
      "courseId": "c1",
      "studentCount": 30,
      "quizCount": 5,
      "status": "active",
      "startDate": "2026-01-01",
      "endDate": "2026-04-01"
    }
  ],
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface ClassListItem {
  classId: string;
  classCode: string;
  courseName: string;
  courseId: string;
  studentCount: number;
  quizCount: number;
  status: "active" | "completed" | "upcoming";
  startDate: string;
  endDate: string;
}
```

---

### 4.2. GET `/api/classes/:classId`

**Mô tả**: Lấy chi tiết lớp học

**Authentication**: ✅ Required (TEACHER role)

**URL Parameters:**

- `classId` (string) - ID của lớp học

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "classId": "cl1",
    "classCode": "SD18301",
    "courseName": "Lập trình Java 6",
    "courseId": "c1",
    "teacherName": "Thầy Nguyễn Văn A",
    "studentCount": 30,
    "status": "active",
    "startDate": "2026-01-01",
    "endDate": "2026-04-01",
    "description": "Khóa học lập trình Java cơ bản"
  },
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface ClassDetail {
  classId: string;
  classCode: string;
  courseName: string;
  courseId: string;
  teacherName: string;
  studentCount: number;
  status: "active" | "completed" | "upcoming";
  startDate: string;
  endDate: string;
  description: string;
}
```

---

### 4.3. GET `/api/classes/:classId/students`

**Mô tả**: Lấy danh sách sinh viên trong lớp

**Authentication**: ✅ Required (TEACHER role)

**URL Parameters:**

- `classId` (string) - ID của lớp học

**Query Parameters (optional):**

- `status` (string) - Filter by status: `"passed"`, `"learning"`, `"incomplete"`
- `sort` (string) - Sort field: `"fullName"`, `"averageScore"`
- `order` (string) - Sort order: `"asc"`, `"desc"`

**Success Response (200):**

```json
{
  "success": true,
  "data": [
    {
      "studentId": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "Nguyễn Văn An",
      "email": "annv@fpt.edu.vn",
      "avatarUrl": "https://lh3.googleusercontent.com/...",
      "completedQuizzes": 5,
      "totalQuizzes": 5,
      "averageScore": 8.5,
      "status": "passed",
      "enrolledAt": "2026-01-01T08:00:00Z"
    },
    {
      "studentId": "660e8400-e29b-41d4-a716-446655440001",
      "fullName": "Trần Thị Bích",
      "email": "bichtt@fpt.edu.vn",
      "avatarUrl": "https://lh3.googleusercontent.com/...",
      "completedQuizzes": 3,
      "totalQuizzes": 5,
      "averageScore": 7.0,
      "status": "learning",
      "enrolledAt": "2026-01-01T08:00:00Z"
    }
  ],
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface ClassStudent {
  studentId: string;
  fullName: string;
  email: string;
  avatarUrl: string;
  completedQuizzes: number;
  totalQuizzes: number;
  averageScore: number;
  status: "passed" | "learning" | "incomplete";
  enrolledAt: string;
}
```

**Status Values:**

- `"passed"` - Đã hoàn thành và đạt yêu cầu
- `"learning"` - Đang học, chưa hoàn thành
- `"incomplete"` - Chưa hoàn thành, có thể bị fail

---

### 4.4. POST `/api/classes`

**Mô tả**: Tạo lớp học mới

**Authentication**: ✅ Required (TEACHER role)

**Request Body:**

```json
{
  "classCode": "SD18301",
  "courseName": "Lập trình Java 6",
  "courseId": "c1",
  "teacherId": "770e8400-e29b-41d4-a716-446655440002",
  "startDate": "2026-01-01",
  "endDate": "2026-04-01",
  "description": "Khóa học lập trình Java cơ bản"
}
```

**Success Response (201):**

```json
{
  "success": true,
  "data": {
    "classId": "cl2",
    "classCode": "SD18301",
    "courseName": "Lập trình Java 6",
    "courseId": "c1",
    "teacherId": "770e8400-e29b-41d4-a716-446655440002",
    "studentCount": 0,
    "quizCount": 0,
    "status": "upcoming",
    "startDate": "2026-01-01",
    "endDate": "2026-04-01",
    "description": "Khóa học lập trình Java cơ bản",
    "createdAt": "2026-02-24T10:00:00Z"
  },
  "message": "Class created successfully",
  "error": null
}
```

---

### 4.5. PUT `/api/classes/:classId`

**Mô tả**: Cập nhật thông tin lớp học

**Authentication**: ✅ Required (TEACHER role)

**URL Parameters:**

- `classId` (string) - ID của lớp học

**Request Body:**

```json
{
  "classCode": "SD18301-UPDATED",
  "courseName": "Lập trình Java 6 - Updated",
  "startDate": "2026-01-15",
  "endDate": "2026-04-15",
  "description": "Updated description"
}
```

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "classId": "cl1",
    "classCode": "SD18301-UPDATED",
    "courseName": "Lập trình Java 6 - Updated",
    "courseId": "c1",
    "teacherId": "770e8400-e29b-41d4-a716-446655440002",
    "studentCount": 30,
    "quizCount": 5,
    "status": "active",
    "startDate": "2026-01-15",
    "endDate": "2026-04-15",
    "description": "Updated description",
    "updatedAt": "2026-02-24T10:30:00Z"
  },
  "message": "Class updated successfully",
  "error": null
}
```

---

### 4.6. GET `/api/classes/:classId/quizzes`

**Mô tả**: Lấy danh sách quiz của lớp

**Authentication**: ✅ Required (TEACHER role)

**URL Parameters:**

- `classId` (string) - ID của lớp học

**Success Response (200):**

```json
{
  "success": true,
  "data": [
    {
      "quizId": "q1",
      "quizName": "Lab 1: Biến và kiểu dữ liệu",
      "duration": 60,
      "passingScore": 5.0,
      "maxScore": 10,
      "questionCount": 10,
      "completionRate": 85,
      "averageScore": 7.5,
      "status": "active",
      "createdAt": "2026-01-05T10:00:00Z"
    }
  ],
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface QuizListItem {
  quizId: string;
  quizName: string;
  duration: number;
  passingScore: number;
  maxScore: number;
  questionCount: number;
  completionRate: number; // 0-100
  averageScore: number;
  status: "active" | "draft" | "archived";
  createdAt: string;
}
```

---

### 4.7. POST `/api/quizzes`

**Mô tả**: Tạo quiz mới

**Authentication**: ✅ Required (TEACHER role)

**Request Body:**

```json
{
  "classId": "cl1",
  "quizName": "Lab 1: Biến và kiểu dữ liệu",
  "duration": 60,
  "passingScore": 5.0,
  "maxScore": 10,
  "questions": [
    {
      "questionText": "Java là gì?",
      "questionType": "multiple_choice",
      "options": [
        {
          "optionId": "A",
          "optionText": "Ngôn ngữ lập trình",
          "isCorrect": true
        },
        {
          "optionId": "B",
          "optionText": "Hệ điều hành",
          "isCorrect": false
        },
        {
          "optionId": "C",
          "optionText": "Cơ sở dữ liệu",
          "isCorrect": false
        },
        {
          "optionId": "D",
          "optionText": "Trình duyệt web",
          "isCorrect": false
        }
      ]
    }
  ]
}
```

**Success Response (201):**

```json
{
  "success": true,
  "data": {
    "quizId": "q1",
    "classId": "cl1",
    "quizName": "Lab 1: Biến và kiểu dữ liệu",
    "duration": 60,
    "passingScore": 5.0,
    "maxScore": 10,
    "questionCount": 1,
    "status": "draft",
    "createdAt": "2026-02-24T11:00:00Z"
  },
  "message": "Quiz created successfully",
  "error": null
}
```

---

### 4.8. PUT `/api/quizzes/:quizId`

**Mô tả**: Cập nhật quiz

**Authentication**: ✅ Required (TEACHER role)

**URL Parameters:**

- `quizId` (string) - ID của quiz

**Request Body:** (Tương tự POST `/api/quizzes`)

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "quizId": "q1",
    "classId": "cl1",
    "quizName": "Lab 1: Biến và kiểu dữ liệu - Updated",
    "duration": 90,
    "passingScore": 6.0,
    "maxScore": 10,
    "questionCount": 1,
    "status": "active",
    "updatedAt": "2026-02-24T11:30:00Z"
  },
  "message": "Quiz updated successfully",
  "error": null
}
```

---

### 4.9. DELETE `/api/quizzes/:quizId`

**Mô tả**: Xóa quiz

**Authentication**: ✅ Required (TEACHER role)

**URL Parameters:**

- `quizId` (string) - ID của quiz

**Success Response (200):**

```json
{
  "success": true,
  "data": null,
  "message": "Quiz deleted successfully",
  "error": null
}
```

---

### 4.10. GET `/api/quizzes/:quizId/submissions`

**Mô tả**: Lấy danh sách bài nộp của quiz

**Authentication**: ✅ Required (TEACHER role)

**URL Parameters:**

- `quizId` (string) - ID của quiz

**Success Response (200):**

```json
{
  "success": true,
  "data": [
    {
      "submissionId": "sub1",
      "studentId": "550e8400-e29b-41d4-a716-446655440000",
      "studentName": "Nguyễn Văn An",
      "studentEmail": "annv@fpt.edu.vn",
      "score": 8.5,
      "maxScore": 10,
      "passed": true,
      "submittedAt": "2026-02-24T10:30:00Z"
    }
  ],
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface QuizSubmission {
  submissionId: string;
  studentId: string;
  studentName: string;
  studentEmail: string;
  score: number;
  maxScore: number;
  passed: boolean;
  submittedAt: string;
}
```

---

## 5. ADMIN APIs

### 5.1. GET `/api/admin/certificates/stats`

**Mô tả**: Lấy thống kê tổng quan về chứng chỉ

**Authentication**: ✅ Required (ADMIN role)

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "totalCertificates": 156,
    "issuedCertificates": 150,
    "revokedCertificates": 6,
    "certificatesThisMonth": 25,
    "certificatesThisYear": 156
  },
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface CertificateStats {
  totalCertificates: number;
  issuedCertificates: number;
  revokedCertificates: number;
  certificatesThisMonth: number;
  certificatesThisYear: number;
}
```

---

### 5.2. GET `/api/certificates/recent`

**Mô tả**: Lấy danh sách chứng chỉ gần đây

**Authentication**: ✅ Required (ADMIN role)

**Query Parameters:**

- `limit` (number, optional) - Số lượng records (default: 10, max: 100)
- `page` (number, optional) - Trang hiện tại (default: 1)

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "certificateId": "CERT-156",
        "studentName": "Nguyễn Văn An",
        "studentEmail": "annv@fpt.edu.vn",
        "className": "SD18301",
        "courseCode": "Java 6",
        "averageScore": 8.5,
        "issuedAt": "2026-02-15T14:00:00Z",
        "status": "issued",
        "verificationHash": "0x7a8b9c1d2e3f4a5b6c7d8e9f0a1b2c3d"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 156,
      "totalPages": 16
    }
  },
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface CertificateListResponse {
  items: CertificateListItem[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}

interface CertificateListItem {
  certificateId: string;
  studentName: string;
  studentEmail: string;
  className: string;
  courseCode: string;
  averageScore: number;
  issuedAt: string;
  status: "issued" | "revoked";
  verificationHash: string;
}
```

---

### 5.3. GET `/api/certificates/search`

**Mô tả**: Tìm kiếm chứng chỉ

**Authentication**: ✅ Required (ADMIN role)

**Query Parameters:**

- `q` (string, required) - Search query (tên SV, email, mã lớp, hoặc verification hash)
- `status` (string, optional) - Filter by status: `"issued"`, `"revoked"`
- `limit` (number, optional) - Số lượng records (default: 20)

**Success Response (200):**

```json
{
  "success": true,
  "data": [
    {
      "certificateId": "CERT-156",
      "studentName": "Nguyễn Văn An",
      "studentEmail": "annv@fpt.edu.vn",
      "className": "SD18301",
      "courseCode": "Java 6",
      "averageScore": 8.5,
      "issuedAt": "2026-02-15T14:00:00Z",
      "status": "issued",
      "verificationHash": "0x7a8b9c1d2e3f4a5b6c7d8e9f0a1b2c3d"
    }
  ],
  "message": null,
  "error": null
}
```

---

### 5.4. GET `/api/certificates/:certificateId`

**Mô tả**: Lấy chi tiết chứng chỉ

**Authentication**: ✅ Required (ADMIN role)

**URL Parameters:**

- `certificateId` (string) - ID của chứng chỉ

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT-156",
    "studentId": "550e8400-e29b-41d4-a716-446655440000",
    "studentName": "Nguyễn Văn An",
    "studentEmail": "annv@fpt.edu.vn",
    "classId": "cl1",
    "className": "SD18301",
    "courseCode": "Lập trình Java 6",
    "courseName": "Lập trình Java 6",
    "averageScore": 8.5,
    "issuedAt": "2026-04-01T14:00:00Z",
    "status": "issued",
    "verificationHash": "0x7a8b9c1d2e3f4a5b6c7d8e9f0a1b2c3d",
    "blockchainInfo": {
      "transactionHash": "0x999888777666555444333222111000",
      "blockNumber": "12345678",
      "contractAddress": "0xABC123DEF456789",
      "networkName": "Ethereum Mainnet",
      "explorerUrl": "https://etherscan.io/tx/0x999888777666555444333222111000"
    },
    "quizResults": [
      {
        "quizId": "q1",
        "quizName": "Lab 1: Biến và kiểu dữ liệu",
        "score": 8.5,
        "maxScore": 10,
        "completedAt": "2026-01-15T10:30:00Z"
      }
    ]
  },
  "message": null,
  "error": null
}
```

**TypeScript Interface:**

```typescript
interface CertificateDetail {
  certificateId: string;
  studentId: string;
  studentName: string;
  studentEmail: string;
  classId: string;
  className: string;
  courseCode: string;
  courseName: string;
  averageScore: number;
  issuedAt: string;
  status: "issued" | "revoked";
  verificationHash: string;
  blockchainInfo: {
    transactionHash: string;
    blockNumber: string;
    contractAddress: string;
    networkName: string;
    explorerUrl: string;
  };
  quizResults: {
    quizId: string;
    quizName: string;
    score: number;
    maxScore: number;
    completedAt: string;
  }[];
}
```

---

### 5.5. POST `/api/certificates/:certificateId/verify`

**Mô tả**: Verify chứng chỉ trên blockchain

**Authentication**: ✅ Required (ADMIN role)

**URL Parameters:**

- `certificateId` (string) - ID của chứng chỉ

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT-156",
    "isValid": true,
    "verificationHash": "0x7a8b9c1d2e3f4a5b6c7d8e9f0a1b2c3d",
    "blockchainInfo": {
      "transactionHash": "0x999888777666555444333222111000",
      "blockNumber": "12345678",
      "contractAddress": "0xABC123DEF456789",
      "timestamp": "2026-04-01T14:00:00Z",
      "status": "confirmed"
    },
    "verifiedAt": "2026-02-24T12:00:00Z"
  },
  "message": "Certificate verified successfully on blockchain",
  "error": null
}
```

**Error Response (400):**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Certificate verification failed: Hash mismatch"
}
```

**TypeScript Interface:**

```typescript
interface CertificateVerificationResult {
  certificateId: string;
  isValid: boolean;
  verificationHash: string;
  blockchainInfo: {
    transactionHash: string;
    blockNumber: string;
    contractAddress: string;
    timestamp: string;
    status: "confirmed" | "pending" | "failed";
  };
  verifiedAt: string;
}
```

---

### 5.6. POST `/api/certificates/:certificateId/revoke`

**Mô tả**: Thu hồi chứng chỉ

**Authentication**: ✅ Required (ADMIN role)

**URL Parameters:**

- `certificateId` (string) - ID của chứng chỉ

**Request Body:**

```json
{
  "reason": "Phát hiện gian lận trong quá trình thi",
  "revokedBy": "admin-user-id"
}
```

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT-156",
    "status": "revoked",
    "revokedAt": "2026-02-24T12:30:00Z",
    "revokedBy": "admin-user-id",
    "reason": "Phát hiện gian lận trong quá trình thi"
  },
  "message": "Certificate revoked successfully",
  "error": null
}
```

---

### 5.7. GET `/api/admin/users`

**Mô tả**: Lấy danh sách users (quản lý user)

**Authentication**: ✅ Required (ADMIN role)

**Query Parameters:**

- `role` (string, optional) - Filter by role: `"STUDENT"`, `"TEACHER"`, `"ADMIN"`
- `status` (string, optional) - Filter by status: `"active"`, `"inactive"`
- `page` (number, optional) - Trang hiện tại
- `limit` (number, optional) - Số lượng per page

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "fullName": "Nguyễn Văn An",
        "email": "annv@fpt.edu.vn",
        "role": "STUDENT",
        "avatarUrl": "https://lh3.googleusercontent.com/...",
        "isActive": true,
        "createdAt": "2025-09-01T08:00:00Z",
        "lastLoginAt": "2026-02-24T09:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 500,
      "totalPages": 25
    }
  },
  "message": null,
  "error": null
}
```

---

### 5.8. PUT `/api/admin/users/:userId/status`

**Mô tả**: Cập nhật trạng thái user (active/inactive)

**Authentication**: ✅ Required (ADMIN role)

**URL Parameters:**

- `userId` (string, UUID) - ID của user

**Request Body:**

```json
{
  "isActive": false,
  "reason": "Vi phạm quy định"
}
```

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "isActive": false,
    "updatedAt": "2026-02-24T13:00:00Z"
  },
  "message": "User status updated successfully",
  "error": null
}
```

---

## 6. ERROR HANDLING

### 6.1. Error Response Format

**Tất cả errors đều follow ApiResponse format:**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Human-readable error message"
}
```

### 6.2. Common Error Responses

#### 400 Bad Request

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Invalid request data: email is required"
}
```

**Khi nào trả:** Request data không hợp lệ, thiếu required fields, format sai

---

#### 401 Unauthorized

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Invalid credentials"
}
```

**Khi nào trả:**

- Token không có trong header
- Token không hợp lệ
- Token đã hết hạn (>24h)

---

#### 403 Forbidden

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Access denied"
}
```

**Khi nào trả:**

- User không có quyền truy cập endpoint (role không đúng)
- User cố gắng truy cập resource của user khác

---

#### 404 Not Found

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Resource not found"
}
```

**Khi nào trả:**

- Endpoint không tồn tại
- Resource ID không tìm thấy trong database

---

#### 409 Conflict

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "Email already exists"
}
```

**Khi nào trả:**

- Conflict với data hiện tại (duplicate email, username, etc.)
- Business logic violation

---

#### 500 Internal Server Error

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "An unexpected error occurred"
}
```

**Khi nào trả:**

- Lỗi server không dự kiến (database down, exception, etc.)
- **CHÚ Ý:** Không expose chi tiết lỗi ra ngoài, chỉ log vào server

---

### 6.3. Validation Errors

Khi có nhiều validation errors, trả về chi tiết:

```json
{
  "success": false,
  "data": {
    "validationErrors": [
      {
        "field": "email",
        "message": "Email is required"
      },
      {
        "field": "fullName",
        "message": "Full name must be at least 2 characters"
      }
    ]
  },
  "message": null,
  "error": "Validation failed"
}
```

---

### 6.4. Frontend Error Handling

**Frontend nên handle như sau:**

```typescript
try {
  const response = await axios.get("/api/auth/me");

  if (response.data.success) {
    const user = response.data.data;
    // Handle success
  } else {
    // Handle API-level error
    console.error(response.data.error);
  }
} catch (error) {
  // Handle HTTP-level error
  if (error.response) {
    const status = error.response.status;
    const errorMsg = error.response.data?.error || "Unknown error";

    switch (status) {
      case 401:
        // Redirect to login
        localStorage.removeItem("authToken");
        window.location.href = "/login";
        break;

      case 403:
        // Show "Access Denied" message
        alert("Bạn không có quyền truy cập");
        break;

      case 404:
        // Show "Not Found" message
        alert("Không tìm thấy dữ liệu");
        break;

      case 500:
        // Show generic error
        alert("Lỗi server, vui lòng thử lại sau");
        break;

      default:
        alert(errorMsg);
    }
  } else {
    // Network error
    alert("Không thể kết nối đến server");
  }
}
```

---

## 7. CHECKLIST TRIỂN KHAI

### Backend Team

#### Authentication

- [x] OAuth2 Google login flow
- [x] JWT token generation
- [x] GET `/api/auth/me`
- [x] POST `/api/auth/logout`
- [x] GET `/api/auth/check-role`

#### Student APIs

- [ ] GET `/api/student/:studentId/courses`
- [ ] GET `/api/courses/:courseId`
- [ ] GET `/api/student/:studentId/certificates`
- [ ] POST `/api/quizzes/:quizId/submit`
- [ ] GET `/api/quizzes/:quizId`

#### Teacher APIs

- [ ] GET `/api/teacher/:teacherId/classes`
- [ ] GET `/api/classes/:classId`
- [ ] GET `/api/classes/:classId/students`
- [ ] POST `/api/classes`
- [ ] PUT `/api/classes/:classId`
- [ ] GET `/api/classes/:classId/quizzes`
- [ ] POST `/api/quizzes`
- [ ] PUT `/api/quizzes/:quizId`
- [ ] DELETE `/api/quizzes/:quizId`
- [ ] GET `/api/quizzes/:quizId/submissions`

#### Admin APIs

- [ ] GET `/api/admin/certificates/stats`
- [ ] GET `/api/certificates/recent`
- [ ] GET `/api/certificates/search`
- [ ] GET `/api/certificates/:certificateId`
- [ ] POST `/api/certificates/:certificateId/verify`
- [ ] POST `/api/certificates/:certificateId/revoke`
- [ ] GET `/api/admin/users`
- [ ] PUT `/api/admin/users/:userId/status`

#### General

- [ ] Tất cả endpoints sử dụng ApiResponse wrapper
- [ ] Field naming conventions đúng (userId, fullName, avatarUrl, etc.)
- [ ] Role values là UPPERCASE (STUDENT, TEACHER, ADMIN)
- [ ] Status values là lowercase (active, completed, pending, etc.)
- [ ] Error handling đầy đủ với HTTP status codes
- [ ] JWT token validation trên tất cả protected endpoints
- [ ] Role-based authorization
- [ ] Logging errors vào server (không expose chi tiết ra client)

### Frontend Team

- [ ] Setup axios instance với base URL
- [ ] Implement request interceptor (tự động thêm JWT token)
- [ ] Implement response interceptor (handle ApiResponse wrapper)
- [ ] Handle 401 errors (redirect to login)
- [ ] Handle 403 errors (show access denied)
- [ ] Extract data từ `response.data.data`
- [ ] Check `response.data.success` trước khi sử dụng data
- [ ] Sử dụng đúng field names (userId, fullName, avatarUrl)
- [ ] Handle role values UPPERCASE
- [ ] Xóa mock data sau khi BE ready
- [ ] Test end-to-end flow cho mọi feature

---

## 📚 TÀI LIỆU LIÊN QUAN

- **[AUTH-ENDPOINTS-REFERENCE.md](./FE%20Tutorial/AUTH-ENDPOINTS-REFERENCE.md)** - Chi tiết về Auth endpoints
- **[API-RESPONSE-FORMAT.md](./FE%20Tutorial/API-RESPONSE-FORMAT.md)** - Format response và error handling
- **[FE-QUICK-START.md](./FE%20Tutorial/FE-QUICK-START.md)** - Quick start guide cho FE
- **[FE-AUTH-INTEGRATION-GUIDE.md](./FE%20Tutorial/FE-AUTH-INTEGRATION-GUIDE.md)** - Hướng dẫn tích hợp auth đầy đủ

---

## 📞 SUPPORT

**Backend Team:**

- Slack: #backend-team
- Email: backend-team@fpt.edu.vn

**Frontend Team:**

- Slack: #frontend-team
- Email: frontend-team@fpt.edu.vn

**API Issues:**

- Tạo issue trên GitHub repository
- Tag: `api`, `backend`, `bug`, `enhancement`

---

**Last Updated:** 2026-02-24  
**Version:** 2.0  
**Status:** ✅ Auth completed | 🚧 Student/Teacher/Admin in progress
