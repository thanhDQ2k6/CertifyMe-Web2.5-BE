# API INTEGRATION REPORT - Frontend Implementation

> **Ngày cập nhật:** 2026-03-19
> **Trạng thái:** Frontend đã hoàn thành tích hợp API
> **Mục đích:** Hướng dẫn BE team test và verify API endpoints

---

## 1. TỔNG QUAN

### 1.1. Trạng thái hiện tại

| Tiêu chí        | Trạng thái                    |
| --------------- | ----------------------------- |
| API Integration | **Hoàn thành**                |
| Error Handling  | **Hoàn thành**                |
| Role-based Menu | **Hoàn thành**                |
| Mock Data       | **Đã xóa** - Sử dụng API thật |
| Build           | **Pass**                      |

### 1.2. Kiến trúc FE

```
src/
├── services/           # API calls (sử dụng apiFetcher)
│   ├── AuthService.js
│   ├── CourseService.js
│   ├── QuizService.js
│   ├── CertificateService.js
│   └── UserService.js
├── composables/        # Business logic + state management
│   ├── useAuth.js
│   ├── useStudent.js
│   ├── useTeacher.js
│   ├── useAdmin.js
│   └── useErrorHandler.js
├── views/              # Pages
│   ├── student/
│   ├── teacher/
│   └── admin/
└── lib/apiFetcher/     # Axios wrapper + interceptors
```

---

## 2. API ENDPOINTS - CHI TIẾT

### 2.1. Authentication APIs

| Method | Endpoint               | Service     | Mô tả                       |
| ------ | ---------------------- | ----------- | --------------------------- |
| GET    | `/api/auth/me`         | AuthService | Lấy thông tin user hiện tại |
| GET    | `/api/auth/check-role` | AuthService | Kiểm tra role               |
| POST   | `/api/auth/logout`     | AuthService | Đăng xuất                   |

#### Expected Response Format

**GET /api/auth/me**

```json
{
    "success": true,
    "data": {
        "userId": "uuid",
        "fullName": "Nguyễn Văn A",
        "email": "user@fpt.edu.vn",
        "role": "STUDENT",
        "avatarUrl": "https://...",
        "isActive": true
    },
    "message": null,
    "error": null
}
```

**JWT Token Payload** (decode từ token):

```json
{
    "sub": "userId",
    "email": "user@fpt.edu.vn",
    "role": "STUDENT",
    "iat": 1709123456,
    "exp": 1709209856
}
```

---

### 2.2. Student APIs

| Method | Endpoint                                | Service            | Mô tả                                |
| ------ | --------------------------------------- | ------------------ | ------------------------------------ |
| GET    | `/api/student/{studentId}/courses`      | CourseService      | Lấy danh sách khóa học của sinh viên |
| GET    | `/api/student/{studentId}/certificates` | CertificateService | Lấy danh sách chứng chỉ              |
| GET    | `/api/courses/{courseId}`               | CourseService      | Chi tiết khóa học                    |
| GET    | `/api/quizzes/{quizId}`                 | QuizService        | Chi tiết quiz (để làm bài)           |
| POST   | `/api/quizzes/{quizId}/submit`          | QuizService        | Nộp bài quiz                         |

#### Expected Response Format

**GET /api/student/{studentId}/courses**

```json
{
    "success": true,
    "data": [
        {
            "courseId": "uuid",
            "courseName": "Web Development",
            "courseCode": "WEB101",
            "courseIcon": "📚",
            "teacherName": "Nguyễn Văn B",
            "progress": 75,
            "totalQuizzes": 4,
            "completedQuizzes": 3,
            "isCompleted": false,
            "averageScore": 8.5
        }
    ],
    "error": null
}
```

**GET /api/student/{studentId}/certificates**

