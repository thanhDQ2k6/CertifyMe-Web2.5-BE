# LMS Backend API Documentation

> Complete API reference for Frontend integration

---

## Table of Contents

1. [Response Structure](#1-response-structure)
2. [Authentication](#2-authentication)
3. [Student APIs](#3-student-apis)
4. [Teacher APIs](#4-teacher-apis)
5. [Quiz APIs](#5-quiz-apis)
6. [Admin APIs](#6-admin-apis)
7. [Enrollment APIs](#7-enrollment-apis)
8. [TypeScript Interfaces](#8-typescript-interfaces)
9. [Error Handling](#9-error-handling)

---

## 1. Response Structure

### Base Response Format

**TẤT CẢ API đều trả về cấu trúc này:**

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "error": null
}
```

### How to Read Response in Frontend

```typescript
// ❌ SAI - thiếu 1 layer .data
const user = response.data;

// ✅ ĐÚNG - axios.data + ApiResponse.data
const user = response.data.data;
```

### Recommended: API Wrapper

```typescript
// src/api/wrapper.ts
import type { AxiosResponse } from "axios";

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  error?: string;
}

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
const user = await api(apiClient.get("/api/auth/me"));
console.log(user.userId); // Direct access, no .data.data needed
```

---

## 2. Authentication

### 2.1 Get Current User

```
GET /api/auth/me
Authorization: Bearer <token>
```

**Response:**

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

**Frontend Usage:**

```typescript
const response = await apiClient.get("/api/auth/me");
const user = response.data.data;

// Store user info
localStorage.setItem("user", JSON.stringify(user));

// Use for subsequent API calls
const teacherId = user.userId;
```

### 2.2 Logout

```
POST /api/auth/logout
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

### 2.3 Check Role

```
GET /api/auth/check-role
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "message": "You are logged in as: TEACHER",
  "data": null
}
```

---

## 3. Student APIs

> **Role Required:** `STUDENT`

### 3.1 Get Student Courses (Dashboard)

```
GET /api/student/{studentId}/courses
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "courseId": "CRS-001",
      "courseCode": "SD18301",
      "courseName": "Lập trình Java 6",
      "courseIcon": "java",
      "teacherName": "Nguyễn Văn Teacher",
      "progress": 75,
      "totalQuizzes": 4,
      "completedQuizzes": 3,
      "averageScore": 7.5,
      "isCompleted": false
    }
  ]
}
```

**Frontend Usage:**

```typescript
async function loadStudentDashboard() {
  const userResponse = await apiClient.get("/api/auth/me");
  const user = userResponse.data.data;

  const coursesResponse = await apiClient.get(
    `/api/student/${user.userId}/courses`,
  );
  const courses = coursesResponse.data.data;

  return courses;
}
```

### 3.2 Get Student Certificates

```
GET /api/student/{studentId}/certificates
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "certificateId": "CERT-001",
      "courseName": "Lập trình Java 6",
      "courseCode": "SD18301",
      "averageScore": 8.5,
      "issuedAt": "2026-03-15T10:30:00",
      "verificationHash": "abc123...",
      "status": "ISSUED",
      "blockchainInfo": {
        "hash": "0x123...",
        "block": "12345",
        "txHash": "0xabc...",
        "contract": "0xdef..."
      }
    }
  ]
}
```

---

## 4. Teacher APIs

> **Role Required:** `TEACHER`

### 4.1 Get Teacher Classes

```
GET /api/teacher/{teacherId}/classes
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "classId": "CLS-005",
      "classCode": "DS18302",
      "courseName": "Lập trình Python",
      "courseId": "CRS-003",
      "teacherName": "Duong Quang Thanh",
      "studentCount": 25,
      "quizCount": 3,
      "status": "ACTIVE",
      "startDate": "2026-03-01",
      "endDate": "2026-08-01",
      "description": null
    }
  ]
}
```

**Frontend Usage:**

```typescript
async function loadTeacherDashboard() {
  const userResponse = await apiClient.get("/api/auth/me");
  const user = userResponse.data.data;

  if (user.role !== "TEACHER") {
    throw new Error("Unauthorized");
  }

  const classesResponse = await apiClient.get(
    `/api/teacher/${user.userId}/classes`,
  );
  return classesResponse.data.data;
}
```

### 4.2 Get Class Detail

```
GET /api/classes/{classId}
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": {
    "classId": "CLS-005",
    "classCode": "DS18302",
    "courseName": "Lập trình Python",
    "courseId": "CRS-003",
    "teacherName": "Duong Quang Thanh",
    "studentCount": 25,
    "quizCount": 3,
    "status": "ACTIVE",
    "startDate": "2026-03-01",
    "endDate": "2026-08-01"
  }
}
```

### 4.3 Get Students in Class

```
GET /api/classes/{classId}/students
Authorization: Bearer <token>
```

**Query Parameters:**

| Param    | Type   | Description                        |
| -------- | ------ | ---------------------------------- |
| `status` | string | Filter by status (LEARNING/PASSED) |
| `sort`   | string | Sort field (name/score/date)       |
| `order`  | string | Sort order (asc/desc)              |

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "studentId": "3d505413-8993-45b2-8d41-3018ba80e0b1",
      "fullName": "Duong Quang Vinh",
      "email": "vinhdqts00629@fpt.edu.vn",
      "avatarUrl": "https://lh3.googleusercontent.com/...",
      "completedQuizzes": 2,
      "totalQuizzes": 3,
      "averageScore": 7.5,
      "status": "LEARNING",
      "enrolledAt": "2026-03-10T08:00:00"
    }
  ]
}
```

### 4.4 Create Class

```
POST /api/classes
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "classCode": "SD18303",
  "courseId": "CRS-001",
  "teacherId": "c167d02c-260e-464a-9443-ebfbc212f300",
  "startDate": "2026-04-01",
  "endDate": "2026-09-01",
  "description": "Lớp Java buổi sáng"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "classId": "CLS-006",
    "classCode": "SD18303",
    "courseName": "Lập trình Java 6",
    "status": "ACTIVE"
  }
}
```

### 4.5 Update Class

```
PUT /api/classes/{classId}
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:** Same as Create Class

---

## 5. Quiz APIs

### 5.1 Get Quizzes by Class

```
GET /api/classes/{classId}/quizzes
Authorization: Bearer <token>
```

> **Role:** STUDENT or TEACHER

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "quizId": "QZ-001",
      "classId": "CLS-001",
      "quizName": "Quiz 1: OOP Basics",
      "duration": 30,
      "passingScore": 5.0,
      "maxScore": 10,
      "questionCount": 10,
      "completionRate": 80,
      "averageScore": 7.2,
      "status": "active"
    }
  ]
}
```

### 5.2 Get Quiz Detail (with Questions)

```
GET /api/quizzes/{quizId}
Authorization: Bearer <token>
```

> **Role:** STUDENT or TEACHER

**Response:**

```json
{
  "success": true,
  "data": {
    "quizId": "QZ-001",
    "quizName": "Quiz 1: OOP Basics",
    "duration": 30,
    "passingScore": 5.0,
    "status": "active",
    "questions": [
      {
        "questionId": 1,
        "questionText": "OOP stands for?",
        "optionA": "Object Oriented Programming",
        "optionB": "Object Optional Programming",
        "optionC": "Optional Oriented Programming",
        "optionD": "Object Oriented Protocol"
      },
      {
        "questionId": 2,
        "questionText": "Which is NOT an OOP principle?",
        "optionA": "Encapsulation",
        "optionB": "Inheritance",
        "optionC": "Compilation",
        "optionD": "Polymorphism"
      }
    ]
  }
}
```

### 5.3 Submit Quiz (IMPORTANT)

```
POST /api/quizzes/{quizId}/submit
Authorization: Bearer <token>
Content-Type: application/json
```

> **Role:** STUDENT only

**Request Body:**

```json
{
  "studentId": "3d505413-8993-45b2-8d41-3018ba80e0b1",
  "answers": [
    { "questionId": "1", "selectedOption": "A" },
    { "questionId": "2", "selectedOption": "C" },
    { "questionId": "3", "selectedOption": "B" },
    { "questionId": "4", "selectedOption": "D" }
  ]
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "score": 7.5,
    "maxScore": 10.0,
    "status": "PASSED",
    "submittedAt": "2026-03-20T10:30:00"
  }
}
```

**Frontend Usage - Complete Quiz Flow:**

```typescript
// 1. Load quiz with questions
async function startQuiz(quizId: string) {
  const response = await apiClient.get(`/api/quizzes/${quizId}`);
  return response.data.data;
}

