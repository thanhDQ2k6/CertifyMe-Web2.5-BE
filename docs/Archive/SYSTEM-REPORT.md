# BÁO CÁO HỆ THỐNG LMS BACKEND

> **Ngày**: 13/03/2026
> **Dự án**: Learning Management System (LMS) - Backend
> **Nhánh**: `lms`

---

## 1. Tổng quan hệ thống

### Hệ thống là gì?

LMS Backend là phần máy chủ (server) của hệ thống **Quản lý học tập trực tuyến**, phục vụ 3 nhóm người dùng:

| Vai trò           | Mô tả                         | Chức năng chính                                                |
| ----------------- | ----------------------------- | -------------------------------------------------------------- |
| **Học sinh**      | Người học đăng ký khóa học    | Xem khóa học, làm bài kiểm tra, nhận chứng chỉ                 |
| **Giáo viên**     | Người tạo và quản lý nội dung | Tạo lớp học, tạo bài kiểm tra, xem kết quả                     |
| **Quản trị viên** | Quản lý toàn hệ thống         | Quản lý người dùng, cấp/thu hồi chứng chỉ, xác minh blockchain |

### Quy mô hệ thống

| Chỉ số                            | Giá trị                                                                                               |
| --------------------------------- | ----------------------------------------------------------------------------------------------------- |
| Tổng số API (giao diện lập trình) | 28 endpoint                                                                                           |
| Số module nghiệp vụ               | 7 (auth, course, classroom, quiz, enrollment, certificate, admin)                                     |
| Số bảng dữ liệu                   | 8 (users, roles, courses, classes, enrollments, quizzes, quiz_questions, quiz_attempts, certificates) |
| Nền tảng                          | Java 21, Spring Boot 3.2.3, MySQL 8                                                                   |

---

## 2. Các chức năng nghiệp vụ

### 2.1. Đăng nhập & Phân quyền

- Đăng nhập bằng **tài khoản Google** (OAuth2) — không cần tạo mật khẩu riêng
- Tài khoản mới đăng ký mặc định là **Học sinh**
- Quản trị viên có thể nâng cấp vai trò (Học sinh → Giáo viên, hoặc Quản trị viên)
- Mỗi phiên đăng nhập được cấp mã xác thực (JWT) có hiệu lực 24 giờ
- Hệ thống có khả năng **vô hiệu hóa tài khoản** mà không cần xóa dữ liệu

### 2.2. Quản lý Khóa học & Lớp học

| Chức năng               | Mô tả                                                                    |
| ----------------------- | ------------------------------------------------------------------------ |
| Tạo lớp học             | Giáo viên tạo lớp, gắn vào 1 khóa học, có ngày bắt đầu & kết thúc        |
| Xem danh sách lớp       | Giáo viên xem các lớp mình phụ trách, kèm số lượng học sinh thực tế      |
| Xem chi tiết lớp        | Thông tin lớp, danh sách học sinh, trạng thái từng em (đang học/đạt/rớt) |
| Xem khóa học (Học sinh) | Tiến độ học tập, danh sách bài kiểm tra, trạng thái chứng chỉ            |

**Trạng thái lớp học**: Đang hoạt động → Hoàn thành → Đã hủy

### 2.3. Bài kiểm tra (Quiz)

| Chức năng        | Vai trò   | Mô tả                                                      |
| ---------------- | --------- | ---------------------------------------------------------- |
| Tạo bài kiểm tra | Giáo viên | Đặt tiêu đề, thời gian, điểm đạt, thêm câu hỏi trắc nghiệm |
| Sửa bài kiểm tra | Giáo viên | Cập nhật nội dung, thay đổi câu hỏi                        |
| Xóa bài kiểm tra | Giáo viên | Xóa mềm (ẩn, không mất dữ liệu)                            |
| Làm bài          | Học sinh  | Gửi bài, hệ thống chấm điểm tự động, trả kết quả ngay      |
| Xem kết quả lớp  | Giáo viên | Danh sách bài nộp, điểm từng học sinh                      |

**Trạng thái bài kiểm tra**: Nháp → Đã công bố → Đã đóng