```json
{
    "success": true,
    "data": [
        {
            "id": "CERT-001",
            "courseName": "Web Development",
            "courseCode": "WEB101",
            "studentName": "Nguyễn Văn A",
            "grade": 8.5,
            "completionDate": "2026-03-15",
            "verificationHash": "0x1234...",
            "blockchainInfo": {
                "hash": "0x...",
                "block": 12345,
                "txHash": "0x...",
                "contract": "0x..."
            }
        }
    ],
    "error": null
}
```

**GET /api/courses/{courseId}** (Course Detail)

```json
{
    "success": true,
    "data": {
        "courseName": "Web Development",
        "courseCode": "WEB101",
        "courseIcon": "📚",
        "teacherName": "Nguyễn Văn B",
        "startDate": "2026-01-01",
        "endDate": "2026-06-30",
        "progress": 75,
        "totalQuizzes": 4,
        "completedQuizzes": 3,
        "isCompleted": false,
        "studentName": "Nguyễn Văn A",
        "quizzes": [
            {
                "id": "quiz-001",
                "name": "Quiz 1: HTML Basics",
                "status": "completed",
                "score": 9,
                "maxScore": 10
            },
            {
                "id": "quiz-002",
                "name": "Quiz 2: CSS",
                "status": "pending",
                "score": null,
                "maxScore": 10
            }
        ],
        "certificate": null
    },
    "error": null
}
```

**GET /api/quizzes/{quizId}** (Quiz để làm bài)

```json
{
    "success": true,
    "data": {
        "id": "quiz-001",
        "name": "Quiz 1: HTML Basics",
        "timeLimit": 30,
        "questions": [
            {
                "id": "q1",
                "questionText": "HTML là viết tắt của?",
                "questionType": "MULTIPLE_CHOICE",
                "options": ["HyperText Markup Language", "High Tech Modern Language", "Home Tool Markup Language", "Hyperlink Text Markup Language"]
            },
            {
                "id": "q2",
                "questionText": "Giải thích thẻ <div>",
                "questionType": "SHORT_ANSWER",
                "options": null
            }
        ]
    },
    "error": null
}
```

**POST /api/quizzes/{quizId}/submit**

Request:

```json
{
    "answers": [
        { "questionId": "q1", "answer": "HyperText Markup Language" },
        { "questionId": "q2", "answer": "Thẻ div dùng để..." }
    ]
}
```

Response:

```json
{
    "success": true,
    "data": {
        "score": 9,
        "totalPoints": 10,
        "isPassed": true,
        "timeTaken": 1200
    },
    "error": null
}
```

---

### 2.3. Teacher APIs

| Method | Endpoint                            | Service       | Mô tả                         |
| ------ | ----------------------------------- | ------------- | ----------------------------- |
| GET    | `/api/teacher/{teacherId}/classes`  | CourseService | Lấy danh sách lớp học         |
| GET    | `/api/classes/{classId}`            | CourseService | Chi tiết lớp học              |
| GET    | `/api/classes/{classId}/students`   | CourseService | Danh sách sinh viên trong lớp |
| GET    | `/api/classes/{classId}/quizzes`    | QuizService   | Danh sách quiz của lớp        |
| POST   | `/api/quizzes`                      | QuizService   | Tạo quiz mới                  |
| PUT    | `/api/quizzes/{quizId}`             | QuizService   | Cập nhật quiz                 |
| DELETE | `/api/quizzes/{quizId}`             | QuizService   | Xóa quiz                      |
| GET    | `/api/quizzes/{quizId}/submissions` | QuizService   | Xem bài nộp của quiz          |

#### Expected Response Format

**GET /api/teacher/{teacherId}/classes**

```json
{
    "success": true,
    "data": [
        {
            "id": "class-001",
            "code": "WEB101-A",
            "courseName": "Web Development",
            "studentCount": 30,
            "quizCount": 4,
            "status": "active"
        }
    ],
    "error": null
}
```

**GET /api/classes/{classId}**

```json
{
    "success": true,
    "data": {
        "id": "class-001",
        "code": "WEB101-A",
        "courseName": "Web Development",
        "courseId": "course-001"
    },
    "error": null
}
```