// 2. User answers questions (store in state)
const userAnswers = ref<{ questionId: string; selectedOption: string }[]>([]);

function selectAnswer(questionId: string, option: string) {
  const existing = userAnswers.value.find((a) => a.questionId === questionId);
  if (existing) {
    existing.selectedOption = option;
  } else {
    userAnswers.value.push({ questionId, selectedOption: option });
  }
}

// 3. Submit all answers at once
async function submitQuiz(quizId: string, studentId: string) {
  const response = await apiClient.post(`/api/quizzes/${quizId}/submit`, {
    studentId: studentId,
    answers: userAnswers.value,
  });

  const result = response.data.data;

  // Show result to user
  if (result.status === "PASSED") {
    showSuccess(`Chúc mừng! Bạn đạt ${result.score}/${result.maxScore} điểm`);
  } else {
    showWarning(
      `Bạn đạt ${result.score}/${result.maxScore} điểm. Cần đạt 5.0 để pass.`,
    );
  }

  return result;
}
```

### 5.4 Get Quiz Result (Student)

```
GET /api/quizzes/{quizId}/result
Authorization: Bearer <token>
```

> **Role:** STUDENT only (auto-detects from token)

**Response:**

```json
{
  "success": true,
  "data": {
    "quizId": "QZ-001",
    "quizTitle": "Quiz 1: OOP Basics",
    "score": 7.5,
    "maxScore": 10.0,
    "passingScore": 5.0,
    "status": "PASSED",
    "attemptCount": 2,
    "submittedAt": "2026-03-20T10:30:00"
  }
}
```

### 5.5 Create Quiz (Teacher)

```
POST /api/quizzes
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "classId": "CLS-005",
  "quizName": "Quiz 2: Functions",
  "duration": 45,
  "passingScore": 5.0,
  "questions": [
    {
      "questionText": "What is a function?",
      "questionType": "multiple_choice",
      "options": [
        {
          "optionId": "A",
          "optionText": "A reusable block of code",
          "isCorrect": true
        },
        { "optionId": "B", "optionText": "A variable", "isCorrect": false },
        { "optionId": "C", "optionText": "A loop", "isCorrect": false },
        { "optionId": "D", "optionText": "A comment", "isCorrect": false }
      ]
    },
    {
      "questionText": "Which keyword defines a function in Python?",
      "questionType": "multiple_choice",
      "options": [
        { "optionId": "A", "optionText": "function", "isCorrect": false },
        { "optionId": "B", "optionText": "def", "isCorrect": true },
        { "optionId": "C", "optionText": "func", "isCorrect": false },
        { "optionId": "D", "optionText": "define", "isCorrect": false }
      ]
    }
  ]
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "quizId": "QZ-008",
    "quizName": "Quiz 2: Functions",
    "duration": 45,
    "passingScore": 5.0,
    "status": "draft"
  }
}
```

### 5.6 Update Quiz (Teacher)

```
PUT /api/quizzes/{quizId}
Authorization: Bearer <token>
```

**Request Body:** Same as Create Quiz

### 5.7 Delete Quiz (Teacher)

```
DELETE /api/quizzes/{quizId}
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": null
}
```

### 5.8 Get Quiz Submissions (Teacher)

```
GET /api/quizzes/{quizId}/submissions
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "submissionId": "1",
      "studentId": "3d505413-8993-45b2-8d41-3018ba80e0b1",
      "studentName": "Duong Quang Vinh",
      "studentEmail": "vinhdqts00629@fpt.edu.vn",
      "score": 7.5,
      "passed": true,
      "submittedAt": "2026-03-20T10:30:00"
    }
  ]
}
```

---

## 6. Admin APIs

> **Role Required:** `ADMIN`

### 6.1 Get Certificate Stats

```
GET /api/admin/certificates/stats
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": {
    "totalCertificates": 150,
    "issuedCertificates": 145,
    "revokedCertificates": 5,
    "certificatesThisMonth": 12,
    "certificatesThisYear": 89
  }
}
```

### 6.2 Get Recent Certificates

```
GET /api/certificates/recent?limit=10&page=1
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "certificateId": "CERT-001",
        "studentName": "Nguyen Van A",
        "studentEmail": "a@student.edu.vn",
        "className": "SD18301",
        "courseCode": "Java",
        "averageScore": 8.5,
        "issuedAt": "2026-03-15",
        "status": "ISSUED",
        "verificationHash": "abc123..."
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 150,
      "totalPages": 15
    }
  }
}
```

### 6.3 Search Certificates

```
GET /api/certificates/search?q=nguyen&status=ISSUED&limit=20
Authorization: Bearer <token>
```

### 6.4 Get Certificate Detail

```
GET /api/certificates/{certificateId}
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT-001",
    "studentId": "USR-STD-001",
    "studentName": "Nguyen Van A",
    "studentEmail": "a@student.edu.vn",
    "classId": "CLS-001",
    "className": "SD18301",
    "courseCode": "SD18301",
    "courseName": "Lập trình Java 6",
    "averageScore": 8.5,
    "issuedAt": "2026-03-15T10:00:00",
    "status": "ISSUED",
    "verificationHash": "abc123...",
    "blockchainInfo": {
      "transactionHash": "0x123...",
      "blockNumber": "12345",
      "contractAddress": "0xdef...",
      "networkName": "Ethereum Testnet",
      "explorerUrl": "https://etherscan.io/tx/0x123..."
    },
    "quizResults": [
      {
        "quizId": "QZ-001",
        "quizName": "Quiz 1: OOP Basics",
        "score": 8.0,
        "maxScore": 10,
        "completedAt": "2026-03-10T14:30:00"
      }
    ]
  }
}
```

### 6.5 Revoke Certificate

```
POST /api/certificates/{certificateId}/revoke
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "reason": "Academic integrity violation",
  "revokedBy": "admin-user-id"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT-001",
    "status": "REVOKED",
    "revokedAt": "2026-03-20T15:00:00",
    "revokedByUserId": "admin-user-id",
    "reason": "Academic integrity violation"
  }
}
```

### 6.6 Verify Certificate

```
POST /api/certificates/{certificateId}/verify
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT-001",
    "isValid": true,
    "verificationHash": "abc123...",
    "blockchainInfo": {
      "transactionHash": "0x123...",
      "blockNumber": "12345",
      "contractAddress": "0xdef...",
      "timestamp": "2026-03-15T10:00:00",
      "status": "CONFIRMED"
    },
    "verifiedAt": "2026-03-20T15:05:00"
  }
}
```

### 6.7 Get Users

```
GET /api/admin/users?role=TEACHER&status=active&page=1&limit=20
Authorization: Bearer <token>
```

**Response:**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "userId": "c167d02c-260e-464a-9443-ebfbc212f300",
        "fullName": "Duong Quang Thanh",
        "email": "thanhdqts00628@fpt.edu.vn",
        "role": "TEACHER",
        "avatarUrl": "https://...",
        "isActive": true,
        "createdAt": "2026-03-20",
        "lastLoginAt": "2026-03-20T08:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 50,
      "totalPages": 3
    }
  }
}
```