### 2.4. Ghi danh & Tiến độ học tập

- Học sinh ghi danh vào lớp học
- Hệ thống theo dõi trạng thái: **Đang học → Đạt / Rớt / Bỏ học**
- Điểm tổng kết (`finalGrade`) được tính và lưu trữ để cấp chứng chỉ

### 2.5. Chứng chỉ & Blockchain

| Chức năng          | Mô tả                                                        |
| ------------------ | ------------------------------------------------------------ |
| Cấp chứng chỉ      | Hệ thống cấp chứng chỉ dưới dạng số, ghi nhận lên blockchain |
| Xác minh chứng chỉ | Kiểm tra tính hợp lệ qua mã hash trên blockchain             |
| Thu hồi chứng chỉ  | Quản trị viên thu hồi kèm lý do, ghi nhận người thu hồi      |
| Thống kê chứng chỉ | Tổng số, số đã cấp, số thu hồi, số cấp trong tháng/năm       |

**Trạng thái chứng chỉ**: Chờ xử lý → Đã cấp → Đã thu hồi

### 2.6. Quản trị hệ thống

| Chức năng             | Mô tả                                                  |
| --------------------- | ------------------------------------------------------ |
| Quản lý người dùng    | Xem danh sách, lọc theo vai trò/trạng thái, phân trang |
| Vô hiệu hóa tài khoản | Khóa/mở tài khoản người dùng                           |
| Quản lý chứng chỉ     | Tìm kiếm, xem chi tiết, xác minh, thu hồi              |
| Thống kê              | Dashboard tổng quan về chứng chỉ đã cấp                |

---

## 3. Kiến trúc hệ thống

### 3.1. Mô hình tổng thể

Hệ thống sử dụng kiến trúc **Modular Monolith** — một ứng dụng duy nhất nhưng được tổ chức thành các module độc lập theo nghiệp vụ. Mô hình này phù hợp cho giai đoạn hiện tại vì:

- Đơn giản trong triển khai (1 server, 1 database)
- Dễ bảo trì nhờ ranh giới module rõ ràng
- Có thể tách thành microservices trong tương lai nếu cần mở rộng

### 3.2. Sơ đồ module

```
┌─────────────────────────────────────────────────────────┐
│                    LMS BACKEND                          │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐               │
│  │   Auth   │  │  Course  │  │  Admin   │               │
│  │ Đăng nhập│  │ Khóa học │  │ Quản trị │               │
│  └────┬─────┘  └────┬─────┘  └─────┬────┘               │
│       │             │              │                    │
│  ┌────▼─────┐  ┌────▼─────┐  ┌─────▼──────┐             │
│  │Enrollment│  │Classroom │  │Certificate │             │
│  │ Ghi danh │  │ Lớp học  │  │ Chứng chỉ  │             │
│  └────┬─────┘  └─────┬────┘  └────────────┘             │
│       │              │                                  │
│       │         ┌────▼────┐                             │
│       └─────────│  Quiz   │                             │
│                 │Kiểm tra │                             │
│                 └─────────┘                             │
│                                                         │
│  ┌──────────────────────────────────────────┐           │
│  │              Common (Dùng chung)         │           │
│  │  Bảo mật, Xử lý lỗi, Cấu hình, Tiện ích  │           │
│  └──────────────────────────────────────────┘           │
└─────────────────────────────────────────────────────────┘
```

### 3.3. Luồng xử lý một yêu cầu

```
Người dùng (Web/App)
       │
       ▼
  Xác thực JWT  ────── Nếu không hợp lệ → Từ chối (401)
       │
       ▼
  Kiểm tra quyền ───── Nếu không đủ quyền → Từ chối (403)
       │
       ▼
  Kiểm tra dữ liệu ── Nếu sai định dạng → Báo lỗi (400)
       │
       ▼
  Xử lý nghiệp vụ
       │
       ▼
  Trả kết quả (200/201)
```

---

## 4. Bảo mật

### 4.1. Các lớp bảo vệ