**GET /api/classes/{classId}/students**

```json
{
    "success": true,
    "data": [
        {
            "id": "student-001",
            "name": "Nguyễn Văn A",
            "email": "annv@fpt.edu.vn",
            "completedQuizzes": 3,
            "totalQuizzes": 4,
            "status": "learning"
        }
    ],
    "error": null
}
```

**GET /api/classes/{classId}/quizzes**

```json
{
    "success": true,
    "data": [
        {
            "id": "quiz-001",
            "name": "Quiz 1",
            "questionsCount": 10,
            "passingScore": 5,
            "status": "published"
        }
    ],
    "error": null
}
```

**POST /api/quizzes**

Request:

```json
{
    "classId": "class-001",
    "name": "Quiz 1",
    "duration": 30,
    "passingScore": 5.0,
    "questions": [
        {
            "text": "Question text?",
            "options": [
                { "label": "A", "value": "Option A", "isCorrect": true },
                { "label": "B", "value": "Option B", "isCorrect": false }
            ]
        }
    ]
}
```

**GET /api/quizzes/{quizId}/submissions**

```json
{
    "success": true,
    "data": [
        {
            "id": "sub-001",
            "studentName": "Nguyễn Văn A",
            "email": "annv@fpt.edu.vn",
            "score": 8,
            "maxScore": 10,
            "isPassed": true,
            "timeSpent": 1200,
            "submittedAt": "2026-03-15T10:30:00Z"
        }
    ],
    "error": null
}
```

---

### 2.4. Admin APIs

| Method | Endpoint                            | Service            | Mô tả               |
| ------ | ----------------------------------- | ------------------ | ------------------- |
| GET    | `/api/admin/users`                  | UserService        | Danh sách users     |
| GET    | `/api/admin/users/{userId}`         | UserService        | Chi tiết user       |
| PUT    | `/api/admin/users/{userId}/role`    | UserService        | Đổi role            |
| PUT    | `/api/admin/users/{userId}/status`  | UserService        | Đổi trạng thái      |
| DELETE | `/api/admin/users/{userId}`         | UserService        | Xóa user            |
| GET    | `/api/admin/certificates/stats`     | CertificateService | Thống kê chứng chỉ  |
| GET    | `/api/certificates/recent?limit=N`  | CertificateService | Chứng chỉ gần đây   |
| GET    | `/api/certificates/search?q=xxx`    | CertificateService | Tìm kiếm chứng chỉ  |
| GET    | `/api/certificates/{certId}`        | CertificateService | Chi tiết chứng chỉ  |
| POST   | `/api/certificates/{certId}/revoke` | CertificateService | Thu hồi chứng chỉ   |
| POST   | `/api/certificates/{certId}/verify` | CertificateService | Xác minh blockchain |
| GET    | `/api/certificates/{certId}/pdf`    | CertificateService | Tải PDF             |

#### Expected Response Format

**GET /api/admin/users**

```json
{
    "success": true,
    "data": [
        {
            "id": "user-001",
            "name": "Nguyễn Văn A",
            "email": "annv@fpt.edu.vn",
            "role": "STUDENT",
            "isActive": true,
            "lastLoginAt": "2026-03-15T10:00:00Z"
        }
    ],
    "error": null
}
```

**PUT /api/admin/users/{userId}/role**

Request:

```json
{ "role": "TEACHER" }
```

**PUT /api/admin/users/{userId}/status**

Request:

```json
{ "isActive": false }
```

**GET /api/admin/certificates/stats**

```json
{
    "success": true,
    "data": {
        "total": 150,
        "issued": 140,
        "revoked": 10
    },
    "error": null
}
```

**GET /api/certificates/recent?limit=20**

```json
{
    "success": true,
    "data": [
        {
            "id": "CERT-001",
            "studentName": "Nguyễn Văn A",
            "className": "WEB101-A",
            "issueDate": "2026-03-15",
            "status": "issued"
        }
    ],
    "error": null
}
```