### 6.8 Update User Status

```
PUT /api/admin/users/{userId}/status
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "isActive": false,
  "reason": "Account suspended"
}
```

### 6.9 Update User Role

```
PUT /api/admin/users/{userId}/role
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "role": "TEACHER"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "userId": "xxx",
    "previousRole": "STUDENT",
    "newRole": "TEACHER",
    "updatedAt": "2026-03-20T15:00:00"
  }
}
```

---

## 7. Enrollment APIs

> **Role Required:** `TEACHER` or `ADMIN`

### 7.1 Create Enrollment

```
POST /api/enrollments
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "studentId": "3d505413-8993-45b2-8d41-3018ba80e0b1",
  "classId": "CLS-005"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "enrollmentId": 1,
    "studentId": "3d505413-8993-45b2-8d41-3018ba80e0b1",
    "classId": "CLS-005",
    "status": "LEARNING",
    "enrolledAt": "2026-03-20T10:00:00"
  }
}
```

### 7.2 Delete Enrollment

```
DELETE /api/enrollments/{enrollmentId}
Authorization: Bearer <token>
```

---

## 8. TypeScript Interfaces

```typescript
// src/types/api.types.ts

// Base Response
interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  error?: string;
}

// User
interface User {
  userId: string;
  email: string;
  fullName: string;
  avatarUrl: string;
  role: "STUDENT" | "TEACHER" | "ADMIN";
  isActive: boolean;
}

// Course (Student Dashboard)
interface CourseResponse {
  courseId: string;
  courseCode: string;
  courseName: string;
  courseIcon: string;
  teacherName: string;
  progress: number;
  totalQuizzes: number;
  completedQuizzes: number;
  averageScore: number;
  isCompleted: boolean;
}

// Class (Teacher Dashboard)
interface ClassResponse {
  classId: string;
  classCode: string;
  courseName: string;
  courseId: string;
  teacherName: string;
  studentCount: number;
  quizCount: number;
  status: string;
  startDate: string;
  endDate: string;
  description?: string;
}

// Student in Class
interface StudentResponse {
  studentId: string;
  fullName: string;
  email: string;
  avatarUrl: string;
  completedQuizzes: number;
  totalQuizzes: number;
  averageScore: number;
  status: "LEARNING" | "PASSED";
  enrolledAt: string;
}

// Quiz
interface QuizResponse {
  quizId: string;
  classId?: string;
  quizName: string;
  duration: number;
  passingScore: number;
  maxScore?: number;
  questionCount?: number;
  completionRate?: number;
  averageScore?: number;
  status: string;
  questions?: QuestionResponse[];
}

interface QuestionResponse {
  questionId: number;
  questionText: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
}

// Quiz Submission
interface QuizSubmissionRequest {
  studentId: string;
  answers: AnswerRequest[];
}

interface AnswerRequest {
  questionId: string;
  selectedOption: "A" | "B" | "C" | "D";
}

interface QuizResultResponse {
  score: number;
  maxScore: number;
  status: "PASSED" | "FAILED";
  submittedAt: string;
}

// Certificate
interface CertificateResponse {
  certificateId: string;
  courseName: string;
  courseCode: string;
  averageScore: number;
  issuedAt: string;
  verificationHash: string;
  status: "ISSUED" | "REVOKED";
  blockchainInfo?: BlockchainInfo;
}

interface BlockchainInfo {
  hash: string;
  block: string;
  txHash: string;
  contract: string;
}

// Pagination
interface PaginationInfo {
  page: number;
  limit: number;
  total: number;
  totalPages: number;
}

interface PaginatedResponse<T> {
  items: T[];
  pagination: PaginationInfo;
}
```