| Lớp                   | Cơ chế          | Mô tả                                           |
| --------------------- | --------------- | ----------------------------------------------- |
| **Đăng nhập**         | Google OAuth2   | Ủy quyền cho Google xác minh danh tính          |
| **Phiên đăng nhập**   | JWT (24h)       | Không lưu session trên server, dễ mở rộng       |
| **Phân quyền URL**    | Spring Security | Mỗi nhóm API chỉ cho phép đúng vai trò truy cập |
| **Phân quyền method** | @PreAuthorize   | Kiểm tra thêm ở từng chức năng cụ thể           |
| **Vô hiệu hóa**       | isActive flag   | Tài khoản bị khóa không thể đăng nhập           |

### 4.2. Ma trận phân quyền

| Nhóm API                         | Học sinh | Giáo viên | Quản trị viên |
| -------------------------------- | :------: | :-------: | :-----------: |
| Xem khóa học, làm bài            |    O     |     —     |       —       |
| Quản lý lớp, tạo bài kiểm tra    |    —     |     O     |       —       |
| Quản lý người dùng, chứng chỉ    |    —     |     —     |       O       |
| Đăng nhập, xem thông tin cá nhân |    O     |     O     |       O       |

---

## 5. Quá trình tái cấu trúc

### 5.1. Tình trạng ban đầu

Dự án được phát triển bởi nhiều thành viên. Module xác thực (Auth) và phần dùng chung (Common) được xây dựng tốt, nhưng các module nghiệp vụ khác có nhiều vấn đề:

| Vấn đề                                     | Mức độ       | Số lượng |
| ------------------------------------------ | ------------ | -------- |
| Lỗi nghiêm trọng (dữ liệu sai, bảo mật lộ) | Nghiêm trọng | 12       |
| Lỗi trung bình (logic thiếu, code cứng)    | Trung bình   | 18       |
| Hiệu năng kém (truy vấn thừa)              | Hiệu năng    | 5        |

**Ví dụ các lỗi nghiêm trọng đã phát hiện:**

- Tất cả API đều mở public — bất kỳ ai cũng truy cập được (không phân quyền)
- Giáo viên A xem được lớp của tất cả giáo viên khác
- Điểm trung bình chứng chỉ luôn trả về `8.5` thay vì tính từ dữ liệu thật
- Vai trò người dùng bị ghi đè mỗi lần đăng nhập lại
- Cấu trúc thư mục không nhất quán (PascalCase lẫn với lowercase)

### 5.2. Quá trình khắc phục (4 giai đoạn)

#### Giai đoạn 0: Xây nền tảng

- Xây dựng hệ thống xử lý lỗi chuyên biệt (thay vì lỗi chung chung)
- Thêm cơ chế phân trang cho danh sách dài
- Chuẩn hóa các giá trị trạng thái dùng chung

#### Giai đoạn 1: Sửa bảo mật

- Khôi phục phân quyền theo URL — chặn truy cập trái phép
- Sửa lỗi mất vai trò khi đăng nhập lại
- Kiểm tra trạng thái tài khoản (khóa/mở) khi xác thực

#### Giai đoạn 2: Tổ chức lại cấu trúc

- Đổi tên từ `LMSCourseService`, `LMSQuizService`... thành `course/`, `quiz/`...
- Tách module lớn thành module nhỏ hơn theo đúng chức năng
- Xóa toàn bộ code và thư mục cũ

#### Giai đoạn 3: Viết lại nghiệp vụ (6 module)

- Sửa tất cả 12 lỗi nghiêm trọng
- Tối ưu truy vấn database (loại bỏ truy vấn N+1)
- Đảm bảo dữ liệu trả về đúng và đầy đủ

#### Giai đoạn 4: Hoàn thiện

- Bật phân quyền trên toàn bộ 28 API (100% coverage)
- Thêm kiểm tra dữ liệu đầu vào
- Loại bỏ code thừa

### 5.3. Kết quả trước và sau