**GET /api/certificates/{certId}**

```json
{
    "success": true,
    "data": {
        "id": "CERT-001",
        "studentName": "Nguyễn Văn A",
        "studentEmail": "annv@fpt.edu.vn",
        "className": "WEB101-A",
        "courseCode": "WEB101",
        "gradeAverage": 8.5,
        "issueDate": "2026-03-15",
        "status": "issued",
        "blockchainHash": "0x...",
        "blockchainInfo": {
            "hash": "0x...",
            "block": 12345,
            "txHash": "0x...",
            "contract": "0x..."
        }
    },
    "error": null
}
```

**POST /api/certificates/{certId}/revoke**

Request:

```json
{ "reason": "Gian lận trong kỳ thi" }
```

---

## 3. ERROR HANDLING

### 3.1. HTTP Status Codes

| Status | Ý nghĩa      | FE xử lý                |
| ------ | ------------ | ----------------------- |
| 200    | Thành công   | Hiển thị data           |
| 400    | Bad Request  | Toast error message     |
| 401    | Unauthorized | Redirect to /auth/login |
| 403    | Forbidden    | Toast "Không có quyền"  |
| 404    | Not Found    | Toast "Không tìm thấy"  |
| 409    | Conflict     | Toast error message     |
| 500    | Server Error | Toast "Lỗi server"      |

### 3.2. Error Response Format

```json
{
    "success": false,
    "data": null,
    "message": null,
    "error": "Mô tả lỗi cụ thể"
}
```

---

## 4. AUTHENTICATION FLOW

### 4.1. OAuth2 Flow

```
1. User click "Đăng nhập với Google"
   → FE redirect to: {BACKEND}/oauth2/authorization/google?role=STUDENT

2. Google OAuth flow (BE handles)

3. BE redirect to: {FE}/oauth2/redirect?token=JWT_TOKEN

4. FE extract token from URL
   → Save to localStorage
   → Decode JWT to get user info
   → Redirect theo role:
     - STUDENT → /student/dashboard
     - TEACHER → /teacher/dashboard
     - ADMIN → /admin/dashboard
```

### 4.2. JWT Token

- Lưu trong `localStorage` với key `authToken`
- Tự động attach vào header: `Authorization: Bearer {token}`
- FE decode JWT để lấy: `sub` (userId), `email`, `role`, `exp`

---

## 5. HƯỚNG DẪN TEST

### 5.1. Setup môi trường

```bash
# FE
cd CertifyMe-Web2.5-FE
npm install
npm run dev
# → http://localhost:5173

# BE
# Chạy backend trên port 8080
# → http://localhost:8080
```

### 5.2. Test Authentication

1. **Login flow:**
    - Truy cập http://localhost:5173/auth/login
    - Click "Đăng nhập với Google"
    - Verify redirect đúng role

2. **Protected routes:**
    - Thử truy cập /student/dashboard khi chưa login → Redirect /auth/login
    - Login với STUDENT → Verify chỉ thấy menu Student

3. **Logout:**
    - Click logout → Verify clear token + redirect login

### 5.3. Test Student Flow

1. **Dashboard:**
    - Login STUDENT
    - Verify load được danh sách courses từ `/api/student/{id}/courses`
    - Verify hiển thị progress đúng

2. **Course Detail:**
    - Click vào course → Load `/api/courses/{id}`
    - Verify quiz list hiển thị đúng

3. **Quiz:**
    - Start quiz → Load `/api/quizzes/{id}`
    - Submit → POST `/api/quizzes/{id}/submit`
    - Verify kết quả hiển thị

4. **Certificates:**
    - Menu "Chứng chỉ" → Load `/api/student/{id}/certificates`
    - Click verify → POST `/api/certificates/{id}/verify`

### 5.4. Test Teacher Flow

1. **Dashboard:**
    - Login TEACHER
    - Verify load `/api/teacher/{id}/classes`

