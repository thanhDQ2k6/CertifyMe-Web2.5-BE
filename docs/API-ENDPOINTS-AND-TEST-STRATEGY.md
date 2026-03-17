# API ENDPOINTS & TEST STRATEGY

> **Dự án**: LMS Backend
> **Ngày**: 17/03/2026
> **Base URL**: `http://localhost:8080`

---

## Mục lục

1. [Response Format](#1-response-format)
2. [Xác thực (Authentication)](#2-xác-thực)
3. [Cài đặt Google OAuth & Cấu hình .env](#3-cài-đặt-google-oauth--cấu-hình-env)
4. [Module: Auth](#4-module-auth)
5. [Module: Enrollment (Student Dashboard)](#5-module-enrollment)
6. [Module: Course (Student Course Detail)](#6-module-course)
7. [Module: Classroom (Teacher)](#7-module-classroom)
8. [Module: Quiz](#8-module-quiz)
9. [Module: Admin — Certificates](#9-module-admin--certificates)
10. [Module: Admin — Users](#10-module-admin--users)
11. [Test Strategy: Postman](#11-test-strategy-postman)
12. [Test Strategy: JUnit](#12-test-strategy-junit)
13. [Checklist tổng hợp](#13-checklist-tổng-hợp)

---

## 1. Response Format

Tất cả API đều trả về cùng 1 cấu trúc:

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "error": null
}
```

Khi lỗi:

```json
{
  "success": false,
  "data": null,
  "error": "Mô tả lỗi"
}
```

**HTTP Status Codes:**

| Code | Ý nghĩa               | Khi nào                              |
| ---- | --------------------- | ------------------------------------ |
| 200  | OK                    | Thành công                           |
| 201  | Created               | Tạo mới thành công                   |
| 400  | Bad Request           | Dữ liệu đầu vào sai, validation fail |
| 401  | Unauthorized          | Chưa đăng nhập / token hết hạn       |
| 403  | Forbidden             | Không đủ quyền (sai role)            |
| 404  | Not Found             | Không tìm thấy resource              |
| 500  | Internal Server Error | Lỗi server                           |

---

## 2. Xác thực

### Luồng đăng nhập

```
1. Browser mở: GET /oauth2/authorization/google
2. Google xác minh → redirect về: GET /oauth2/redirect?token=<JWT>
3. Frontend lưu JWT, gửi trong mọi request tiếp theo
```

### Header cho mọi request (trừ đăng nhập)

```
Authorization: Bearer <JWT_TOKEN>
```

### Lấy token test

Đăng nhập qua Google OAuth2, lấy token từ redirect URL. Hoặc:

- Tạo token thủ công trong DB test với role STUDENT / TEACHER / ADMIN
- Gọi `/oauth2/authorization/google` trên browser, copy token từ redirect

---

## 3. Cài đặt Google OAuth & Cấu hình .env

### 3.1. Tạo Google OAuth Credentials

#### Bước 1: Truy cập Google Cloud Console

1. Mở trình duyệt, truy cập: https://console.cloud.google.com/
2. Đăng nhập bằng tài khoản Google

#### Bước 2: Tạo Project (nếu chưa có)

1. Click vào dropdown chọn project ở thanh trên cùng
2. Click **"New Project"**
3. Đặt tên project: `LMS Backend` (hoặc tên tùy chọn)
4. Click **"Create"**
5. Chờ project tạo xong, chọn project vừa tạo

#### Bước 3: Bật Google OAuth API

1. Vào menu **"APIs & Services" → "Library"**
2. Tìm kiếm **"Google+ API"** hoặc **"Google People API"**
3. Click **"Enable"** để bật API

#### Bước 4: Cấu hình OAuth Consent Screen

1. Vào **"APIs & Services" → "OAuth consent screen"**
2. Chọn **User Type**: **External** → Click **"Create"**
3. Điền thông tin:
   - **App name**: `LMS Backend`
   - **User support email**: email của bạn
   - **Developer contact information**: email của bạn
4. Click **"Save and Continue"**
5. Ở bước **Scopes**: click **"Add or Remove Scopes"**, chọn:
   - `openid`
   - `email`
   - `profile`
6. Click **"Save and Continue"** qua các bước còn lại

#### Bước 5: Tạo OAuth Client ID

1. Vào **"APIs & Services" → "Credentials"**
2. Click **"+ CREATE CREDENTIALS" → "OAuth client ID"**
3. Chọn **Application type**: **Web application**
4. Đặt tên: `LMS Backend Local`
5. Thêm **Authorized JavaScript origins**:
   ```
   http://localhost:3000
   http://localhost:8080
   ```
6. Thêm **Authorized redirect URIs**:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
7. Click **"Create"**
8. Một popup hiện ra với **Client ID** và **Client Secret** → **Copy cả hai giá trị này**

> **Lưu ý**: Client ID có dạng `xxxx.apps.googleusercontent.com`, Client Secret có dạng `GOCSPX-xxxx`

### 3.2. Cấu hình file .env

#### Bước 1: Tạo file .env

Tại thư mục gốc project (cùng cấp với `pom.xml`), copy file mẫu:

```bash
cp .env.example .env
```

#### Bước 2: Điền thông tin

Mở file `.env` và thay thế các giá trị:

```properties
# Google OAuth2 — Lấy từ Google Cloud Console (Bước 5 ở trên)
GOOGLE_CLIENT_ID=123456789-abcdef.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-your-actual-secret-here

# JWT — Chuỗi bí mật để ký token, ít nhất 32 ký tự
JWT_SECRET=my-super-secret-jwt-key-change-this-in-production-2026

# Frontend — URL frontend đang chạy
FRONTEND_URL=http://localhost:3000
```

| Biến                   | Mô tả                                  | Cách lấy                           |
| ---------------------- | -------------------------------------- | ---------------------------------- |
| `GOOGLE_CLIENT_ID`     | OAuth Client ID từ Google              | Google Cloud Console → Credentials |
| `GOOGLE_CLIENT_SECRET` | OAuth Client Secret từ Google          | Google Cloud Console → Credentials |
| `JWT_SECRET`           | Khóa bí mật để ký JWT token            | Tự tạo, tối thiểu 32 ký tự         |
| `FRONTEND_URL`         | URL frontend (dùng cho redirect OAuth) | Mặc định `http://localhost:3000`   |

> **Quan trọng**: File `.env` đã nằm trong `.gitignore` — KHÔNG ĐƯỢC commit lên Git.

### 3.3. Cách nạp biến môi trường khi chạy

Project **không** dùng thư viện `spring-dotenv`, nên file `.env` không tự động được load. Chọn **một trong các cách** sau:

#### Cách 1: Dùng IDE (IntelliJ IDEA — Khuyến nghị)

1. Cài plugin **"EnvFile"**: `File → Settings → Plugins → Tìm "EnvFile" → Install`
2. Mở **Run/Debug Configuration** (`Edit Configurations...`)
3. Chọn cấu hình `BackendApplication`
4. Tab **"EnvFile"** → Tick **"Enable EnvFile"**
5. Click **"+"** → Chọn file `.env` ở thư mục gốc project
6. Click **"Apply" → "OK"**
7. Chạy bằng nút **Run** như bình thường

#### Cách 2: Dùng IDE (IntelliJ IDEA — không cần plugin)

1. Mở **Run/Debug Configuration** (`Edit Configurations...`)
2. Tìm mục **"Environment variables"**
3. Click icon **"..."** bên phải, nhập từng biến:
   ```
   GOOGLE_CLIENT_ID=123456789-abcdef.apps.googleusercontent.com;GOOGLE_CLIENT_SECRET=GOCSPX-xxx;JWT_SECRET=my-secret-key;FRONTEND_URL=http://localhost:3000
   ```
4. Click **"Apply" → "OK"**

#### Cách 3: Dùng terminal (Linux / macOS / Git Bash)

```bash
# Nạp biến từ .env vào shell, sau đó chạy Spring Boot
export $(cat .env | grep -v '^#' | xargs) && mvn spring-boot:run
```

#### Cách 4: Dùng terminal (Windows CMD)

```cmd
:: Đặt biến thủ công
set GOOGLE_CLIENT_ID=123456789-abcdef.apps.googleusercontent.com
set GOOGLE_CLIENT_SECRET=GOCSPX-xxx
set JWT_SECRET=my-secret-key
set FRONTEND_URL=http://localhost:3000
mvn spring-boot:run
```

#### Cách 5: Dùng terminal (Windows PowerShell)

```powershell
$env:GOOGLE_CLIENT_ID="123456789-abcdef.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET="GOCSPX-xxx"
$env:JWT_SECRET="my-secret-key"
$env:FRONTEND_URL="http://localhost:3000"
mvn spring-boot:run
```

### 3.4. Kiểm thử cấu hình OAuth

#### Test 1: Kiểm tra ứng dụng khởi động thành công

```bash
mvn spring-boot:run
```

Nếu cấu hình đúng, console sẽ hiện:

```
Started BackendApplication in X.XXX seconds
```

Nếu sai `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`, ứng dụng vẫn khởi động được nhưng sẽ lỗi khi đăng nhập.

#### Test 2: Kiểm tra luồng đăng nhập OAuth

1. Mở trình duyệt, truy cập:
   ```
   http://localhost:8080/oauth2/authorization/google
   ```
2. Trình duyệt sẽ redirect sang trang đăng nhập Google
3. Đăng nhập bằng tài khoản Google
4. Sau khi xác thực, Google redirect về:
   ```
   http://localhost:3000/oauth2/redirect?token=eyJhbGciOiJIUzI1NiJ9...
   ```
5. Copy giá trị `token` từ URL → Đây là JWT token dùng cho các API tiếp theo

> **Nếu frontend chưa chạy**: URL sẽ báo lỗi không truy cập được, nhưng bạn vẫn có thể copy token từ thanh địa chỉ trình duyệt.

#### Test 3: Dùng token để gọi API

Mở Postman hoặc terminal:

```bash
curl -H "Authorization: Bearer <TOKEN_VỪA_COPY>" http://localhost:8080/api/auth/me
```

**Kết quả mong đợi** `200`:

```json
{
  "success": true,
  "data": {
    "userId": "...",
    "email": "your-email@gmail.com",
    "fullName": "Tên Google của bạn",
    "avatarUrl": "https://lh3.googleusercontent.com/...",
    "role": "STUDENT",
    "isActive": true
  }
}
```

#### Test 4: Kiểm tra lỗi cấu hình thường gặp

| Triệu chứng                                         | Nguyên nhân                         | Cách sửa                                                              |
| --------------------------------------------------- | ----------------------------------- | --------------------------------------------------------------------- |
| Google hiện **"Error 401: invalid_client"**         | Sai `GOOGLE_CLIENT_ID`              | Kiểm tra lại Client ID trong Google Cloud Console                     |
| Google hiện **"Error 400: redirect_uri_mismatch"**  | Thiếu redirect URI trong Google     | Thêm `http://localhost:8080/login/oauth2/code/google` vào Credentials |
| Redirect về frontend nhưng **không có token**       | Sai `GOOGLE_CLIENT_SECRET`          | Kiểm tra lại Client Secret                                            |
| Token trả về nhưng gọi API báo **401 Unauthorized** | Sai `JWT_SECRET` hoặc token hết hạn | Kiểm tra `JWT_SECRET` khớp, thử đăng nhập lại                         |
| Ứng dụng báo lỗi khi khởi động                      | Biến môi trường chưa được nạp       | Kiểm tra lại cách nạp `.env` (mục 3.3)                                |

---

## 4. Module: Auth

> **Mô tả**: Quản lý phiên đăng nhập và thông tin người dùng
> **Base**: `/api/auth`
> **Quyền**: Mọi user đã đăng nhập

### 4.1. GET /api/auth/me

Lấy thông tin tài khoản đang đăng nhập.

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "student@gmail.com",
    "fullName": "Nguyễn Văn A",
    "avatarUrl": "https://lh3.googleusercontent.com/...",
    "role": "STUDENT",
    "isActive": true
  }
}
```

### 4.2. POST /api/auth/logout

Đăng xuất (client-side — xóa token).

**Response** `200`:

```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

### 4.3. GET /api/auth/check-role

Kiểm tra role hiện tại.

**Response** `200`:

```json
{
  "success": true,
  "message": "You are logged in as: STUDENT"
}
```

---

## 5. Module: Enrollment

> **Mô tả**: Dashboard học sinh — danh sách khóa học & chứng chỉ
> **Base**: `/api/student`
> **Quyền**: STUDENT

### 5.1. GET /api/student/{studentId}/courses

Danh sách khóa học đã ghi danh.

**Response** `200`:

```json
{
  "success": true,
  "data": [
    {
      "courseId": "CRS001",
      "courseCode": "CS101",
      "courseName": "Lập trình Java",
      "courseIcon": "https://...",
      "teacherName": "Trần Văn B",
      "progress": 75,
      "totalQuizzes": 4,
      "completedQuizzes": 3,
      "averageScore": 8.2,
      "isCompleted": false
    }
  ]
}
```

### 5.2. GET /api/student/{studentId}/certificates

Danh sách chứng chỉ.

**Response** `200`:

```json
{
  "success": true,
  "data": [
    {
      "courseName": "Lập trình Java",
      "courseCode": "CS101",
      "certificateId": "CERT-001",
      "averageScore": 8.5,
      "issuedAt": "2026-01-15T00:00:00Z",
      "verificationHash": "0xabc123...",
      "status": "issued",
      "blockchainInfo": {
        "hash": "0xabc...",
        "block": "12345",
        "txHash": "0xdef...",
        "contract": "0x789..."
      }
    }
  ]
}
```

---

## 6. Module: Course

> **Mô tả**: Chi tiết khóa học cho học sinh (tiến độ, quiz, chứng chỉ)
> **Base**: `/api/courses`
> **Quyền**: STUDENT

### 6.1. GET /api/courses/{courseId}

Chi tiết khóa học với tiến độ cá nhân (lấy studentId từ JWT).

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "courseIcon": "https://...",
    "courseName": "Lập trình Java",
    "courseCode": "CS101",
    "teacherName": "Trần Văn B",
    "startDate": "2026-01-01",
    "endDate": "2026-06-01",
    "progress": 75,
    "totalQuizzes": 4,
    "completedQuizzes": 3,
    "isCompleted": false,
    "studentName": "Nguyễn Văn A",
    "averageScore": 8.2,
    "quizzes": [
      {
        "id": "QZ001",
        "name": "Quiz 1: Biến và kiểu dữ liệu",
        "score": 9.0,
        "maxScore": 10.0,
        "status": "completed"
      },
      {
        "id": "QZ002",
        "name": "Quiz 2: Vòng lặp",
        "score": null,
        "maxScore": 10.0,
        "status": "pending"
      }
    ],
    "certificate": null
  }
}
```

**status** trong quizzes: `"completed"` | `"pending"` | `"locked"`

---

## 7. Module: Classroom

> **Mô tả**: Quản lý lớp học cho giáo viên
> **Base**: `/api/teacher`, `/api/classes`
> **Quyền**: TEACHER

### 7.1. GET /api/teacher/{teacherId}/classes

Danh sách lớp học của giáo viên.

**Response** `200`:

```json
{
  "success": true,
  "data": [
    {
      "classId": "CLS001",
      "classCode": "JAVA-01",
      "courseName": "Lập trình Java",
      "courseId": "CRS001",
      "teacherName": "Trần Văn B",
      "studentCount": 25,
      "quizCount": 4,
      "status": "ACTIVE",
      "startDate": "2026-01-01",
      "endDate": "2026-06-01",
      "description": "Lớp Java cơ bản"
    }
  ]
}
```

### 7.2. GET /api/classes/{classId}

Chi tiết 1 lớp học.

**Response** `200`: Cùng cấu trúc với item trong 6.1.

### 7.3. GET /api/classes/{classId}/students

Danh sách học sinh trong lớp.

**Query params** (optional):

| Param    | Mô tả               | Ví dụ                                     |
| -------- | ------------------- | ----------------------------------------- |
| `status` | Lọc theo trạng thái | `LEARNING`, `PASSED`, `FAILED`, `DROPPED` |
| `sort`   | Sắp xếp theo field  | `fullName`, `averageScore`, `enrolledAt`  |
| `order`  | Chiều sắp xếp       | `asc`, `desc`                             |

**Response** `200`:

```json
{
  "success": true,
  "data": [
    {
      "studentId": "STU001",
      "fullName": "Nguyễn Văn A",
      "email": "student@gmail.com",
      "avatarUrl": "https://...",
      "completedQuizzes": 3,
      "totalQuizzes": 4,
      "averageScore": 8.2,
      "status": "LEARNING",
      "enrolledAt": "2026-01-15T08:00:00"
    }
  ]
}
```

### 7.4. POST /api/classes

Tạo lớp học mới.

**Request Body**:

```json
{
  "classCode": "JAVA-02",
  "courseId": "CRS001",
  "teacherId": "TCH001",
  "startDate": "2026-03-01",
  "endDate": "2026-08-01",
  "description": "Lớp Java nâng cao"
}
```

| Field         | Bắt buộc | Validation                   |
| ------------- | :------: | ---------------------------- |
| `classCode`   |   Yes    | @NotBlank                    |
| `courseId`    |   Yes    | @NotBlank                    |
| `teacherId`   |   Yes    | @NotBlank                    |
| `startDate`   |   Yes    | @NotNull, format: YYYY-MM-DD |
| `endDate`     |   Yes    | @NotNull, format: YYYY-MM-DD |
| `courseName`  |    No    |                              |
| `description` |    No    |                              |

**Response** `201`: Cùng cấu trúc `ClassResponseDTO`.

### 7.5. PUT /api/classes/{id}

Cập nhật lớp học.

**Request Body**: Cùng cấu trúc với POST.

**Response** `200`: Cùng cấu trúc `ClassResponseDTO`.

---

## 8. Module: Quiz

> **Mô tả**: CRUD bài kiểm tra (Teacher) + làm bài (Student)
> **Base**: `/api/quizzes`, `/api/classes/{classId}/quizzes`

### 8.1. GET /api/classes/{classId}/quizzes

Danh sách bài kiểm tra của lớp.

**Quyền**: STUDENT hoặc TEACHER

**Response** `200`:

```json
{
  "success": true,
  "data": [
    {
      "quizId": "QZ001",
      "classId": "CLS001",
      "quizName": "Quiz 1: Biến và kiểu dữ liệu",
      "duration": 30,
      "passingScore": 5.0,
      "maxScore": 10,
      "questionCount": 10,
      "completionRate": 80,
      "averageScore": 7.5,
      "status": "PUBLISHED",
      "createdAt": "2026-01-10T10:00:00",
      "updatedAt": "2026-01-10T10:00:00",
      "questions": null
    }
  ]
}
```

### 8.2. GET /api/quizzes/{quizId}

Chi tiết bài kiểm tra (kèm câu hỏi, KHÔNG kèm đáp án).

**Quyền**: STUDENT hoặc TEACHER

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "quizId": "QZ001",
    "classId": "CLS001",
    "quizName": "Quiz 1",
    "duration": 30,
    "passingScore": 5.0,
    "maxScore": 10,
    "questionCount": 2,
    "status": "PUBLISHED",
    "questions": [
      {
        "questionId": 1,
        "questionText": "Java là ngôn ngữ gì?",
        "optionA": "Lập trình",
        "optionB": "Đánh dấu",
        "optionC": "Truy vấn",
        "optionD": "Kịch bản"
      }
    ]
  }
}
```

### 8.3. POST /api/quizzes

Tạo bài kiểm tra mới.

**Quyền**: TEACHER

**Request Body**:

```json
{
  "classId": "CLS001",
  "quizName": "Quiz 3: OOP",
  "duration": 45,
  "passingScore": 5.0,
  "maxScore": 10,
  "questions": [
    {
      "questionText": "Tính đóng gói là gì?",
      "questionType": "SINGLE_CHOICE",
      "options": [
        { "optionText": "Ẩn dữ liệu bên trong", "isCorrect": true },
        { "optionText": "Kế thừa", "isCorrect": false },
        { "optionText": "Đa hình", "isCorrect": false },
        { "optionText": "Trừu tượng", "isCorrect": false }
      ]
    }
  ]
}
```

| Field                 | Bắt buộc | Validation                                        |
| --------------------- | :------: | ------------------------------------------------- |
| `classId`             |   Yes    | @NotBlank                                         |
| `quizName`            |   Yes    | @NotBlank                                         |
| `duration`            |   Yes    | @NotNull, @Positive (phút)                        |
| `passingScore`        |   Yes    | @NotNull                                          |
| `questions`           |   Yes    | @NotEmpty, mỗi question có @NotBlank questionText |
| `questions[].options` |   Yes    | @NotEmpty, mỗi option có @NotBlank optionText     |

**Response** `201`: `QuizResponseDTO`

### 8.4. PUT /api/quizzes/{quizId}

Cập nhật bài kiểm tra (thay thế toàn bộ câu hỏi).

**Quyền**: TEACHER

**Request Body**: Cùng cấu trúc với POST.

**Response** `200`: `QuizResponseDTO`

### 8.5. DELETE /api/quizzes/{quizId}

Xóa mềm bài kiểm tra (đổi status → CLOSED).

**Quyền**: TEACHER

**Response** `200`:

```json
{
  "success": true,
  "data": null
}
```

### 8.6. POST /api/quizzes/{quizId}/submit

Nộp bài kiểm tra.

**Quyền**: STUDENT

**Request Body**:

```json
{
  "studentId": "STU001",
  "answers": [
    { "questionId": "1", "selectedOption": "A" },
    { "questionId": "2", "selectedOption": "C" }
  ]
}
```

| Field                      | Bắt buộc | Validation          |
| -------------------------- | :------: | ------------------- |
| `studentId`                |   Yes    | @NotBlank           |
| `answers`                  |   Yes    | @NotEmpty           |
| `answers[].questionId`     |   Yes    | @NotBlank           |
| `answers[].selectedOption` |   Yes    | @NotBlank (A/B/C/D) |

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "score": 8.0,
    "maxScore": 10.0,
    "status": "passed",
    "submittedAt": "2026-03-17T14:30:00"
  }
}
```

### 8.7. GET /api/quizzes/{quizId}/submissions

Danh sách bài nộp (cho giáo viên xem kết quả).

**Quyền**: TEACHER

**Response** `200`:

```json
{
  "success": true,
  "data": [
    {
      "submissionId": "1",
      "studentId": "STU001",
      "studentName": "Nguyễn Văn A",
      "studentEmail": "student@gmail.com",
      "score": 8.0,
      "maxScore": 10,
      "passed": true,
      "submittedAt": "2026-03-17T14:30:00"
    }
  ]
}
```

---

## 9. Module: Admin — Certificates

> **Mô tả**: Quản lý & xác minh chứng chỉ
> **Base**: `/api/admin/certificates`, `/api/certificates`
> **Quyền**: ADMIN

### 9.1. GET /api/admin/certificates/stats

Thống kê tổng quan.

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "totalCertificates": 150,
    "issuedCertificates": 120,
    "revokedCertificates": 5,
    "certificatesThisMonth": 12,
    "certificatesThisYear": 80
  }
}
```

### 9.2. GET /api/certificates/recent

Chứng chỉ gần đây (phân trang).

**Query params**:

| Param   | Default | Mô tả            |
| ------- | ------- | ---------------- |
| `page`  | 1       | Trang            |
| `limit` | 10      | Số lượng / trang |

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "certificateId": "CERT-001",
        "studentName": "Nguyễn Văn A",
        "studentEmail": "student@gmail.com",
        "className": "JAVA-01",
        "courseCode": "CS101",
        "averageScore": 8.5,
        "issuedAt": "2026-01-15T00:00:00Z",
        "status": "issued",
        "verificationHash": "0xabc123..."
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

### 9.3. GET /api/certificates/search

Tìm kiếm chứng chỉ.

**Query params**:

| Param    | Bắt buộc | Mô tả                              |
| -------- | :------: | ---------------------------------- |
| `q`      |   Yes    | Từ khóa (tên, email, mã lớp, hash) |
| `status` |    No    | `ISSUED`, `REVOKED`, `PENDING`     |
| `limit`  |    No    | Default 20                         |

**Response** `200`: Cùng cấu trúc 8.2.

### 9.4. GET /api/certificates/{certificateId}

Chi tiết chứng chỉ (kèm blockchain info + quiz results).

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT-001",
    "studentId": "STU001",
    "studentName": "Nguyễn Văn A",
    "studentEmail": "student@gmail.com",
    "classId": "CLS001",
    "className": "JAVA-01",
    "courseCode": "CS101",
    "courseName": "Lập trình Java",
    "averageScore": 8.5,
    "issuedAt": "2026-01-15T00:00:00Z",
    "status": "issued",
    "verificationHash": "0xabc123...",
    "blockchainInfo": {
      "transactionHash": "0xdef...",
      "blockNumber": "12345",
      "contractAddress": "0x789...",
      "networkName": "Ethereum Mainnet",
      "explorerUrl": "https://etherscan.io/tx/0xdef..."
    },
    "quizResults": [
      {
        "quizId": "QZ001",
        "quizName": "Quiz 1",
        "score": 9.0,
        "maxScore": 10,
        "completedAt": "2026-01-10T14:30:00Z"
      }
    ]
  }
}
```

### 9.5. POST /api/certificates/{certificateId}/verify

Xác minh chứng chỉ trên blockchain.

**Response thành công** `200`:

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT-001",
    "isValid": true,
    "verificationHash": "0xabc123...",
    "blockchainInfo": {
      "transactionHash": "0xdef...",
      "blockNumber": "12345",
      "contractAddress": "0x789...",
      "timestamp": "2026-01-15T00:00:00Z",
      "status": "confirmed"
    },
    "verifiedAt": "2026-03-17T10:00:00Z"
  }
}
```

**Response thất bại** `400`:

```json
{
  "success": false,
  "error": "Certificate verification failed: Hash mismatch"
}
```

### 9.6. POST /api/certificates/{certificateId}/revoke

Thu hồi chứng chỉ.

**Request Body**:

```json
{
  "reason": "Vi phạm quy chế thi",
  "revokedBy": "ADMIN-USER-ID"
}
```

| Field       | Bắt buộc | Validation                        |
| ----------- | :------: | --------------------------------- |
| `reason`    |   Yes    | @NotBlank                         |
| `revokedBy` |   Yes    | @NotBlank (phải là userId hợp lệ) |

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT-001",
    "status": "revoked",
    "revokedAt": "2026-03-17T10:00:00Z",
    "revokedByUserId": "ADMIN-USER-ID",
    "reason": "Vi phạm quy chế thi"
  }
}
```

---

## 10. Module: Admin — Users

> **Mô tả**: Quản lý người dùng
> **Base**: `/api/admin/users`
> **Quyền**: ADMIN

### 10.1. GET /api/admin/users

Danh sách người dùng (phân trang, lọc).

**Query params**:

| Param    | Default | Mô tả                         |
| -------- | ------- | ----------------------------- |
| `role`   | —       | `STUDENT`, `TEACHER`, `ADMIN` |
| `status` | —       | `active`, `inactive`          |
| `page`   | 1       | Trang                         |
| `limit`  | 20      | Số lượng / trang              |

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "userId": "STU001",
        "fullName": "Nguyễn Văn A",
        "email": "student@gmail.com",
        "role": "STUDENT",
        "avatarUrl": "https://...",
        "isActive": true,
        "createdAt": "2026-01-01T00:00:00Z",
        "lastLoginAt": "2026-03-17T08:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

### 10.2. PUT /api/admin/users/{userId}/status

Khóa / mở tài khoản.

**Request Body**:

```json
{
  "isActive": false,
  "reason": "Vi phạm nội quy"
}
```

| Field      | Bắt buộc | Validation            |
| ---------- | :------: | --------------------- |
| `isActive` |   Yes    | @NotNull (true/false) |
| `reason`   |    No    |                       |

**Response** `200`:

```json
{
  "success": true,
  "data": {
    "userId": "STU001",
    "isActive": false,
    "updatedAt": "2026-03-17T10:00:00Z"
  }
}
```

---

## 11. Test Strategy: Postman

### 11.1. Tổ chức Collection

```
LMS Backend/
├── 🟢 Environment/
│   ├── Local (base_url, student_token, teacher_token, admin_token)
│   └── Staging
│
├── 📁 01. Auth/
│   ├── GET /api/auth/me
│   ├── POST /api/auth/logout
│   └── GET /api/auth/check-role
│
├── 📁 02. Student — Enrollment/
│   ├── GET /api/student/{studentId}/courses
│   └── GET /api/student/{studentId}/certificates
│
├── 📁 03. Student — Course Detail/
│   └── GET /api/courses/{courseId}
│
├── 📁 04. Teacher — Classroom/
│   ├── GET /api/teacher/{teacherId}/classes
│   ├── GET /api/classes/{classId}
│   ├── GET /api/classes/{classId}/students
│   ├── GET /api/classes/{classId}/students?status=LEARNING&sort=averageScore&order=desc
│   ├── POST /api/classes
│   └── PUT /api/classes/{classId}
│
├── 📁 05. Teacher — Quiz CRUD/
│   ├── GET /api/classes/{classId}/quizzes
│   ├── GET /api/quizzes/{quizId}
│   ├── POST /api/quizzes
│   ├── PUT /api/quizzes/{quizId}
│   ├── DELETE /api/quizzes/{quizId}
│   └── GET /api/quizzes/{quizId}/submissions
│
├── 📁 06. Student — Quiz Submit/
│   └── POST /api/quizzes/{quizId}/submit
│
├── 📁 07. Admin — Certificates/
│   ├── GET /api/admin/certificates/stats
│   ├── GET /api/certificates/recent
│   ├── GET /api/certificates/search?q=...
│   ├── GET /api/certificates/{certificateId}
│   ├── POST /api/certificates/{certificateId}/verify
│   └── POST /api/certificates/{certificateId}/revoke
│
├── 📁 08. Admin — Users/
│   ├── GET /api/admin/users
│   ├── GET /api/admin/users?role=STUDENT&status=active
│   └── PUT /api/admin/users/{userId}/status
│
└── 📁 09. Error Cases/
    ├── 401 — Request không có token
    ├── 403 — Student gọi API admin
    ├── 403 — Teacher gọi API admin
    ├── 404 — courseId không tồn tại
    ├── 404 — quizId không tồn tại
    ├── 400 — Tạo quiz thiếu field bắt buộc
    └── 400 — Revoke certificate thiếu reason
```

### 11.2. Environment Variables

```json
{
  "base_url": "http://localhost:8080",
  "student_token": "<JWT của tài khoản STUDENT>",
  "teacher_token": "<JWT của tài khoản TEACHER>",
  "admin_token": "<JWT của tài khoản ADMIN>",
  "student_id": "<userId của student>",
  "teacher_id": "<userId của teacher>",
  "class_id": "<classId có sẵn>",
  "course_id": "<courseId có sẵn>",
  "quiz_id": "<quizId có sẵn>",
  "certificate_id": "<certificateId có sẵn>"
}
```

### 11.3. Test Script mẫu (Postman Tests tab)

**Kiểm tra response cơ bản:**

```javascript
pm.test("Status 200", () => pm.response.to.have.status(200));

pm.test("Success response", () => {
  const json = pm.response.json();
  pm.expect(json.success).to.be.true;
  pm.expect(json.data).to.not.be.null;
});
```

**Kiểm tra 401 khi không có token:**

```javascript
pm.test("Status 401 without token", () => {
  pm.response.to.have.status(401);
});
```

**Kiểm tra 403 khi sai role:**

```javascript
pm.test("Status 403 wrong role", () => {
  pm.response.to.have.status(403);
});
```

**Kiểm tra pagination:**

```javascript
pm.test("Pagination structure", () => {
  const json = pm.response.json();
  pm.expect(json.data.pagination).to.have.property("page");
  pm.expect(json.data.pagination).to.have.property("total");
  pm.expect(json.data.pagination).to.have.property("totalPages");
  pm.expect(json.data.items).to.be.an("array");
});
```

**Kiểm tra validation error:**

```javascript
pm.test("Status 400 validation", () => {
  pm.response.to.have.status(400);
  const json = pm.response.json();
  pm.expect(json.success).to.be.false;
  pm.expect(json.error).to.be.a("string");
});
```

### 11.4. Kịch bản test theo luồng nghiệp vụ

#### Luồng 1: Giáo viên tạo lớp + tạo quiz

```
1. [Teacher token] POST /api/classes → lưu classId
2. [Teacher token] GET /api/teacher/{teacherId}/classes → verify lớp mới xuất hiện
3. [Teacher token] POST /api/quizzes (classId = lớp vừa tạo) → lưu quizId
4. [Teacher token] GET /api/classes/{classId}/quizzes → verify quiz xuất hiện
5. [Teacher token] PUT /api/quizzes/{quizId} → sửa quiz
6. [Teacher token] GET /api/quizzes/{quizId} → verify đã cập nhật
```

#### Luồng 2: Học sinh xem khóa học + làm bài

```
1. [Student token] GET /api/student/{studentId}/courses → lưu courseId
2. [Student token] GET /api/courses/{courseId} → verify quizzes list, status = "pending"
3. [Student token] GET /api/quizzes/{quizId} → xem đề (không có đáp án)
4. [Student token] POST /api/quizzes/{quizId}/submit → lưu score
5. [Student token] GET /api/courses/{courseId} → verify quiz status = "completed"
```

#### Luồng 3: Admin quản lý chứng chỉ

```
1. [Admin token] GET /api/admin/certificates/stats → verify số liệu
2. [Admin token] GET /api/certificates/recent → danh sách gần đây
3. [Admin token] GET /api/certificates/search?q=student@gmail.com → tìm kiếm
4. [Admin token] GET /api/certificates/{certId} → chi tiết
5. [Admin token] POST /api/certificates/{certId}/verify → xác minh
6. [Admin token] POST /api/certificates/{certId}/revoke → thu hồi
7. [Admin token] GET /api/certificates/{certId} → verify status = "revoked"
```

#### Luồng 4: Kiểm tra phân quyền

```
1. [No token] GET /api/auth/me → expect 401
2. [Student token] POST /api/classes → expect 403
3. [Student token] GET /api/admin/users → expect 403
4. [Teacher token] GET /api/admin/users → expect 403
5. [Teacher token] POST /api/certificates/{certId}/revoke → expect 403
6. [Admin token] POST /api/quizzes/{quizId}/submit → expect 403
```

---

## 12. Test Strategy: JUnit

### 12.1. Cấu trúc test

```
src/test/java/main/backend/
├── auth/
│   ├── controller/AuthControllerTest.java
│   └── service/AuthServiceImplTest.java
│
├── classroom/
│   ├── controller/TeacherClassControllerTest.java
│   └── service/ClassServiceTest.java
│
├── course/
│   └── service/CourseServiceTest.java
│
├── quiz/
│   ├── controller/QuizControllerTest.java
│   └── service/QuizServiceTest.java
│
├── enrollment/
│   ├── controller/StudentControllerTest.java
│   └── service/StudentServiceTest.java
│
├── certificate/
│   └── service/CertificateServiceTest.java
│
├── admin/
│   ├── controller/AdminControllerTest.java
│   └── service/AdminServiceTest.java
│
└── integration/
    ├── QuizSubmitFlowTest.java
    └── CertificateRevokeFlowTest.java
```

### 12.2. Loại test và mục đích

| Loại                       | Framework             | Mục đích                                | Ví dụ                                   |
| -------------------------- | --------------------- | --------------------------------------- | --------------------------------------- |
| **Unit Test — Service**    | JUnit 5 + Mockito     | Test logic nghiệp vụ, mock repository   | QuizService.submitQuiz() tính điểm đúng |
| **Unit Test — Controller** | @WebMvcTest + MockMvc | Test HTTP mapping, validation, security | POST /api/quizzes thiếu field → 400     |
| **Integration Test**       | @SpringBootTest       | Test toàn luồng, có DB thật (H2)        | Tạo quiz → submit → verify score        |

### 12.3. Dependencies cần thêm (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 12.4. Test mẫu — Service (Unit Test)

```java
// quiz/service/QuizServiceTest.java

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @InjectMocks private QuizService quizService;
    @Mock private QuizRepository quizRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private UserQueryService userQueryService;

    @Test
    void submitQuiz_allCorrect_shouldReturnMaxScore() {
        // Arrange
        Quiz quiz = Quiz.builder()
                .quizId("QZ001")
                .passingScore(5.0)
                .status(QuizStatus.PUBLISHED)
                .build();

        Question q1 = new Question();
        q1.setQuiz(quiz);
        q1.setCorrectAnswer("A");

        when(quizRepository.findById("QZ001")).thenReturn(Optional.of(quiz));
        when(questionRepository.findByQuiz_QuizId("QZ001")).thenReturn(List.of(q1));
        when(userQueryService.getByIdOrThrow("STU001")).thenReturn(new User());

        QuizSubmissionRequest request = QuizSubmissionRequest.builder()
                .studentId("STU001")
                .answers(List.of(
                    QuizSubmissionRequest.AnswerRequest.builder()
                        .questionId("1")
                        .selectedOption("A")
                        .build()
                ))
                .build();

        // Act
        QuizResultResponse result = quizService.submitQuiz("QZ001", request);

        // Assert
        assertThat(result.getScore()).isEqualTo(10.0);
        assertThat(result.getStatus()).isEqualTo("passed");
        verify(quizAttemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    void submitQuiz_quizNotFound_shouldThrow404() {
        when(quizRepository.findById("INVALID")).thenReturn(Optional.empty());

        QuizSubmissionRequest request = QuizSubmissionRequest.builder()
                .studentId("STU001")
                .answers(List.of())
                .build();

        assertThatThrownBy(() -> quizService.submitQuiz("INVALID", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitQuiz_draftQuiz_shouldThrowBusinessException() {
        Quiz quiz = Quiz.builder()
                .quizId("QZ001")
                .status(QuizStatus.DRAFT)
                .build();
        when(quizRepository.findById("QZ001")).thenReturn(Optional.of(quiz));

        QuizSubmissionRequest request = QuizSubmissionRequest.builder()
                .studentId("STU001")
                .answers(List.of())
                .build();

        assertThatThrownBy(() -> quizService.submitQuiz("QZ001", request))
                .isInstanceOf(BusinessException.class);
    }
}
```

### 12.5. Test mẫu — Controller (WebMvcTest)

```java
// admin/controller/AdminControllerTest.java

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AdminService adminService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStats_asAdmin_shouldReturn200() throws Exception {
        CertificateStats stats = CertificateStats.builder()
                .totalCertificates(100L)
                .issuedCertificates(80L)
                .build();
        when(adminService.getCertificateStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/certificates/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCertificates").value(100));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getStats_asStudent_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/certificates/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStats_noAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/certificates/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void revokeCertificate_missingReason_shouldReturn400() throws Exception {
        // reason và revokedBy đều @NotBlank
        String body = """
            { "reason": "", "revokedBy": "" }
            """;

        mockMvc.perform(post("/api/certificates/CERT-001/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }
}
```

### 12.6. Test mẫu — Integration Test

```java
// integration/QuizSubmitFlowTest.java

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // rollback sau mỗi test
class QuizSubmitFlowTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private QuizRepository quizRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private QuizAttemptRepository quizAttemptRepository;

    @Test
    @WithMockUser(roles = "STUDENT")
    void fullFlow_createAndSubmitQuiz() throws Exception {
        // Giả sử DB test có sẵn quiz QZ-TEST với 2 câu hỏi
        // Student submit bài → verify điểm → verify attempt được lưu

        String body = """
            {
              "studentId": "STU-TEST",
              "answers": [
                { "questionId": "1", "selectedOption": "A" },
                { "questionId": "2", "selectedOption": "B" }
              ]
            }
            """;

        mockMvc.perform(post("/api/quizzes/QZ-TEST/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").isNumber())
                .andExpect(jsonPath("$.data.status").isString());

        // Verify attempt saved in DB
        List<QuizAttempt> attempts = quizAttemptRepository
                .findByStudentAndQuizOrderByScoreDesc("STU-TEST", "QZ-TEST");
        assertThat(attempts).hasSize(1);
    }
}
```

### 12.7. Test profile (application-test.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
  flyway:
    enabled: false

app:
  jwt:
    secret: test-secret-key-for-unit-tests-only-32chars
    expiration: 86400000
```

### 12.8. Ưu tiên viết test

| Ưu tiên | Module     | Test                                 | Lý do                                                  |
| :-----: | ---------- | ------------------------------------ | ------------------------------------------------------ |
|    1    | quiz       | `QuizService.submitQuiz()`           | Logic phức tạp nhất: tính điểm, pass/fail, lưu attempt |
|    2    | admin      | `AdminService.revokeCertificate()`   | Ảnh hưởng lớn: thay đổi trạng thái chứng chỉ           |
|    3    | admin      | `AdminController` — role check       | Bảo mật: chỉ ADMIN mới được gọi                        |
|    4    | classroom  | `ClassService.getTeacherClasses()`   | Phân quyền data: teacher chỉ thấy lớp mình             |
|    5    | quiz       | `QuizController` — validation        | Input validation: thiếu field → 400                    |
|    6    | enrollment | `StudentService.getStudentCourses()` | N+1 query đã fix, cần verify không regression          |
|    7    | course     | `CourseService.getCourseDetail()`    | Logic tính progress, quiz status                       |
|    8    | auth       | `AuthController.getCurrentUser()`    | Smoke test: endpoint cơ bản nhất                       |

---

## 13. Checklist tổng hợp

### Postman — Trước khi bàn giao

- [ ] Tất cả 25 endpoint đều có request trong Postman collection
- [ ] Mỗi endpoint có ít nhất 1 test script kiểm tra response
- [ ] 3 token (Student/Teacher/Admin) đã cấu hình trong Environment
- [ ] 4 luồng nghiệp vụ chạy thông (tạo lớp, làm bài, quản lý cert, phân quyền)
- [ ] Error cases: 401, 403, 404, 400 đều có test
- [ ] Pagination: verify cấu trúc `items` + `pagination`

### JUnit — Trước khi merge

- [ ] `QuizServiceTest` — tính điểm đúng, quiz not found, draft quiz reject
- [ ] `AdminServiceTest` — revoke cert, get stats, update user status
- [ ] `AdminControllerTest` — role check (ADMIN ok, STUDENT 403, no auth 401)
- [ ] `ClassServiceTest` — teacher chỉ thấy lớp mình
- [ ] `QuizControllerTest` — validation (@Valid: thiếu field → 400)
- [ ] `StudentServiceTest` — verify N+1 fix
- [ ] `CourseServiceTest` — progress calculation, quiz status
- [ ] Integration: quiz submit full flow
- [ ] `application-test.yml` cấu hình H2 in-memory database
- [ ] `mvn test` pass 100%