---

## 9. Error Handling

### Error Response Format

```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": "Error message here"
}
```

### Common HTTP Status Codes

| Code | Meaning               | When                             |
| ---- | --------------------- | -------------------------------- |
| 200  | OK                    | Request successful               |
| 201  | Created               | Resource created (POST)          |
| 400  | Bad Request           | Invalid request body/params      |
| 401  | Unauthorized          | Missing/invalid token            |
| 403  | Forbidden             | Valid token but wrong role       |
| 404  | Not Found             | Resource doesn't exist           |
| 500  | Internal Server Error | Server error (report to BE team) |

### Frontend Error Handling

```typescript
import axios from "axios";

const apiClient = axios.create({
  baseURL: "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

// Add token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle errors globally
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // Token expired/invalid - redirect to login
          localStorage.removeItem("token");
          window.location.href = "/login";
          break;
        case 403:
          // Wrong role - show error
          showError("Bạn không có quyền truy cập");
          break;
        case 404:
          showError("Không tìm thấy dữ liệu");
          break;
        default:
          showError(error.response.data?.error || "Có lỗi xảy ra");
      }
    } else {
      showError("Không thể kết nối đến server");
    }
    return Promise.reject(error);
  },
);

export default apiClient;
```

---

## Quick Reference

### API Endpoints by Role

| Role    | Endpoints                                                               |
| ------- | ----------------------------------------------------------------------- |
| Public  | `/api/auth/**`, `/oauth2/**`, `/login/**`                               |
| STUDENT | `/api/student/**`, `GET /api/quizzes/**`, `POST /api/quizzes/*/submit`  |
| TEACHER | `/api/teacher/**`, `/api/classes/**`, `POST/PUT/DELETE /api/quizzes/**` |
| ADMIN   | `/api/admin/**`, `/api/certificates/**`                                 |

### Base URL

- **Development:** `http://localhost:8080`
- **Production:** `https://api.lms.example.com`
