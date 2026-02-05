# 📋 KẾ HOẠCH PHÁT TRIỂN DỰ ÁN LMS BACKEND

> **Nguyên tắc**: Trừu tượng đơn giản → Kỹ thuật phức tạp (INFJ Approach)

---

## 🎯 PHẦN 1: TẦM NHÌN TỔNG QUAN

### 1.1. Mục đích hệ thống

Xây dựng hệ thống quản lý học tập (LMS) với khả năng:

- Quản lý người dùng & phân quyền
- Quản lý lớp học & ghi danh
- Kiểm tra đánh giá qua quiz
- Cấp chứng chỉ & xác thực blockchain

### 1.2. Các nhóm chức năng chính

```
┌─────────────────────────────────────────────────────────────┐
│                      LMS BACKEND                            │
├─────────────┬─────────────┬─────────────┬─────────────┬─────┤
│    AUTH     │    CLASS    │    QUIZ     │    CERT     │ SYS │
│  (Xác thực) │ (Lớp học)   │ (Kiểm tra)  │ (Chứng chỉ) │     │
├─────────────┼─────────────┼─────────────┼─────────────┼─────┤
│ • users     │ • classes   │ • quizzes   │ • certs     │ org │
│ • roles     │ • enroll    │ • questions │             │info │
│ • oauth2    │             │ • attempts  │             │     │
│             │             │ • answers   │             │     │
└─────────────┴─────────────┴─────────────┴─────────────┴─────┘
```

---

## 🏗️ PHẦN 2: CẤU TRÚC MICROSERVICE

### 2.1. Tổ chức thư mục theo Module

```
src/main/java/main/backend/
│
├── common/                          # 🔧 Module dùng chung
│   ├── config/                      # Cấu hình chung (Security, CORS, etc.)
│   ├── exception/                   # Xử lý ngoại lệ toàn cục
│   ├── dto/                         # DTO dùng chung (ApiResponse, PageResponse)
│   └── util/                        # Tiện ích (ID Generator, Date Utils)
│
├── auth/                            # 🔐 Module Xác thực
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/                      # User, Role
│   ├── dto/
│   ├── enums/                       # RoleType enum
│   └── security/                    # OAuth2, JWT handlers
│
├── classroom/                       # 📚 Module Lớp học
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/                      # Class, Enrollment
│   ├── dto/
│   └── enums/                       # ClassStatus, EnrollmentStatus
│
├── quiz/                            # 📝 Module Kiểm tra
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/                      # Quiz, Question, Attempt, Answer
│   ├── dto/
│   └── enums/                       # QuizStatus, AnswerOption
│
├── certificate/                     # 🎓 Module Chứng chỉ
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/                      # Certificate
│   ├── dto/
│   └── enums/                       # CertificateStatus
│
└── organization/                    # ⚙️ Module Cấu hình
    ├── controller/
    ├── service/
    ├── repository/
    ├── entity/                      # OrganizationInfo
    └── dto/
```

### 2.2. Nguyên tắc tổ chức

| Tầng       | Trách nhiệm                          | Quy ước đặt tên                       |
| ---------- | ------------------------------------ | ------------------------------------- |
| Controller | Nhận request, validate, trả response | `*Controller.java`                    |
| Service    | Business logic                       | `*Service.java` + `*ServiceImpl.java` |
| Repository | Truy vấn database                    | `*Repository.java`                    |
| Entity     | Mapping với DB table                 | Tên bảng (số ít)                      |
| DTO        | Transfer data                        | `*Request.java`, `*Response.java`     |
| Enum       | Hằng số                              | `*Status.java`, `*Type.java`          |

---

## 🔐 PHẦN 3: KẾ HOẠCH CHỨC NĂNG ĐĂNG NHẬP GOOGLE OAUTH2

### 3.1. Ý tưởng cốt lõi (Concept)

```
┌─────────────────────────────────────────────────────────────┐
│                    LUỒNG ĐĂNG NHẬP                          │
│                                                             │
│  [User] ──→ [Chọn Role] ──→ [Login Google] ──→ [Backend]    │
│                                                    │        │
│                              ┌─────────────────────┘        │
│                              ▼                              │
│                    [Tạo/Cập nhật User với Role]             │
│                              │                              │
│                              ▼                              │
│                    [Trả về JWT Token]                       │
└─────────────────────────────────────────────────────────────┘
```

**Điểm đặc biệt**: User chọn role TRƯỚC khi đăng nhập Google

- Đăng nhập với vai trò: `STUDENT`, `TEACHER`, `ADMIN`
- Role được truyền qua OAuth2 state parameter

### 3.2. Các bước triển khai (Từ đơn giản → Phức tạp)

---

#### 📌 BƯỚC 1: Chuẩn bị Enum & Entity cơ bản

**Mục tiêu**: Định nghĩa các kiểu dữ liệu nền tảng

```
auth/
├── enums/
│   └── RoleType.java              # enum: STUDENT, TEACHER, ADMIN
├── entity/
│   ├── Role.java                  # Mapping bảng roles
│   └── User.java                  # Mapping bảng users
└── repository/
    ├── RoleRepository.java
    └── UserRepository.java
```

**Logic**:

- `RoleType` enum phải khớp với dữ liệu trong bảng `roles`
- Sử dụng `@Enumerated(EnumType.STRING)` để lưu tên role

---

#### 📌 BƯỚC 2: Cấu hình Google OAuth2

**Mục tiêu**: Tích hợp đăng nhập Google

