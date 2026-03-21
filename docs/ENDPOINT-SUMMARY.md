# Endpoint Summary (Ngắn gọn cho UI)

Nguồn tổng hợp từ code thực tế trong `src/main/java/main/backend/**/controller`.

## 1) Public / Auth

- `GET /oauth2/**`, `GET /login/**` (Spring Security OAuth2 flow, public)
- `GET /api/auth/me` (đăng nhập rồi mới lấy user)
- `POST /api/auth/logout`
- `GET /api/auth/check-role`

## 2) Student

- `GET /api/student/{studentId}/courses` → dashboard khóa học
- `GET /api/student/{studentId}/certificates` → danh sách chứng chỉ
- `GET /api/courses/{courseId}` → chi tiết course theo student context

## 3) Teacher (Classroom)

- `GET /api/teacher/{teacherId}/classes` → danh sách lớp giáo viên
- `GET /api/classes/{classId}` → chi tiết lớp
- `GET /api/classes/{classId}/students?status=&sort=&order=` → danh sách học viên lớp
- `POST /api/classes` → tạo lớp
- `PUT /api/classes/{id}` → cập nhật lớp

## 4) Quiz

- `GET /api/classes/{classId}/quizzes` (Student/Teacher)
- `GET /api/quizzes/{quizId}` (Student/Teacher)
- `POST /api/quizzes/{quizId}/submit` (Student)
- `GET /api/quizzes/{quizId}/result` (Student)
- `POST /api/quizzes` (Teacher)
- `PUT /api/quizzes/{quizId}` (Teacher)
- `DELETE /api/quizzes/{quizId}` (Teacher)
- `GET /api/quizzes/{quizId}/submissions` (Teacher)

## 5) Admin

- `GET /api/admin/certificates/stats`
- `GET /api/certificates/recent?limit=&page=`
- `GET /api/certificates/search?q=&status=&limit=`
- `GET /api/certificates/{certificateId}`
- `POST /api/certificates/{certificateId}/revoke`
- `POST /api/certificates/{certificateId}/verify`
- `GET /api/admin/users?role=&status=&page=&limit=`
- `PUT /api/admin/users/{userId}/status`
- `PUT /api/admin/users/{userId}/role`

## 6) Enrollment

- `POST /api/enrollments` (Teacher/Admin)
- `DELETE /api/enrollments/{enrollmentId}` (Teacher/Admin)

## 7) Gợi ý nhóm UI (để lên yêu cầu màn hình)

- **Auth**: login OAuth2, callback redirect, profile chip (`/api/auth/me`)
- **Student**: dashboard khóa học, chứng chỉ, làm quiz, xem kết quả quiz
- **Teacher**: quản lý lớp, học viên trong lớp, CRUD quiz, xem submissions
- **Admin**: dashboard chứng chỉ, tra cứu/verify/revoke chứng chỉ, quản lý user
- **Shared**: form enrollment cho Teacher/Admin

## 8) Lưu ý tích hợp FE

- Tất cả API business trả về wrapper: `{ success, message, data, error }`
- FE nên đọc dữ liệu theo `response.data.data`
- Role-based access được enforce bởi `@PreAuthorize` + `SecurityConfig`

## 9) Blockchain placeholder

- Chưa bật luồng blockchain thật (Sepolia).
- TODO chi tiết tại: `docs/BLOCKCHAIN-TODO-SEPOLIA.md`

## 10) UI direction

- Tài liệu định hướng giao diện (phân tích + ưu tiên theo phase): `docs/UI-DIRECTION-ROADMAP.md`