| Tiêu chí                       | Trước             | Sau                                     |
| ------------------------------ | ----------------- | --------------------------------------- |
| API có phân quyền              | 5/28 (18%)        | **28/28 (100%)**                        |
| Lỗi nghiêm trọng               | 12                | **0**                                   |
| Lỗi trung bình                 | 18                | **0**                                   |
| Cấu trúc module                | 4 tên không chuẩn | **7 module chuẩn**                      |
| `RuntimeException` (lỗi chung) | 8 chỗ             | **0**                                   |
| Dữ liệu viết cứng (hardcode)   | 6+ chỗ            | **0** (chỉ còn giá trị mặc định hợp lý) |
| Build (biên dịch)              | Pass              | **Pass**                                |

---

## 6. Danh sách API theo chức năng

### Đăng nhập (5 API)

| #   | Phương thức | Đường dẫn                      | Mô tả                                  |
| --- | ----------- | ------------------------------ | -------------------------------------- |
| 1   | GET         | `/oauth2/authorization/google` | Chuyển hướng đến Google đăng nhập      |
| 2   | GET         | `/oauth2/redirect`             | Nhận mã đăng nhập từ Google            |
| 3   | GET         | `/api/auth/me`                 | Xem thông tin tài khoản đang đăng nhập |
| 4   | POST        | `/api/auth/logout`             | Đăng xuất                              |
| 5   | GET         | `/api/auth/check-role`         | Kiểm tra vai trò hiện tại              |

### Học sinh (5 API)

| #   | Phương thức | Đường dẫn                        | Mô tả                              |
| --- | ----------- | -------------------------------- | ---------------------------------- |
| 6   | GET         | `/api/student/{id}/courses`      | Xem danh sách khóa học đã ghi danh |
| 7   | GET         | `/api/courses/{id}`              | Xem chi tiết khóa học & tiến độ    |
| 8   | GET         | `/api/student/{id}/certificates` | Xem danh sách chứng chỉ            |
| 9   | POST        | `/api/quizzes/{id}/submit`       | Nộp bài kiểm tra                   |
| 10  | GET         | `/api/quizzes/{id}`              | Xem chi tiết bài kiểm tra          |

### Giáo viên (10 API)

| #   | Phương thức | Đường dẫn                       | Mô tả                            |
| --- | ----------- | ------------------------------- | -------------------------------- |
| 11  | GET         | `/api/teacher/{id}/classes`     | Xem danh sách lớp phụ trách      |
| 12  | GET         | `/api/classes/{id}`             | Xem chi tiết lớp học             |
| 13  | GET         | `/api/classes/{id}/students`    | Xem danh sách học sinh trong lớp |
| 14  | POST        | `/api/classes`                  | Tạo lớp học mới                  |
| 15  | PUT         | `/api/classes/{id}`             | Cập nhật thông tin lớp           |
| 16  | GET         | `/api/classes/{id}/quizzes`     | Xem bài kiểm tra của lớp         |
| 17  | POST        | `/api/quizzes`                  | Tạo bài kiểm tra mới             |
| 18  | PUT         | `/api/quizzes/{id}`             | Sửa bài kiểm tra                 |
| 19  | DELETE      | `/api/quizzes/{id}`             | Xóa bài kiểm tra                 |
| 20  | GET         | `/api/quizzes/{id}/submissions` | Xem bài nộp của học sinh         |

### Quản trị viên (8 API)

| #   | Phương thức | Đường dẫn                       | Mô tả                              |
| --- | ----------- | ------------------------------- | ---------------------------------- |
| 21  | GET         | `/api/admin/certificates/stats` | Thống kê chứng chỉ                 |
| 22  | GET         | `/api/certificates/recent`      | Chứng chỉ gần đây                  |
| 23  | GET         | `/api/certificates/search`      | Tìm kiếm chứng chỉ                 |
| 24  | GET         | `/api/certificates/{id}`        | Chi tiết chứng chỉ                 |
| 25  | POST        | `/api/certificates/{id}/verify` | Xác minh chứng chỉ trên blockchain |
| 26  | POST        | `/api/certificates/{id}/revoke` | Thu hồi chứng chỉ                  |
| 27  | GET         | `/api/admin/users`              | Danh sách người dùng               |
| 28  | PUT         | `/api/admin/users/{id}/status`  | Khóa/mở tài khoản                  |

---

## 7. Cơ sở dữ liệu

