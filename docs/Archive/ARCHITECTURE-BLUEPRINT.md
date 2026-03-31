# Architecture Blueprint - Backend LMS

> **Ngày tạo**: 2026-03-12
> **Mục đích**: Định nghĩa cấu trúc tổng thể (khung sườn) cho toàn bộ backend
> **Trạng thái**: PLAN - Chưa code, chỉ kiến trúc

---

## Mục lục

1. [Nguyên tắc thiết kế](#1-nguyên-tắc-thiết-kế)
2. [Cấu trúc thư mục đề xuất](#2-cấu-trúc-thư-mục-đề-xuất)
3. [Kiến trúc phân lớp (Layered)](#3-kiến-trúc-phân-lớp)
4. [Module Dependency Map](#4-module-dependency-map)
5. [Entity & Database Schema](#5-entity--database-schema)
6. [Security Architecture](#6-security-architecture)
7. [Exception Handling Strategy](#7-exception-handling-strategy)
8. [DTO Strategy](#8-dto-strategy)
9. [API Endpoint Map](#9-api-endpoint-map)
10. [Kế hoạch refactor từng bước](#10-kế-hoạch-refactor-từng-bước)

---

## 1. Nguyên tắc thiết kế

### 1.1. Core Principles

| Nguyên tắc                    | Mô tả                                                                                                   |
| ----------------------------- | ------------------------------------------------------------------------------------------------------- |
| **Modular Monolith**          | Một ứng dụng Spring Boot duy nhất, nhưng chia rõ ràng theo domain modules                               |
| **Unidirectional Dependency** | Module chỉ được phụ thuộc theo 1 chiều, KHÔNG circular. Giao tiếp qua service interface                 |
| **Repository Encapsulation**  | Repository KHÔNG được import trực tiếp từ module khác. Muốn data từ module khác → gọi service interface |
| **Consistent Patterns**       | Mọi module đều tuân theo cùng 1 pattern: Controller → Service (interface + impl) → Repository → Entity  |
| **Contract-First**            | API response format tuân thủ `ApiResponse<T>`, field naming theo BE-Integration-Guide                   |
| **Fail Fast**                 | Validation tại biên (controller), custom exceptions rõ ràng, không RuntimeException                     |

### 1.2. Quy ước đặt tên

| Đối tượng         | Quy ước                | Ví dụ                                                          |
| ----------------- | ---------------------- | -------------------------------------------------------------- |
| Package           | lowercase, theo domain | `auth`, `course`, `quiz`, `enrollment`, `certificate`, `admin` |
| Entity            | Singular noun          | `User`, `Course`, `ClassEntity`, `Quiz`                        |
| Repository        | Entity + `Repository`  | `CourseRepository`, `QuizRepository`                           |
| Service Interface | Domain + `Service`     | `CourseService`, `QuizService`                                 |
| Service Impl      | Interface + `Impl`     | `CourseServiceImpl`, `QuizServiceImpl`                         |
| Controller        | Domain + `Controller`  | `CourseController`, `QuizController`                           |
| Request DTO       | Action + `Request`     | `CreateClassRequest`, `SubmitQuizRequest`                      |
| Response DTO      | Domain + `Response`    | `CourseResponse`, `QuizDetailResponse`                         |
| Exception         | Domain + `Exception`   | `ResourceNotFoundException`, `ValidationException`             |
| Enum (shared)     | Domain + noun          | `ClassStatus`, `QuizStatus`                                    |

---

## 2. Cấu trúc thư mục đề xuất

```
src/main/java/main/backend/
│
├── BackendApplication.java
│
├── common/                          ← GIỮ NGUYÊN + bổ sung
│   ├── config/
│   │   ├── SecurityConfig.java         ← SỬA: restore proper auth rules
│   │   └── WebConfig.java              ← MỚI: CORS config riêng
│   ├── dto/
│   │   ├── ApiResponse.java            ← GIỮ NGUYÊN
│   │   └── PaginationResponse.java     ← MỚI: reusable pagination wrapper
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java ← SỬA: dùng SLF4J, thêm handlers
│   │   ├── ResourceNotFoundException.java    ← MỚI
│   │   ├── BusinessException.java            ← MỚI
│   │   ├── UnauthorizedException.java        ← MỚI
│   │   └── ValidationException.java          ← MỚI
│   ├── util/
│   │   └── IdGenerator.java            ← GIỮ NGUYÊN
│   └── enums/                           ← MỚI: tập trung enum dùng chung
│       ├── ClassStatus.java
│       ├── EnrollmentStatus.java
│       ├── QuizStatus.java
│       └── CertificateStatus.java
│
├── auth/                            ← GIỮ CẤU TRÚC + sửa bugs
│   ├── controller/
│   │   └── AuthController.java
│   ├── dto/
│   │   └── UserResponse.java          ← GIỮ (xóa dead DTOs)
│   ├── entity/
│   │   ├── User.java
│   │   └── Role.java
│   ├── enums/
│   │   └── RoleType.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── RoleRepository.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtTokenProvider.java
│   │   ├── CustomOAuth2UserService.java    ← SỬA: không ghi đè role
│   │   ├── OAuth2AuthenticationSuccessHandler.java  ← SỬA: xóa duplicate logic
│   │   └── UserPrincipal.java              ← SỬA: check isActive
│   └── service/
│       ├── AuthService.java (interface)
│       ├── UserQueryService.java (interface)  ← MỚI: cho module khác gọi
│       └── impl/
│           ├── AuthServiceImpl.java
│           └── UserQueryServiceImpl.java      ← MỚI
│
├── course/                          ← ĐỔI TÊN từ LMSCourseService
│   ├── controller/
│   │   └── CourseController.java       ← VIẾT LẠI: student course view
│   ├── dto/
│   │   ├── CourseListResponse.java     ← MỚI: thay CourseResponse
│   │   └── CourseDetailResponse.java   ← VIẾT LẠI
│   ├── entity/
│   │   └── CourseEntity.java           ← GIỮ + thêm @Builder
│   ├── repository/
│   │   └── CourseRepository.java       ← GIỮ
│   └── service/
│       ├── CourseService.java (interface)    ← MỚI
│       └── impl/
│           └── CourseServiceImpl.java        ← VIẾT LẠI
│
├── classroom/                       ← ĐỔI TÊN, tách từ LMSCourseService
│   ├── controller/
│   │   └── ClassController.java        ← VIẾT LẠI: teacher class management
│   ├── dto/
│   │   ├── CreateClassRequest.java     ← ĐỔI TÊN từ ClassRequestDTO
│   │   ├── UpdateClassRequest.java     ← MỚI: tách riêng update
│   │   ├── ClassListResponse.java      ← MỚI
│   │   ├── ClassDetailResponse.java    ← MỚI
│   │   └── ClassStudentResponse.java   ← MỚI
│   ├── entity/
│   │   └── ClassEntity.java           ← GIỮ
│   ├── repository/
│   │   └── ClassRepository.java       ← GIỮ + thêm query methods
│   └── service/
│       ├── ClassService.java (interface)    ← MỚI
│       └── impl/
│           └── ClassServiceImpl.java        ← VIẾT LẠI
│
├── enrollment/                      ← ĐỔI TÊN từ LMSLearningService
│   ├── controller/
│   │   └── StudentDashboardController.java  ← VIẾT LẠI
│   ├── dto/
│   │   └── StudentCourseResponse.java       ← MỚI
│   ├── entity/
│   │   └── Enrollment.java            ← GIỮ + xóa inner enum
│   ├── repository/
│   │   └── EnrollmentRepository.java  ← GIỮ
│   └── service/
│       ├── EnrollmentService.java (interface)    ← MỚI: expose cho module khác
│       └── impl/
│           └── EnrollmentServiceImpl.java        ← VIẾT LẠI
│
├── quiz/                            ← ĐỔI TÊN từ LMSQuizService
│   ├── controller/
│   │   ├── QuizController.java         ← VIẾT LẠI: teacher quiz CRUD
│   │   └── QuizSubmitController.java   ← MỚI: student submit
│   ├── dto/
│   │   ├── CreateQuizRequest.java      ← ĐỔI TÊN từ QuizRequestDTO
│   │   ├── UpdateQuizRequest.java      ← MỚI
│   │   ├── SubmitQuizRequest.java      ← ĐỔI TÊN từ QuizSubmissionRequest
│   │   ├── QuizListResponse.java       ← MỚI
│   │   ├── QuizDetailResponse.java     ← MỚI: student-facing (no answers)
│   │   ├── QuizSubmitResponse.java     ← MỚI
│   │   └── QuizSubmissionListResponse.java  ← MỚI: teacher-facing
│   ├── entity/
│   │   ├── Quiz.java                  ← GIỮ + xóa inner enum, thêm @Builder
│   │   ├── Question.java              ← GIỮ
│   │   └── QuizAttempt.java           ← GIỮ
│   ├── repository/
│   │   ├── QuizRepository.java        ← SỬA: thêm findByClassEntity_ClassId
│   │   ├── QuestionRepository.java    ← GIỮ
│   │   └── QuizAttemptRepository.java ← SỬA: fix generic type Long
│   └── service/
│       ├── QuizService.java (interface)            ← MỚI
│       ├── QuizSubmissionService.java (interface)  ← MỚI: tách submit logic
│       └── impl/
│           ├── QuizServiceImpl.java              ← VIẾT LẠI
│           └── QuizSubmissionServiceImpl.java    ← VIẾT LẠI
│
├── certificate/                     ← ĐỔI TÊN từ LMSCertificateBlockchainService
│   ├── controller/
│   │   └── CertificateController.java  ← VIẾT LẠI: student certificates
│   ├── dto/
│   │   ├── CertificateListResponse.java    ← MỚI
│   │   └── CertificateDetailResponse.java  ← MỚI
│   ├── entity/
│   │   └── Certificate.java           ← GIỮ + xóa inner enum, fix @Data
│   ├── repository/
│   │   └── CertificateRepository.java ← GIỮ
│   └── service/
│       ├── CertificateService.java (interface)    ← MỚI
│       ├── CertificateQueryService.java (interface)  ← MỚI: cho module khác
│       └── impl/
│           ├── CertificateServiceImpl.java        ← VIẾT LẠI
│           └── CertificateQueryServiceImpl.java   ← VIẾT LẠI
│
└── admin/                           ← ĐỔI TÊN, tách riêng admin logic
    ├── controller/
    │   └── AdminController.java        ← VIẾT LẠI
    ├── dto/
    │   ├── CertificateStatsResponse.java    ← MỚI
    │   ├── AdminCertificateResponse.java    ← MỚI
    │   ├── AdminCertificateDetailResponse.java  ← MỚI
    │   ├── CertificateVerifyResponse.java   ← MỚI
    │   ├── RevokeRequest.java               ← MỚI: typed
    │   ├── UserListResponse.java            ← MỚI
    │   └── UpdateUserStatusRequest.java     ← MỚI: typed
    └── service/
        ├── AdminCertificateService.java (interface)  ← MỚI
        ├── AdminUserService.java (interface)         ← MỚI
        └── impl/
            ├── AdminCertificateServiceImpl.java      ← VIẾT LẠI
            └── AdminUserServiceImpl.java             ← VIẾT LẠI
```

---

## 3. Kiến trúc phân lớp

### 3.1. Luồng xử lý request

```
HTTP Request
    │
    ▼
┌─────────────────────────────────┐
│  JwtAuthenticationFilter        │  ← Extract & validate JWT
│  (OncePerRequestFilter)         │  ← Set SecurityContext
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│  @PreAuthorize                  │  ← Role-based access check
│  (Method Security)              │  ← hasRole('STUDENT'), etc.
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│  Controller                     │  ← Nhận request, validate input (@Valid)
│  - Thin layer                   │  ← Gọi service, trả ApiResponse<T>
│  - Trả ResponseEntity           │  ← Set HTTP status code
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│  Service (Interface + Impl)     │  ← Business logic
│  - Transaction management       │  ← Entity ↔ DTO mapping
│  - Gọi repository CỦA MODULE    │  ← Gọi service interface module khác
│  - Throw custom exceptions      │
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│  Repository                     │  ← Spring Data JPA
│  - Custom JPQL queries          │  ← Chỉ expose cho service cùng module
│  - No findAll() abuse           │
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│  Entity (JPA)                   │  ← Map to MySQL table
│  - Flyway quản lý schema        │
└─────────────────────────────────┘

     [On Exception]
          │
          ▼
┌─────────────────────────────────┐
│  GlobalExceptionHandler         │  ← Bắt mọi exception
│  (@ControllerAdvice)            │  ← Map thành ApiResponse.error()
│                                 │  ← Set đúng HTTP status code
└─────────────────────────────────┘
```

### 3.2. Quy tắc từng lớp

| Lớp            | ĐƯỢC làm                                                                | KHÔNG ĐƯỢC làm                                              |
| -------------- | ----------------------------------------------------------------------- | ----------------------------------------------------------- |
| **Controller** | Nhận input, `@Valid`, gọi service, trả `ResponseEntity<ApiResponse<T>>` | Chứa business logic, gọi repository trực tiếp               |
| **Service**    | Business logic, gọi repo cùng module, gọi service interface khác module | Gọi repository module khác, throw `RuntimeException`        |
| **Repository** | JPQL queries, derived queries                                           | `findAll()` rồi filter Java, expose ra ngoài module         |
| **Entity**     | JPA mapping, lifecycle callbacks (@PrePersist)                          | Chứa business logic, dùng `@Data` (gây lỗi equals/hashCode) |
| **DTO**        | Data transfer, validation annotations                                   | Chứa logic, dùng chung giữa nhiều module                    |

---

## 4. Module Dependency Map

### 4.1. Dependency Rules

```
                  ┌──────────┐
                  │  common  │  ← Foundation: exception, dto, config, enums
                  └────┬─────┘
                       │ (mọi module đều phụ thuộc common)
         ┌─────────────┼─────────────────────┐
         │             │                     │
    ┌────▼────┐   ┌────▼─────┐          ┌────▼────┐
    │  auth   │   │  course  │          │  admin  │
    └────┬────┘   └────┬─────┘          └────┬────┘
         │             │                     │
         │        ┌────▼──────┐              │ (admin gọi service
         │        │ classroom │              │  interfaces từ
         │        └────┬──────┘              │  các module khác)
         │             │                     │
         │    ┌────────┼────────┐            │
         │    │        │        │            │
    ┌────▼────▼┐  ┌────▼───┐    │            │
    │enrollment│  │  quiz  │    │            │
    └────┬─────┘  └────┬───┘    │            │
         │             │        │            │
         └──────┬──────┘        │            │
                │               │            │
         ┌──────▼──────┐        │            │
         │ certificate │◄───────┴────────────┘
         └─────────────┘
```

### 4.2. Inter-Module Communication (CHỈ qua service interface)

| Caller Module           | Calls Interface                       | Purpose                            |
| ----------------------- | ------------------------------------- | ---------------------------------- |
| `enrollment`            | `auth.UserQueryService`               | Validate student exists            |
| `classroom`             | `auth.UserQueryService`               | Validate teacher exists            |
| `classroom`             | `course.CourseService`                | Validate course exists             |
| `classroom`             | `enrollment.EnrollmentService`        | Count students in class            |
| `quiz`                  | `classroom.ClassService`              | Validate class exists              |
| `quiz`                  | `enrollment.EnrollmentService`        | Update student progress            |
| `certificate`           | `enrollment.EnrollmentService`        | Check completion status            |
| `certificate`           | `quiz.QuizService`                    | Get quiz results for cert          |
| `admin`                 | `auth.UserQueryService`               | List/update users                  |
| `admin`                 | `certificate.CertificateQueryService` | Cert stats, detail, verify, revoke |
| `admin`                 | `quiz.QuizService`                    | Get quiz scores for cert detail    |
| `course` (student view) | `enrollment.EnrollmentService`        | Get enrollment data                |
| `course` (student view) | `quiz.QuizService`                    | Get quiz attempts                  |
| `course` (student view) | `certificate.CertificateQueryService` | Get certificate info               |

### 4.3. Forbidden Dependencies

| KHÔNG CHO PHÉP                 | Lý do                                                                 |
| ------------------------------ | --------------------------------------------------------------------- |
| Module A import `B.repository` | Vi phạm encapsulation. Phải gọi `B.service` interface                 |
| Module A import `B.entity`     | Entity là implementation detail. Trả DTO hoặc primitive qua interface |
| Circular: A → B → A            | Tách phần chung ra interface hoặc event                               |
| Controller gọi Repository      | Bỏ qua business logic layer                                           |

---

## 5. Entity & Database Schema

### 5.1. Entity Diagram (đã có trong DB)

```
┌─────────┐    1:N    ┌─────────┐
│  roles  │◄──────────│  users  │
└─────────┘           └────┬────┘
                           │
              ┌────────────┼────────────────┐
              │            │                │
         1:N  │       1:N  │           1:N  │
              ▼            ▼                ▼
        ┌───────────┐ ┌──────────┐  ┌──────────────┐
        │enrollments│ │quiz_     │  │ certificates │
        │           │ │attempts  │  │              │
        └─────┬─────┘ └────┬─────┘  └──────┬───────┘
              │            │               │
              │ N:1        │ N:1           │ N:1
              ▼            ▼               ▼
        ┌───────────┐ ┌──────────┐  ┌──────────────┐
        │  classes  │ │ quizzes  │  │   classes    │
        └─────┬─────┘ └────┬─────┘  └──────────────┘
              │            │
         N:1  │        N:1 │
              ▼            ▼
        ┌───────────┐ ┌──────────┐
        │  courses  │ │ classes  │──────► courses
        └───────────┘ └────┬─────┘
                           │
                      1:N  │
                           ▼
                    ┌──────────────┐
                    │quiz_questions│
                    └──────────────┘
```

### 5.2. Cần sửa trong DB (V5 migration)

| Sửa                        | Chi tiết                                                                                          |
| -------------------------- | ------------------------------------------------------------------------------------------------- |
| `classes.status` ENUM      | Thêm `'UPCOMING'`, `'ONGOING'` vào MySQL ENUM, **HOẶC** xóa 2 giá trị này khỏi Java enum cho khớp |
| `quiz_attempts.attempt_id` | Đảm bảo Java entity + repository đều dùng `Long`                                                  |
| `quizzes`                  | Cân nhắc thêm `max_score DOUBLE DEFAULT 10.0` nếu cần flex                                        |

//TODOS Xóa upcoming và ongoing. Chỉ thêm trạng thái nếu thật sự cần

### 5.3. Entity Annotation Standards

```java
// ĐÚNG: dùng cho mọi entity
@Entity
@Table(name = "table_name")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// SAI: KHÔNG dùng @Data trên JPA entity
// @Data  ← generates equals/hashCode over ALL fields including lazy relations
```

---

## 6. Security Architecture

### 6.1. Authentication Flow (giữ nguyên)

```
Browser → /oauth2/authorization/google → Google OAuth2
       ← redirect với ?token=JWT ← OAuth2SuccessHandler
                                      │
                                      ├──  findOrCreateUser()
                                      └──  generateToken(userId, email, role)

Mỗi request tiếp theo:
Authorization: Bearer <JWT>
       │
       ▼
JwtAuthenticationFilter
       │
       ├── validateToken()
       ├── getUserIdFromToken()
       ├── loadUser from DB
       ├── check isActive
       └── set SecurityContext
```

//TODOS Đăng ký mặc định role học sinh nhưng có endpoint edit role cho admin chỉnh tài khoản thành giáo viên hoặc admin khác

### 6.2. Authorization Matrix

```
SecurityConfig.filterChain:
    /oauth2/**, /login/**                         → permitAll
    /api/auth/**                                  → permitAll
    /api/student/**, /api/courses/*, /api/quizzes/* (GET, POST submit) → hasRole('STUDENT')
    /api/teacher/**, /api/classes/**, /api/quizzes/** (CRUD)           → hasRole('TEACHER')
    /api/admin/**, /api/certificates/** (admin ops)                    → hasRole('ADMIN')
    /**                                           → authenticated
```

### 6.3. Sửa lỗi cần làm

| #   | Hiện trạng                         | Cần sửa                                          |
| --- | ---------------------------------- | ------------------------------------------------ |
| 1   | `/api/**` permitAll                | Restore proper role-based rules theo matrix trên |
| 2   | `@PreAuthorize` comment out        | Bật lại ở TẤT CẢ controller methods              |
| 3   | `isEnabled()` luôn true            | Check `user.getIsActive()`                       |
| 4   | `updateExistingUser()` ghi đè role | KHÔNG ghi đè role khi update                     |
| 5   | Duplicate find-or-create           | Chỉ giữ 1 nơi (trong `CustomOAuth2UserService`)  |

---

## 7. Exception Handling Strategy

### 7.1. Custom Exception Hierarchy

```
                    RuntimeException
                         │
              ┌──────────┴──────────┐
              │                     │
     BusinessException      ResourceNotFoundException
     (400 Bad Request)       (404 Not Found)
              │
    ┌─────────┤
    │         │
ValidationEx  UnauthorizedException
(400)         (401)
```

### 7.2. GlobalExceptionHandler Mapping

| Exception                   | HTTP Status | Error Message                         |
| --------------------------- | ----------- | ------------------------------------- |
| `ResourceNotFoundException` | 404         | `ex.getMessage()`                     |
| `BusinessException`         | 400         | `ex.getMessage()`                     |
| `ValidationException`       | 400         | `"Validation failed"` + field details |
| `AccessDeniedException`     | 403         | `"Access denied"`                     |
| `IllegalArgumentException`  | 400         | `ex.getMessage()`                     |
| `Exception` (catch-all)     | 500         | `"An unexpected error occurred"`      |

### 7.3. Quy tắc throw exception

```java
// ĐÚNG
throw new ResourceNotFoundException("User not found with id: " + userId);
throw new BusinessException("Quiz is not published");
throw new ValidationException("email", "Email is required");

// SAI
throw new RuntimeException("User not found");  // ← không rõ ràng, trả 500
```

---

## 8. DTO Strategy

### 8.1. Quy tắc

| Quy tắc                          | Chi tiết                                              |
| -------------------------------- | ----------------------------------------------------- |
| **Mỗi module sở hữu DTOs riêng** | Không import DTO module khác                          |
| **Tách Request / Response**      | `CreateXxxRequest`, `UpdateXxxRequest`, `XxxResponse` |
| **Validation trên Request DTOs** | `@NotBlank`, `@NotNull`, `@Size`, `@Min`, `@Max`      |
| **Controller dùng @Valid**       | `@Valid @RequestBody CreateClassRequest request`      |
| **Mapping trong Service**        | Service chịu trách nhiệm Entity ↔ DTO mapping         |
| **Không lồng DTO quá sâu**       | Max 2 levels (Response → nested list item)            |

### 8.2. Pagination Pattern (reusable)

```java
// common/dto/PaginationResponse.java
public class PaginationResponse<T> {
    private List<T> items;
    private PaginationInfo pagination;

    @Data @Builder
    public static class PaginationInfo {
        private int page;
        private int limit;
        private long total;
        private int totalPages;
    }
}
```

Dùng cho: `/api/certificates/recent`, `/api/admin/users`, `/api/certificates/search`

### 8.3. Controller Return Type (nhất quán)

```java
// TẤT CẢ controllers đều dùng pattern này:
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<XxxResponse>> getById(@PathVariable String id) {
    XxxResponse data = service.getById(id);
    return ResponseEntity.ok(ApiResponse.success(data));
}

@PostMapping
public ResponseEntity<ApiResponse<XxxResponse>> create(@Valid @RequestBody CreateXxxRequest req) {
    XxxResponse data = service.create(req);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Created successfully", data));
}
```

---

## 9. API Endpoint Map

### 9.1. Endpoint → Module → Controller mapping

#### Auth (5 endpoints — DONE, cần fix bugs)

| Method | Path                           | Controller       | Service Method     |
| ------ | ------------------------------ | ---------------- | ------------------ |
| GET    | `/oauth2/authorization/google` | (Spring auto)    | —                  |
| GET    | `/oauth2/redirect?token=`      | (Spring auto)    | —                  |
| GET    | `/api/auth/me`                 | `AuthController` | `getCurrentUser()` |
| POST   | `/api/auth/logout`             | `AuthController` | (no-op)            |
| GET    | `/api/auth/check-role`         | `AuthController` | `checkRole()`      |

#### Student APIs (5 endpoints — module: course, enrollment, quiz, certificate)

| Method | Path                             | Controller                   | Module      | Service Method             |
| ------ | -------------------------------- | ---------------------------- | ----------- | -------------------------- |
| GET    | `/api/student/{id}/courses`      | `StudentDashboardController` | enrollment  | `getStudentCourses()`      |
| GET    | `/api/courses/{id}`              | `CourseController`           | course      | `getCourseDetail()`        |
| GET    | `/api/student/{id}/certificates` | `CertificateController`      | certificate | `getStudentCertificates()` |
| POST   | `/api/quizzes/{id}/submit`       | `QuizSubmitController`       | quiz        | `submitQuiz()`             |
| GET    | `/api/quizzes/{id}`              | `QuizSubmitController`       | quiz        | `getQuizForStudent()`      |

#### Teacher APIs (10 endpoints — module: classroom, quiz)

| Method | Path                            | Controller        | Module    | Service Method         |
| ------ | ------------------------------- | ----------------- | --------- | ---------------------- |
| GET    | `/api/teacher/{id}/classes`     | `ClassController` | classroom | `getTeacherClasses()`  |
| GET    | `/api/classes/{id}`             | `ClassController` | classroom | `getClassDetail()`     |
| GET    | `/api/classes/{id}/students`    | `ClassController` | classroom | `getStudentsInClass()` |
| POST   | `/api/classes`                  | `ClassController` | classroom | `createClass()`        |
| PUT    | `/api/classes/{id}`             | `ClassController` | classroom | `updateClass()`        |
| GET    | `/api/classes/{id}/quizzes`     | `QuizController`  | quiz      | `getQuizzesByClass()`  |
| POST   | `/api/quizzes`                  | `QuizController`  | quiz      | `createQuiz()`         |
| PUT    | `/api/quizzes/{id}`             | `QuizController`  | quiz      | `updateQuiz()`         |
| DELETE | `/api/quizzes/{id}`             | `QuizController`  | quiz      | `deleteQuiz()`         |
| GET    | `/api/quizzes/{id}/submissions` | `QuizController`  | quiz      | `getSubmissions()`     |

#### Admin APIs (8 endpoints — module: admin)

| Method | Path                            | Controller        | Module | Service Method            |
| ------ | ------------------------------- | ----------------- | ------ | ------------------------- |
| GET    | `/api/admin/certificates/stats` | `AdminController` | admin  | `getCertificateStats()`   |
| GET    | `/api/certificates/recent`      | `AdminController` | admin  | `getRecentCertificates()` |
| GET    | `/api/certificates/search`      | `AdminController` | admin  | `searchCertificates()`    |
| GET    | `/api/certificates/{id}`        | `AdminController` | admin  | `getCertificateDetail()`  |
| POST   | `/api/certificates/{id}/verify` | `AdminController` | admin  | `verifyCertificate()`     |
| POST   | `/api/certificates/{id}/revoke` | `AdminController` | admin  | `revokeCertificate()`     |
| GET    | `/api/admin/users`              | `AdminController` | admin  | `getUsers()`              |
| PUT    | `/api/admin/users/{id}/status`  | `AdminController` | admin  | `updateUserStatus()`      |

---

## 10. Kế hoạch refactor từng bước

### Phase 0: Foundation (không ảnh hưởng logic)

> **Mục tiêu**: Chuẩn bị khung sườn chung

| Step | Action                                                                                | Scope                     |
| ---- | ------------------------------------------------------------------------------------- | ------------------------- |
| 0.1  | Thêm Flyway dependency vào `pom.xml`                                                  | `pom.xml`                 |
| 0.2  | Tạo custom exceptions (`ResourceNotFoundException`, `BusinessException`, etc.)        | `common/exception/`       |
| 0.3  | Sửa `GlobalExceptionHandler` (thêm handlers cho custom exceptions, dùng SLF4J)        | `common/exception/`       |
| 0.4  | Tạo `PaginationResponse<T>`                                                           | `common/dto/`             |
| 0.5  | Gom enum vào `common/enums/`, xóa inner enums trong entities, xóa `constant/` package | `common/enums/`, entities |
| 0.6  | Tạo V5 migration fix ClassStatus ENUM nếu cần                                         | `db/migration/`           |

### Phase 1: Fix Auth & Security (critical)

> **Mục tiêu**: Hệ thống bảo mật hoạt động đúng

| Step | Action                                                       | Scope            |
| ---- | ------------------------------------------------------------ | ---------------- |
| 1.1  | Sửa `SecurityConfig`: restore proper URL-level authorization | `common/config/` |
| 1.2  | Sửa `CustomOAuth2UserService`: không ghi đè role             | `auth/security/` |
| 1.3  | Xóa duplicate find-or-create trong `OAuth2SuccessHandler`    | `auth/security/` |
| 1.4  | Sửa `UserPrincipal.isEnabled()`: check `isActive`            | `auth/security/` |
| 1.5  | Xóa dead DTOs (`AuthResponse`, `GoogleUserInfo`)             | `auth/dto/`      |
| 1.6  | Tạo `UserQueryService` interface (để module khác gọi)        | `auth/service/`  |
| 1.7  | Xóa `constant/RoleName.java` (dead code)                     | `constant/`      |

### Phase 2: Restructure modules (rename + move)

> **Mục tiêu**: Đúng package naming, đúng module boundary

| Step | Action                                                                |
| ---- | --------------------------------------------------------------------- |
| 2.1  | Rename `LMSCourseService/` → `course/` + `classroom/`                 |
| 2.2  | Rename `LMSQuizService/` → `quiz/`                                    |
| 2.3  | Rename `LMSLearningService/` → `enrollment/`                          |
| 2.4  | Rename `LMSCertificateBlockchainService/` → `certificate/` + `admin/` |
| 2.5  | Move shared request DTOs từ `lms/dto/request/` vào module tương ứng   |
| 2.6  | Xóa package `lms/`, `constant/` (đã move hết)                         |

### Phase 3: Rewrite services (từng module)

> **Mục tiêu**: Business logic đúng, repository queries đúng, no hardcode

| Step | Module        | Key Fixes                                                                                           |
| ---- | ------------- | --------------------------------------------------------------------------------------------------- |
| 3.1  | `classroom`   | Fix `getTeacherClasses` (filter by teacherId), fix studentCount, add filter/sort                    |
| 3.2  | `quiz`        | Fix `getByClass` (filter by classId), fix repository generic type, fix updateQuiz, fix passingScore |
| 3.3  | `enrollment`  | Fix N+1 query, remove dead code, set courseIcon                                                     |
| 3.4  | `certificate` | Fix missing fields, fix courseCode mapping, real averageScore, typed responses                      |
| 3.5  | `course`      | Fix courseCode source, proper quiz status (completed/pending/locked)                                |
| 3.6  | `admin`       | Fix averageScore, fix role query, fix datetime format, typed revoke response                        |

### Phase 4: Polish

> **Mục tiêu**: Production-ready

| Step | Action                                                             |
| ---- | ------------------------------------------------------------------ |
| 4.1  | Thêm `@Valid` + validation annotations trên tất cả request DTOs    |
| 4.2  | Bật `@PreAuthorize` trên tất cả controllers                        |
| 4.3  | Thêm service interfaces cho mọi service (để test + decouple)       |
| 4.4  | Nhất quán return type: `ResponseEntity<ApiResponse<T>>` everywhere |
| 4.5  | Xóa toàn bộ dead code đã identify                                  |
| 4.6  | Externalize secrets (DB password, JWT secret)                      |

---

## Appendix: Tech Stack Summary

| Layer     | Technology                     | Version                |
| --------- | ------------------------------ | ---------------------- |
| Language  | Java                           | 21                     |
| Framework | Spring Boot                    | 3.2.3                  |
| Security  | Spring Security + OAuth2 + JWT | managed                |
| ORM       | Spring Data JPA / Hibernate    | managed                |
| Database  | MySQL                          | 8.x                    |
| Migration | Flyway                         | (cần thêm vào pom.xml) |
| Build     | Maven                          | 3.x                    |
| JWT       | jjwt                           | 0.12.3                 |
| Utility   | Lombok                         | managed                |