**Cấu hình cần thêm** (application.properties):

```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_SECRET
spring.security.oauth2.client.registration.google.scope=email,profile
```

**Files cần tạo**:

```
auth/
└── security/
    ├── OAuth2Config.java             # Cấu hình OAuth2
    └── CustomOAuth2UserService.java  # Xử lý user sau khi Google trả về
```

---

#### 📌 BƯỚC 3: Xử lý Role trong OAuth2 Flow

**Mục tiêu**: Truyền và nhận role thông qua OAuth2

**Luồng chi tiết**:

```
1. Frontend gọi: /api/auth/login?role=TEACHER
                                     │
2. Backend tạo OAuth2 URL với state: │
   state = { "role": "TEACHER" }     │
                                     ▼
3. Google xác thực ──→ Redirect về callback
                                  │
4. Backend đọc state, lấy role    │
                                  ▼
5. Tạo/Update user với role đã chọn
```

**Files cần tạo**:

```
auth/
├── controller/
│   └── AuthController.java        # Endpoints: /login, /callback
├── service/
│   ├── AuthService.java
│   └── impl/
│       └── AuthServiceImpl.java
├── dto/
│   ├── LoginRequest.java          # { role: "TEACHER" }
│   ├── LoginResponse.java         # { accessToken, user }
│   └── GoogleUserInfo.java        # Thông tin từ Google
└── security/
    ├── JwtTokenProvider.java      # Tạo JWT token
    ├── JwtAuthenticationFilter.java
    └── OAuth2AuthenticationSuccessHandler.java
```

---

#### 📌 BƯỚC 4: Bảo mật & JWT

**Mục tiêu**: Tạo token và bảo vệ API

**Luồng JWT**:

```
[Login thành công] ──→ [Tạo JWT với role] ──→ [Trả về Frontend]
                                                     │
[Request tiếp theo] ←── [Kiểm tra JWT] ←── [Gửi kèm token]
```

**Files cần tạo**:

```
common/
└── config/
    └── SecurityConfig.java        # Cấu hình Spring Security

auth/
└── security/
    ├── JwtTokenProvider.java         # Tạo & validate JWT
    ├── JwtAuthenticationFilter.java  # Filter kiểm tra token
    └── UserPrincipal.java            # Custom UserDetails
```

---

### 3.3. API Endpoints

| Method | Endpoint             | Mô tả                       | Request              |
| ------ | -------------------- | --------------------------- | -------------------- |
| GET    | `/api/auth/login`    | Khởi tạo OAuth2 login       | `?role=TEACHER`      |
| GET    | `/api/auth/callback` | Google callback             | (OAuth2 tự động)     |
| GET    | `/api/auth/me`       | Lấy thông tin user hiện tại | Header: Bearer token |
| POST   | `/api/auth/logout`   | Đăng xuất                   | Header: Bearer token |

---

### 3.4. Database seed (Migration V2)

```sql
-- V2__Seed_roles.sql
INSERT INTO roles (role_name) VALUES ('STUDENT');
INSERT INTO roles (role_name) VALUES ('TEACHER');
INSERT INTO roles (role_name) VALUES ('ADMIN');
```

---

## 📅 PHẦN 4: THỨ TỰ TRIỂN KHAI

### Phase 1: Nền tảng (Foundation)

- [ ] Tạo cấu trúc thư mục theo module
- [ ] Tạo common module (config, exception, dto, util)
- [ ] Tạo RoleType enum & Entity (Role, User)
- [ ] Seed data cho bảng roles

### Phase 2: Authentication Core

- [ ] Cấu hình Google OAuth2 credentials
- [ ] Tạo SecurityConfig cơ bản
- [ ] Implement CustomOAuth2UserService
- [ ] Xử lý role trong OAuth2 state

### Phase 3: JWT & Authorization

- [ ] Implement JwtTokenProvider
- [ ] Tạo JwtAuthenticationFilter
- [ ] Implement AuthController endpoints
- [ ] Test luồng đăng nhập hoàn chỉnh

### Phase 4: Enhancement

- [ ] Xử lý exception toàn cục
- [ ] Thêm logging
- [ ] Viết unit tests
- [ ] API documentation (Swagger)

---

## 🔧 PHẦN 5: DEPENDENCIES CẦN THÊM

```xml
<!-- pom.xml -->
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Client (Google Login) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- OAuth2 Resource Server (JWT Support built-in) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Ưu điểm khi dùng Spring Security thay JJWT:

| Spring Security                 | JJWT                   |
| ------------------------------- | ---------------------- |
| ✅ Tích hợp sẵn với Spring Boot | ❌ Cần config thủ công |
| ✅ Auto-configure JWT decoder   | ❌ Tự viết JWT parser  |
| ✅ Built-in token validation    | ❌ Tự handle exception |
| ✅ Hỗ trợ RSA/EC keys dễ dàng   | ❌ Phức tạp hơn        |
| ✅ Test support tốt hơn         | ❌ Mock khó hơn        |

---

## 📝 GHI CHÚ

- **INFJ Approach**: Kế hoạch đi từ tầm nhìn tổng thể (Ni) → Cấu trúc logic (Ti) → Chi tiết kỹ thuật (Se)
- Mỗi module độc lập, có thể tách thành microservice riêng sau này
- Role được chọn trước khi đăng nhập để đảm bảo user có vai trò rõ ràng ngay từ đầu

---

_Cập nhật: 2026-02-03_