2. **Class Detail:**
    - Click class → Load students + quizzes
    - Verify tabs hoạt động

3. **Quiz Management:**
    - Create quiz → POST `/api/quizzes`
    - Edit quiz → PUT `/api/quizzes/{id}`
    - Delete quiz → DELETE `/api/quizzes/{id}`

4. **Submissions:**
    - View submissions → GET `/api/quizzes/{id}/submissions`
    - Export CSV → Verify download

### 5.5. Test Admin Flow

1. **Dashboard:**
    - Login ADMIN
    - Load stats `/api/admin/certificates/stats`
    - Load recent certs `/api/certificates/recent`

2. **Certificate Detail:**
    - Click cert → Load `/api/certificates/{id}`
    - Revoke → POST `/api/certificates/{id}/revoke`

3. **User Management:**
    - Load users `/api/admin/users`
    - Change role → PUT `/api/admin/users/{id}/role`
    - Toggle status → PUT `/api/admin/users/{id}/status`

---

## 6. CHECKLIST CHO BE TEAM

### 6.1. Authentication

- [ ] GET /api/auth/me → Trả về ApiResponse<UserResponse>
- [ ] GET /api/auth/check-role → Trả về message role
- [ ] POST /api/auth/logout → Clear session (nếu có)
- [ ] JWT payload có: sub, email, role, exp

### 6.2. Student APIs

- [ ] GET /api/student/{studentId}/courses → Trả về array
- [ ] GET /api/student/{studentId}/certificates → Trả về array
- [ ] GET /api/courses/{courseId} → Trả về course detail + quizzes
- [ ] GET /api/quizzes/{quizId} → Trả về quiz + questions
- [ ] POST /api/quizzes/{quizId}/submit → Trả về result

### 6.3. Teacher APIs

- [ ] GET /api/teacher/{teacherId}/classes → Trả về array
- [ ] GET /api/classes/{classId} → Trả về class detail
- [ ] GET /api/classes/{classId}/students → Trả về array
- [ ] GET /api/classes/{classId}/quizzes → Trả về array
- [ ] POST /api/quizzes → Create quiz
- [ ] PUT /api/quizzes/{quizId} → Update quiz
- [ ] DELETE /api/quizzes/{quizId} → Delete quiz
- [ ] GET /api/quizzes/{quizId}/submissions → Trả về array

### 6.4. Admin APIs

- [ ] GET /api/admin/users → Trả về array hoặc paginated
- [ ] PUT /api/admin/users/{id}/role → Update role
- [ ] PUT /api/admin/users/{id}/status → Update isActive
- [ ] GET /api/admin/certificates/stats → Trả về stats object
- [ ] GET /api/certificates/recent?limit=N → Trả về array
- [ ] GET /api/certificates/search?q=xxx → Trả về array
- [ ] GET /api/certificates/{id} → Trả về detail
- [ ] POST /api/certificates/{id}/revoke → Revoke cert

### 6.5. Response Format

- [ ] Tất cả response đều có wrapper: `{ success, data, message, error }`
- [ ] Success: `success=true, data=<payload>, error=null`
- [ ] Error: `success=false, data=null, error=<message>`
- [ ] Role values: UPPERCASE (`STUDENT`, `TEACHER`, `ADMIN`)

---

## 7. KNOWN ISSUES & NOTES

1. **CORS:** Đảm bảo BE cho phép origin `http://localhost:5173`

2. **Token expiration:** FE decode JWT để check exp, tự động logout nếu hết hạn

3. **Authorization header:** FE tự động thêm `Bearer {token}` vào mọi request

4. **401 handling:** FE tự động redirect to login khi nhận 401

5. **Field naming:**
    - User: `userId` (NOT `id`), `fullName` (NOT `name`)
    - Role: UPPERCASE (`STUDENT`, `TEACHER`, `ADMIN`)

---

**Prepared by:** Claude AI
**Version:** 1.0
**Last Updated:** 2026-03-19