### Sơ đồ quan hệ (đơn giản hóa)

```
  Vai trò (roles)
     │
     └──── Người dùng (users)
                │
                ├──── Ghi danh (enrollments) ──── Lớp học (classes) ──── Khóa học (courses)
                │
                ├──── Bài nộp (quiz_attempts) ──── Bài kiểm tra (quizzes) ──── Lớp học
                │                                       │
                │                                  Câu hỏi (quiz_questions)
                │
                └──── Chứng chỉ (certificates) ──── Lớp học
```

### Các bảng chính

| Bảng             | Mô tả                | Số trường quan trọng                                                    |
| ---------------- | -------------------- | ----------------------------------------------------------------------- |
| `users`          | Thông tin người dùng | userId, email, fullName, role, isActive                                 |
| `courses`        | Khóa học             | courseId, courseCode, courseName                                        |
| `classes`        | Lớp học              | classId, classCode, teacher, course, startDate, endDate, status         |
| `enrollments`    | Ghi danh             | student, class, status, finalGrade                                      |
| `quizzes`        | Bài kiểm tra         | quizId, class, title, duration, passingScore, status                    |
| `quiz_questions` | Câu hỏi              | questionId, quiz, content, options, correctAnswer                       |
| `quiz_attempts`  | Bài nộp              | student, quiz, score, answers, submittedAt                              |
| `certificates`   | Chứng chỉ            | certificateId, student, class, status, certificateHash, transactionHash |

---

## 8. Đánh giá hiện trạng và hướng phát triển

### 8.1. Hiện trạng

| Hạng mục                     |  Trạng thái  | Ghi chú                                    |
| ---------------------------- | :----------: | ------------------------------------------ |
| Kiến trúc hệ thống           |     Tốt      | 7 module rõ ràng, phân lớp đúng            |
| Bảo mật                      |     Tốt      | 100% API có phân quyền                     |
| Xử lý lỗi                    |     Tốt      | Lỗi chi tiết, mã HTTP chính xác            |
| Hiệu năng truy vấn           |     Tốt      | Đã loại bỏ truy vấn thừa                   |
| Kiểm tra dữ liệu đầu vào     |    Cơ bản    | Đã có validation trên request DTOs         |
| Kiểm thử tự động (Unit test) |   Chưa có    | Cần bổ sung                                |
| Database migration           | Chuẩn bị sẵn | Flyway đã thêm vào, chưa có file migration |

### 8.2. Khuyến nghị tiếp theo

| Ưu tiên | Hạng mục                     | Mô tả                                                                       |
| :-----: | ---------------------------- | --------------------------------------------------------------------------- |
|    1    | **Unit & Integration Test**  | Viết test cho các service quan trọng (quiz submit, enrollment, cert revoke) |
|    2    | **Flyway Migration**         | Tạo file migration để quản lý thay đổi DB có kiểm soát                      |
|    3    | **API cho admin chỉnh role** | Endpoint để admin đổi vai trò người dùng (Học sinh → Giáo viên)             |
|    4    | **Logging & Monitoring**     | Ghi log truy cập, cảnh báo lỗi để vận hành sản phẩm                         |
|    5    | **Externalize secrets**      | Chuyển mật khẩu DB, JWT secret ra biến môi trường (không để trong code)     |
|    6    | **API Documentation**        | Tích hợp Swagger/OpenAPI để Frontend dễ tích hợp                            |

---

## 9. Tóm tắt

Hệ thống LMS Backend đã hoàn thành giai đoạn tái cấu trúc toàn diện:

- **28 API** phục vụ đầy đủ 3 nhóm người dùng (Học sinh, Giáo viên, Quản trị viên)
- **12 lỗi nghiêm trọng** đã được khắc phục triệt để
- **100% API** được bảo vệ bằng phân quyền
- Kiến trúc **7 module** rõ ràng, dễ bảo trì và mở rộng
- Tích hợp **Blockchain** cho chứng chỉ số (xác minh, chống giả mạo)

Hệ thống sẵn sàng cho giai đoạn **kiểm thử tích hợp** (integration testing) với Frontend và triển khai thử nghiệm.
