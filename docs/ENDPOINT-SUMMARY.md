# API Endpoints

Base URL: `http://localhost:8080`

Response format: `{ success, message, data, error }` → FE đọc `response.data.data`

---

## AUTH

| Method | Endpoint                       | Chức năng                         |
| ------ | ------------------------------ | --------------------------------- |
| GET    | `/oauth2/authorization/google` | Redirect tới Google OAuth         |
| GET    | `/api/auth/me`                 | Lấy thông tin user đang đăng nhập |
| POST   | `/api/auth/logout`             | Đăng xuất                         |
| GET    | `/api/auth/check-role`         | Kiểm tra role hiện tại            |

**User object:**

```json
{
  "userId": "uuid",
  "userCode": "HS00001",
  "email": "...",
  "fullName": "...",
  "avatarUrl": "...",
  "role": "STUDENT|TEACHER|ADMIN",
  "isActive": true
}
```

---

## STUDENT

| Method | Endpoint                                | Chức năng                              |
| ------ | --------------------------------------- | -------------------------------------- |
| GET    | `/api/student/{studentId}/courses`      | Dashboard: danh sách khóa học đang học |
| GET    | `/api/student/{studentId}/certificates` | Danh sách chứng chỉ đã nhận            |
| GET    | `/api/courses/{courseId}`               | Chi tiết khóa học                      |

**Flow học viên:**

1. Login → `/api/auth/me` → lấy `userId`
2. Load dashboard → `/api/student/{userId}/courses`
3. Chọn khóa → xem quizzes của class
4. Làm quiz → submit → xem kết quả
5. Hoàn thành → nhận certificate

---

## TEACHER

| Method | Endpoint                           | Chức năng                    |
| ------ | ---------------------------------- | ---------------------------- |
| GET    | `/api/teacher/{teacherId}/classes` | Danh sách lớp đang dạy       |
| GET    | `/api/classes/{classId}`           | Chi tiết lớp                 |
| POST   | `/api/classes`                     | Tạo lớp mới                  |
| PUT    | `/api/classes/{classId}`           | Cập nhật lớp                 |
| GET    | `/api/classes/{classId}/students`  | Danh sách học viên trong lớp |

**Query params cho `/students`:** `?status=LEARNING|PASSED&sort=name|score|date&order=asc|desc`

**Flow giáo viên:**

1. Login → `/api/auth/me` → lấy `userId`
2. Load dashboard → `/api/teacher/{userId}/classes`
3. Quản lý lớp: tạo/sửa class, xem students
4. Quản lý quiz: CRUD quiz, xem submissions
5. Thêm học viên: search student → enroll

---

## QUIZ

### Chung (Student + Teacher)

| Method | Endpoint                         | Chức năng               |
| ------ | -------------------------------- | ----------------------- |
| GET    | `/api/classes/{classId}/quizzes` | Danh sách quiz của lớp  |
| GET    | `/api/quizzes/{quizId}`          | Chi tiết quiz + câu hỏi |

### Student only

| Method | Endpoint                       | Chức năng               |
| ------ | ------------------------------ | ----------------------- |
| POST   | `/api/quizzes/{quizId}/submit` | Nộp bài quiz            |
| GET    | `/api/quizzes/{quizId}/result` | Xem kết quả quiz đã làm |

**Submit body:**

```json
{
  "studentId": "uuid",
  "answers": [
    { "questionId": "1", "selectedOption": "A" },
    { "questionId": "2", "selectedOption": "C" }
  ]
}
```

### Teacher only

| Method | Endpoint                            | Chức năng              |
| ------ | ----------------------------------- | ---------------------- |
| POST   | `/api/quizzes`                      | Tạo quiz mới           |
| PUT    | `/api/quizzes/{quizId}`             | Cập nhật quiz          |
| DELETE | `/api/quizzes/{quizId}`             | Xóa quiz (soft delete) |
| GET    | `/api/quizzes/{quizId}/submissions` | Xem danh sách bài nộp  |

---

## ENROLLMENT

| Method | Endpoint                                 | Role          | Chức năng                 |
| ------ | ---------------------------------------- | ------------- | ------------------------- |
| POST   | `/api/enrollments`                       | TEACHER/ADMIN | Ghi danh học viên vào lớp |
| DELETE | `/api/enrollments?studentCode=&classId=` | TEACHER/ADMIN | Xóa ghi danh              |

**Enroll body (chọn 1):**

```json
{ "studentCode": "HS00001", "classId": "CLS-001" }
// hoặc
{ "studentId": "uuid", "classId": "CLS-001" }
```

**Delete:** `DELETE /api/enrollments?studentCode=HS00001&classId=CLS-001`

---

## USER SEARCH

| Method | Endpoint                        | Role          | Chức năng                            |
| ------ | ------------------------------- | ------------- | ------------------------------------ |
| GET    | `/api/users/{idOrCode}`         | TEACHER/ADMIN | Tìm user bằng UUID hoặc mã (HS00001) |
| GET    | `/api/users/search/students?q=` | TEACHER/ADMIN | Tìm học viên theo keyword            |
| GET    | `/api/users/search/teachers?q=` | ADMIN         | Tìm giáo viên theo keyword           |

**Search query:** tìm trong `userCode`, `email`, `fullName`

---

## ADMIN - CERTIFICATES

| Method | Endpoint                                | Chức năng                       |
| ------ | --------------------------------------- | ------------------------------- |
| GET    | `/api/admin/certificates/stats`         | Thống kê tổng quan              |
| GET    | `/api/certificates/recent?limit=&page=` | Danh sách chứng chỉ mới nhất    |
| GET    | `/api/certificates/search?q=&status=`   | Tìm chứng chỉ                   |
| GET    | `/api/certificates/{id}`                | Chi tiết chứng chỉ              |
| POST   | `/api/certificates/{id}/verify`         | Xác minh chứng chỉ (blockchain) |
| POST   | `/api/certificates/{id}/revoke`         | Thu hồi chứng chỉ               |

---

## ADMIN - USERS

| Method | Endpoint                                      | Chức năng                  |
| ------ | --------------------------------------------- | -------------------------- |
| GET    | `/api/admin/users?role=&status=&page=&limit=` | Danh sách users            |
| PUT    | `/api/admin/users/{userId}/status`            | Kích hoạt/vô hiệu hóa user |
| PUT    | `/api/admin/users/{userId}/role`              | Đổi role user              |

---

## User Code Format

| Prefix | Role                | Ví dụ   |
| ------ | ------------------- | ------- |
| HS     | Học Sinh (Student)  | HS00001 |
| GV     | Giáo Viên (Teacher) | GV00001 |
| AD     | Admin               | AD00001 |

Auto-generated khi user đăng nhập Google lần đầu.

---

## HTTP Status

| Code | Ý nghĩa            |
| ---- | ------------------ |
| 200  | OK                 |
| 201  | Tạo thành công     |
| 400  | Request sai format |
| 401  | Chưa đăng nhập     |
| 403  | Không có quyền     |
| 404  | Không tìm thấy     |
| 500  | Lỗi server         |
