# Code Audit Report - Backend LMS

> **Ngày audit**: 2026-03-12
> **Branch**: `lms`
> **Phạm vi**: Toàn bộ `src/main/java/main/backend/`

---

## Mục lục

1. [Tổng quan cấu trúc](#1-tổng-quan-cấu-trúc)
2. [Đánh giá Auth + Common (mẫu mực)](#2-đánh-giá-auth--common-mẫu-mực)
3. [Đánh giá các service do thành viên làm](#3-đánh-giá-các-service-do-thành-viên-làm)
4. [Phân tích Database vs Code](#4-phân-tích-database-vs-code)
5. [Bản đồ liên kết Cross-Module](#5-bản-đồ-liên-kết-cross-module)
6. [Tổng kết và bảng điểm](#6-tổng-kết-và-bảng-điểm)

---

## 1. Tổng quan cấu trúc

```
src/main/java/main/backend/
├── auth/                              ← MẪU MỰC (16 files)
│   ├── controller/AuthController.java
│   ├── dto/AuthResponse, GoogleUserInfo, UserResponse
│   ├── entity/Role, User
│   ├── enums/RoleType
│   ├── repository/RoleRepository, UserRepository
│   ├── security/CustomOAuth2UserService, JwtAuthFilter, JwtTokenProvider,
│   │           OAuth2SuccessHandler, UserPrincipal
│   └── service/AuthService (interface) + impl/AuthServiceImpl
│
├── common/                            ← MẪU MỰC (4 files)
│   ├── config/SecurityConfig
│   ├── dto/ApiResponse<T>
│   ├── exception/GlobalExceptionHandler
│   └── util/IdGenerator
│
├── constant/                          ← 5 enum files
│   ├── CertificateStatus, ClassStatus, EnrollmentStatus
│   ├── QuizStatus, RoleName
│
├── lms/dto/request/                   ← 3 shared request DTOs
│   ├── ClassRequestDTO, QuizRequestDTO, QuizSubmissionRequest
│
├── LMSCourseService/                  ← Thành viên (12 files)
│   ├── controller/CourseController, TeacherClassController
│   ├── dto/response/6 DTOs
│   ├── model/ClassEntity, CourseEntity
│   ├── repository/ClassRepository, CourseRepository
│   └── service/ClassService, CourseService
│
├── LMSQuizService/                    ← Thành viên (8 files)
│   ├── controller/QuizController
│   ├── dto/response/QuizResultResponse
│   ├── model/Quiz, Question, QuizAttempt
│   ├── repository/QuizRepo, QuestionRepo, QuizAttemptRepo
│   └── service/QuizService
│
├── LMSLearningService/                ← Thành viên (4 files)
│   ├── controller/StudentController
│   ├── model/Enrollment
│   ├── repository/EnrollmentRepository
│   └── service/StudentService
│
└── LMSCertificateBlockchainService/   ← Thành viên (7 files)
    ├── controller/AdminController
    ├── dto/response/AdminResponseDTO, CertificateResponse
    ├── model/Certificate
    ├── repository/CertificateRepository
    └── service/AdminService, CertificateService
```

**Database**: MySQL `lms_database`, 9 bảng, Flyway migrations (V1-V4).

---

## 2. Đánh giá Auth + Common (mẫu mực)

### 2.1. Auth Service — 7.5/10

**Điểm mạnh:**

- Kiến trúc layered chuẩn: Controller → Service (interface + impl) → Repository → Entity
- OAuth2 Google + JWT stateless hoàn chỉnh
- Lombok, Builder pattern nhất quán
- DTO field naming đúng chuẩn camelCase theo BE-Integration-Guide

**Vấn đề:**

| #   | Mức độ | Mô tả                                                                                                         |
| --- | ------ | ------------------------------------------------------------------------------------------------------------- |
| A1  | HIGH   | `CustomOAuth2UserService.updateExistingUser()` ghi đè role → STUDENT mỗi lần login. TEACHER/ADMIN bị hạ quyền |
| A2  | MEDIUM | `UserPrincipal.isEnabled()` luôn trả `true`, bỏ qua `user.isActive`                                           |
| A3  | MEDIUM | `AuthResponse.java` và `GoogleUserInfo.java` là dead code                                                     |
| A4  | MEDIUM | DB hit mỗi request trong `JwtAuthenticationFilter` (không cache)                                              |
| A5  | LOW    | Duplicate find-or-create logic giữa `CustomOAuth2UserService` và `OAuth2SuccessHandler`                       |
| A6  | LOW    | Bare `RuntimeException` thay vì custom exceptions                                                             |

### 2.2. Common Module — 8/10

**Điểm mạnh:**

- `ApiResponse<T>` wrapper chuẩn với factory methods
- `GlobalExceptionHandler` bắt 4 loại exception → đúng HTTP status
- `IdGenerator` tạo ID có prefix rõ ràng (CRS*, CLS*, QZ*, CERT*)

**Vấn đề:**

| #   | Mức độ   | Mô tả                                                                               |
| --- | -------- | ----------------------------------------------------------------------------------- |
| C1  | CRITICAL | `SecurityConfig`: `.requestMatchers("/api/**").permitAll()` → TẤT CẢ API đều public |
| C2  | LOW      | `GlobalExceptionHandler` dùng `ex.printStackTrace()` thay vì SLF4J                  |
| C3  | LOW      | `generateCourseId()` và `generateCertificateId()` không ai gọi                      |

---

## 3. Đánh giá các service do thành viên làm

### 3.1. LMSCourseService — 3.5/10

| #    | Mức độ   | File:Line               | Mô tả                                                                                |
| ---- | -------- | ----------------------- | ------------------------------------------------------------------------------------ |
| CS1  | CRITICAL | `ClassService:28-29`    | `getTeacherClasses()` bỏ qua `teacherId`, gọi `findAll()` trả TẤT CẢ lớp             |
| CS2  | CRITICAL | `CourseService:68`      | `courseCode` lấy từ `classCode` thay vì `courseEntity.courseCode`                    |
| CS3  | MEDIUM   | `ClassService:104`      | `studentCount` hardcode = 30                                                         |
| CS4  | MEDIUM   | `ClassService:38`       | `getStudentsInClass()` nhận params sort/filter nhưng không dùng                      |
| CS5  | PERF     | `CourseService:39`      | `quizRepository.findAll()` load toàn bộ quiz, filter trong memory                    |
| CS6  | PERF     | `CourseService:44`      | N+1: `findByStudent_UserId()` gọi trong loop mỗi quiz                                |
| CS7  | LOW      | —                       | `CourseResponse`, `QuizResponseDTO`, `QuizSubmissionResponseDTO` = dead code         |
| CS8  | LOW      | —                       | DI không nhất quán: `@RequiredArgsConstructor` vs `@Autowired`                       |
| CS9  | LOW      | —                       | Controller return type lẫn lộn: `ApiResponse<T>` vs `ResponseEntity<ApiResponse<T>>` |
| CS10 | LOW      | `CourseService:102-105` | `determineQuizStatus()` không bao giờ trả `"locked"`                                 |

### 3.2. LMSQuizService — 3/10

| #    | Mức độ   | File:Line                  | Mô tả                                                                   |
| ---- | -------- | -------------------------- | ----------------------------------------------------------------------- |
| QS1  | CRITICAL | `QuizService:43`           | `getQuizzesByClassResponse()` bỏ qua `classId`, gọi `findAll()`         |
| QS2  | CRITICAL | `QuizAttemptRepository:12` | Generic type `<QuizAttempt, String>` nhưng PK là `Long` → type mismatch |
| QS3  | MEDIUM   | `QuizService:50`           | Quiz status hardcode = `"active"`, quiz đã xóa mềm vẫn hiển thị         |
| QS4  | MEDIUM   | `QuizService:148-158`      | `updateQuiz()` chỉ update `title`, bỏ qua mọi field khác                |
| QS5  | MEDIUM   | `QuizService:255`          | Passing score hardcode `5.0` thay vì `quiz.getPassingScore()`           |
| QS6  | MEDIUM   | `QuizService:223-229`      | `isBestAttempt`, `startedAt` không bao giờ set                          |
| QS7  | MEDIUM   | —                          | `maxAttempts` field tồn tại nhưng không enforce                         |
| QS8  | LOW      | `QuizService:105-107`      | Missing correctAnswer mặc định về A thay vì báo lỗi                     |
| QS9  | LOW      | `QuizController:27`        | `ResponseEntity<?>` thay vì typed generic                               |
| QS10 | LOW      | `QuizController:24`        | `StudentService` inject nhưng không ai gọi                              |

### 3.3. LMSLearningService — 4/10

| #   | Mức độ   | File:Line                 | Mô tả                                                                         |
| --- | -------- | ------------------------- | ----------------------------------------------------------------------------- |
| LS1 | CRITICAL | `StudentController:22,29` | `@PreAuthorize` comment out → mọi endpoint public                             |
| LS2 | PERF     | `StudentService:40`       | N+1: `findByStudent_UserId()` gọi N lần (1 lần/enrollment)                    |
| LS3 | MEDIUM   | `StudentService:78-89`    | `getStudentCertificates()` là dead code (controller gọi `CertificateService`) |
| LS4 | MEDIUM   | `Enrollment:32`           | `passedQuizzes` field không ai đọc/update                                     |
| LS5 | LOW      | `StudentService:64-74`    | `courseIcon` không bao giờ set                                                |
| LS6 | LOW      | `StudentService:30-31`    | `User student` fetch xong không dùng                                          |

### 3.4. LMSCertificateBlockchainService — 3/10

| #    | Mức độ   | File:Line                  | Mô tả                                                                                                   |
| ---- | -------- | -------------------------- | ------------------------------------------------------------------------------------------------------- |
| CB1  | CRITICAL | `AdminService:59,137`      | `averageScore` hardcode = 8.5                                                                           |
| CB2  | CRITICAL | —                          | Không có `@PreAuthorize` → admin endpoints public                                                       |
| CB3  | CRITICAL | `CertificateService:42-51` | `getCertificatesByStudentId()` thiếu fields: certificateId, averageScore, status, blockchainInfo = null |
| CB4  | PERF     | `CertificateService:27`    | `classRepository.findAll()` load ALL classes                                                            |
| CB5  | MEDIUM   | `CertificateService:48`    | `courseCode` lấy nhầm từ `classCode`                                                                    |
| CB6  | MEDIUM   | `AdminService:77`          | `revokeCertificate()` trả raw `Map<String, Object>`                                                     |
| CB7  | MEDIUM   | `AdminService:84`          | `revokedBy` không validate → set null nếu user không tồn tại                                            |
| CB8  | MEDIUM   | `AdminService:183-187`     | Role ID hardcode (STUDENT=1, TEACHER=2, ADMIN=3)                                                        |
| CB9  | MEDIUM   | `AdminService:208`         | `lastLoginAt` lấy từ `updatedAt` (sai ngữ nghĩa)                                                        |
| CB10 | MEDIUM   | —                          | Blockchain verify chỉ là giả (check string non-empty)                                                   |
| CB11 | LOW      | —                          | `canIssueCertificate()` là dead code                                                                    |
| CB12 | LOW      | `Certificate.java`         | `@Data` trên JPA entity → equals/hashCode lỗi với lazy fields                                           |

---

## 4. Phân tích Database vs Code

### 4.1. Schema (Flyway V1-V4)

9 bảng: `roles`, `users`, `courses`, `classes`, `enrollments`, `quizzes`, `quiz_questions`, `quiz_attempts`, `certificates` + `organization_info`.

### 4.2. Mismatch nghiêm trọng

| #   | Vấn đề                        | Chi tiết                                                                                                                                                                   |
| --- | ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| DB1 | **ClassStatus enum mismatch** | Java: 5 values (ACTIVE, COMPLETED, CANCELED, UPCOMING, ONGOING). MySQL ENUM: 3 values (ACTIVE, COMPLETED, CANCELED). Persist UPCOMING/ONGOING → SQL error                  |
| DB2 | **Duplicate enums**           | `CertificateStatus`, `EnrollmentStatus`, `QuizStatus` tồn tại 2 nơi: `constant/` package VÀ inner enum trong entity. Entity dùng inner enum → constant package = dead code |
| DB3 | **RoleName vs RoleType**      | `constant/RoleName` = `ROLE_ADMIN,...` (có prefix). `auth/enums/RoleType` = `ADMIN,...` (không prefix). Entity dùng `RoleType` → `RoleName` = dead code                    |
| DB4 | **Flyway dependency thiếu**   | `pom.xml` không khai báo `flyway-core`/`flyway-mysql` dù application.properties enable Flyway                                                                              |
| DB5 | **Dead DTO fields**           | `ClassRequestDTO.courseName`, `ClassRequestDTO.description`, `QuizRequestDTO.maxScore`, `QuizRequestDTO.questionType` — không ai đọc                                       |
| DB6 | **Không validation**          | Không DTO nào có `@NotNull`, `@Size`, `@Valid`                                                                                                                             |

---

## 5. Bản đồ liên kết Cross-Module

```
LMSCourseService ──imports──> EnrollmentRepository (Learning)
                 ──imports──> QuizRepository, QuizAttemptRepository (Quiz)
                 ──imports──> CertificateRepository (Certificate)

LMSQuizService   ──imports──> StudentService (Learning)
                 ──imports──> UserRepository (Auth)
                 ──imports──> EnrollmentRepository (Learning)
                 ──imports──> QuizResponseDTO, QuizSubmissionResponseDTO (Course!)

LMSLearningService ──imports──> CertificateService (Certificate)
                   ──imports──> QuizAttemptRepository (Quiz)

LMSCertificateBlockchainService ──imports──> UserRepository (Auth)
                                ──imports──> QuizAttemptRepository (Quiz)
                                ──imports──> ClassRepository, CourseRepository (Course)
                                ──imports──> EnrollmentRepository (Learning)
```

**Kết luận**: Mọi service đều import trực tiếp repository/service của nhau → không có ranh giới module rõ ràng, không thể tách microservice.

---

## 6. Tổng kết và bảng điểm

### Bảng điểm

| Service                  | Hoàn thiện | Liên kết | Tối ưu | Trung bình |
| ------------------------ | ---------- | -------- | ------ | ---------- |
| Auth (mẫu)               | 7.5        | 8.0      | 7.0    | **7.5/10** |
| Common (mẫu)             | 8.0        | 9.0      | 7.5    | **8.0/10** |
| LMSCourseService         | 3.5        | 4.0      | 2.0    | **3.5/10** |
| LMSQuizService           | 3.0        | 3.0      | 3.0    | **3.0/10** |
| LMSLearningService       | 4.0        | 4.0      | 2.5    | **4.0/10** |
| LMSCertificateBlockchain | 3.0        | 3.5      | 2.0    | **3.0/10** |

### Thống kê lỗi

| Loại                | Số lượng |
| ------------------- | -------- |
| CRITICAL bugs       | 12       |
| MEDIUM bugs         | 18       |
| PERFORMANCE issues  | 5        |
| Dead code instances | 10+      |
| Inconsistencies     | 15+      |

### Top 5 ưu tiên sửa

1. **`findAll()` thay vì filter query** — 3 service gọi `findAll()` rồi filter Java → lộ data sai, hiệu năng tệ
2. **Security bypass toàn bộ** — `/api/**` permitAll, `@PreAuthorize` bị comment out
3. **Hardcoded values** — studentCount=30, averageScore=8.5, passingScore=5.0, status="active"
4. **Type mismatch** — `QuizAttemptRepository<QuizAttempt, String>` nhưng PK là `Long`
5. **ClassStatus enum** — 5 values Java vs 3 values MySQL → SQL error khi persist
