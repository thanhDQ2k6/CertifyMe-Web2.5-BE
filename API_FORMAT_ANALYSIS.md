# 🔍 KIỂM TRA API FORMAT - BACKEND vs FRONTEND

> **Ngày kiểm tra**: 2026-02-20  
> **Mục đích**: So sánh response format BE hiện tại với yêu cầu của FE team

---

## ❌ TỔNG QUAN

| Mục                | Trạng thái        | Mức độ ưu tiên    |
| ------------------ | ----------------- | ----------------- |
| Authentication API | ⚠️ **KHÔNG KHỚP** | 🔴 **CAO**        |
| Student API        | ❌ **CHƯA CÓ**    | 🔴 **CAO**        |
| Teacher API        | ❌ **CHƯA CÓ**    | 🟡 **TRUNG BÌNH** |
| Admin API          | ❌ **CHƯA CÓ**    | 🟡 **TRUNG BÌNH** |
| Response Wrapper   | ⚠️ **KHÔNG KHỚP** | 🟢 **THẤP**       |

---

## 1. 🔐 AUTHENTICATION API

### 📌 FE yêu cầu (từ BE-Integration-Guide.md):

#### Endpoint: `POST /api/auth/google`

**Request:**

```json
{
  "credential": "eyJhbGciOiJSUzI1NiIs..."
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "u1",
    "name": "Nguyễn Văn A",
    "email": "annv@fpt.edu.vn",
    "role": "student",
    "avatar": "https://..."
  }
}
```

---

### 📌 BE hiện tại:

#### ❌ **THIẾU** Endpoint `POST /api/auth/google`

Hiện chỉ có:

- ✅ `GET /api/auth/me` - Lấy user hiện tại
- ✅ `POST /api/auth/logout` - Đăng xuất
- ✅ `GET /api/auth/check-role` - Kiểm tra role

**Backend đang dùng Google OAuth2 flow qua Spring Security:**

```
http://localhost:8080/oauth2/authorization/google
→ Google Login
→ Callback: /login/oauth2/code/google
→ Redirect: /oauth2/redirect?token=...
```

#### ⚠️ **AuthResponse.java - KHÔNG KHỚP**

**BE hiện tại:**

```java
{
  "accessToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "user": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "annv@fpt.edu.vn",
    "fullName": "Nguyễn Văn A",
    "avatarUrl": "https://...",
    "role": "STUDENT",
    "isActive": true
  }
}
```

**FE yêu cầu:**

```json
{
  "token": "eyJhbGc...",
  "user": {
    "id": "u1",
    "name": "Nguyễn Văn A",
    "email": "annv@fpt.edu.vn",
    "role": "student",
    "avatar": "https://..."
  }
}
```

**❌ Các điểm KHÔNG KHỚP:**

| Field BE                  | Field FE yêu cầu          | Trạng thái                |
| ------------------------- | ------------------------- | ------------------------- |
| `accessToken`             | `token`                   | ❌ Tên field khác         |
| `tokenType`               | -                         | ⚠️ FE không cần           |
| `user.userId`             | `user.id`                 | ❌ Tên field khác         |
| `user.fullName`           | `user.name`               | ❌ Tên field khác         |
| `user.avatarUrl`          | `user.avatar`             | ❌ Tên field khác         |
| `user.role` = `"STUDENT"` | `user.role` = `"student"` | ❌ Uppercase vs lowercase |
| `user.isActive`           | -                         | ⚠️ FE không cần           |

---

### 🔧 CÁCH SỬA:

#### Option 1: Tạo endpoint mới `/api/auth/google` (KHUYÊN DÙNG)

```java
@PostMapping("/google")
public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
    // Verify Google credential
    // Create/Update user
    // Generate JWT
    // Return theo format FE yêu cầu
}
```

**GoogleLoginRequest.java:**

```java
public class GoogleLoginRequest {
    private String credential;
}
```

**AuthResponse.java (SỬA):**

```java
@Data
@Builder
public class AuthResponse {
    private String token;  // Đổi từ accessToken
    private UserResponse user;
    // Bỏ tokenType
}
```

**UserResponse.java (SỬA):**

```java
@Data
@Builder
public class UserResponse {
    private String id;          // Đổi từ userId
    private String name;        // Đổi từ fullName
    private String email;
    private String role;        // Lowercase: "student", "teacher", "admin"
    private String avatar;      // Đổi từ avatarUrl
    // Bỏ isActive
}
```

#### Option 2: Giữ OAuth2 flow hiện tại + Thêm endpoint FE-friendly

Giữ OAuth2 flow cho browser, thêm endpoint `/api/auth/google` để FE có thể gọi trực tiếp.

---

## 2. 📚 STUDENT API

### ❌ **HOÀN TOÀN CHƯA CÓ**

FE yêu cầu các endpoints:

| Method | Endpoint                               | Mô tả                    | Ưu tiên |
| ------ | -------------------------------------- | ------------------------ | ------- |
| GET    | `/api/student/:studentId/courses`      | DS khóa học của SV       | 🔴 CAO  |
| GET    | `/api/courses/:courseId`               | Chi tiết khóa học + quiz | 🔴 CAO  |
| GET    | `/api/student/:studentId/certificates` | DS chứng chỉ             | 🟡 TB   |
| POST   | `/api/quizzes/:quizId/submit`          | Nộp bài quiz             | 🟡 TB   |

### 📌 FE yêu cầu - `GET /student/:id/courses`:

```json
[
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
]
```

### 🔧 CẦN TẠO:

1. **StudentController.java**
2. **StudentService.java**
3. **DTOs:**
   - `StudentCourseResponse.java`
   - `CourseDetailResponse.java`
   - `CertificateResponse.java`
   - `QuizSubmitRequest.java`
   - `QuizResultResponse.java`

---

## 3. 👨‍🏫 TEACHER API

### ❌ **HOÀN TOÀN CHƯA CÓ**

FE yêu cầu các endpoints:

| Method | Endpoint                      | Ưu tiên |
| ------ | ----------------------------- | ------- |
| GET    | `/teacher/:teacherId/classes` | 🔴 CAO  |
| GET    | `/classes/:classId`           | 🔴 CAO  |
| GET    | `/classes/:classId/students`  | 🟡 TB   |
| POST   | `/classes`                    | 🟡 TB   |
| PUT    | `/classes/:classId`           | 🟢 THẤP |
| GET    | `/courses/:courseId/quizzes`  | 🟡 TB   |
| POST   | `/quizzes`                    | 🟡 TB   |
| PUT    | `/quizzes/:quizId`            | 🟢 THẤP |
| DELETE | `/quizzes/:quizId`            | 🟢 THẤP |

### 🔧 CẦN TẠO:

1. **TeacherController.java**
2. **ClassController.java**
3. **QuizController.java**
4. **Services + DTOs tương ứng**

---

## 4. 👑 ADMIN API

### ❌ **HOÀN TOÀN CHƯA CÓ**

FE yêu cầu các endpoints:

| Method | Endpoint                       | Ưu tiên |
| ------ | ------------------------------ | ------- |
| GET    | `/admin/certificates/stats`    | 🔴 CAO  |
| GET    | `/certificates/recent?limit=N` | 🟡 TB   |
| GET    | `/certificates/search?q=...`   | 🟡 TB   |
| GET    | `/certificates/:certId`        | 🟡 TB   |
| POST   | `/certificates/:certId/verify` | 🟢 THẤP |
| POST   | `/certificates/:certId/revoke` | 🟢 THẤP |

### 🔧 CẦN TẠO:

1. **AdminController.java**
2. **CertificateController.java**
3. **Services + DTOs tương ứng**

---

## 5. 📦 RESPONSE WRAPPER FORMAT

### 📌 FE yêu cầu:

**Thành công:**

```json
{
  "data": { ... },
  "message": "Success"
}
```

Hoặc trả trực tiếp data (ưu tiên).

**Lỗi:**

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Token expired"
  }
}
```

---

### 📌 BE hiện tại:

**ApiResponse.java:**

```java
{
  "success": true,
  "message": "...",
  "data": { ... },
  "error": null
}
```

### ⚠️ Không khớp hoàn toàn nhưng chấp nhận được

**Lý do:**

- FE có thể access data qua `response.data.data` (axios tự unwrap)
- Hoặc BE có thể trả trực tiếp object không cần wrapper

### 🔧 KHUYẾN NGHỊ:

**Option 1:** Trả trực tiếp data (đơn giản nhất)

```java
@GetMapping("/courses")
public ResponseEntity<List<CourseResponse>> getCourses() {
    return ResponseEntity.ok(courses);  // FE nhận: response.data
}
```

**Option 2:** Giữ ApiResponse nhưng FE unwrap

```javascript
// FE vẫn dùng được
const courses = response.data.data;
```

---

## 📋 CHECKLIST TÍCH HỢP

### 🔴 Ưu tiên CAO (Cần làm ngay):

- [ ] **Tạo endpoint `POST /api/auth/google`**
  - [ ] Nhận Google credential
  - [ ] Verify với Google
  - [ ] Tạo/cập nhật user trong DB
  - [ ] Generate JWT token
  - [ ] Trả về format: `{ token, user: { id, name, email, role, avatar } }`

- [ ] **Sửa AuthResponse.java & UserResponse.java**
  - [ ] `accessToken` → `token`
  - [ ] `userId` → `id`
  - [ ] `fullName` → `name`
  - [ ] `avatarUrl` → `avatar`
  - [ ] `role`: `"STUDENT"` → `"student"` (lowercase)
  - [ ] Bỏ `tokenType`, `isActive`

- [ ] **Tạo Student APIs**
  - [ ] `GET /api/student/:id/courses` - Danh sách khóa học
  - [ ] `GET /api/courses/:id` - Chi tiết khóa học
  - [ ] StudentController.java
  - [ ] StudentService.java
  - [ ] DTOs: StudentCourseResponse, CourseDetailResponse

### 🟡 Ưu tiên TRUNG BÌNH (Làm sau):

- [ ] **Tạo Teacher APIs**
  - [ ] `GET /api/teacher/:id/classes`
  - [ ] `GET /api/classes/:id`
  - [ ] `GET /api/classes/:id/students`
  - [ ] `POST /api/quizzes`
  - [ ] TeacherController, ClassController, QuizController

- [ ] **Tạo Admin APIs**
  - [ ] `GET /api/admin/certificates/stats`
  - [ ] `GET /api/certificates/recent`
  - [ ] `GET /api/certificates/:id`
  - [ ] AdminController, CertificateController

- [ ] **Tạo các DTOs theo spec FE**

### 🟢 Ưu tiên THẤP (Optional):

- [ ] Cập nhật Response wrapper (nếu cần)
- [ ] Thêm pagination cho list endpoints
- [ ] Thêm error codes chuẩn
- [ ] API documentation (Swagger)

---

## 🚨 VẤN ĐỀ QUAN TRỌNG NHẤT

### ❌ **AUTHENTICATION FLOW KHÔNG TƯƠNG THÍCH**

**FE mong đợi:**

```javascript
// FE gọi
const response = await AuthService.signInWithGoogle(credential);
// POST /api/auth/google
// Body: { credential: "eyJ..." }

// Nhận về
response = {
  token: "...",
  user: { id, name, email, role, avatar },
};
```

**BE hiện tại:**

```
User visit: http://localhost:8080/oauth2/authorization/google
→ Google login
→ Redirect: /login/oauth2/code/google (BE internal)
→ Redirect: /oauth2/redirect?token=...
```

**⚠️ BE đang dùng OAuth2 flow kiểu redirect, không phải REST API!**

### 🔧 GIẢI PHÁP:

**Cần implement endpoint mới:**

```java
@PostMapping("/api/auth/google")
public ResponseEntity<AuthResponse> authenticateWithGoogle(
    @RequestBody GoogleLoginRequest request
) {
    // 1. Verify Google credential với Google API
    GoogleIdToken.Payload payload = verifyGoogleToken(request.getCredential());

    // 2. Extract user info
    String email = payload.getEmail();
    String name = (String) payload.get("name");
    String picture = (String) payload.get("picture");
    String googleId = payload.getSubject();

    // 3. Create/Update user trong DB
    User user = userService.findOrCreateUserFromGoogle(email, name, picture, googleId);

    // 4. Generate JWT
    String token = jwtTokenProvider.generateToken(user);

    // 5. Map to FE format
    UserResponse userResponse = UserResponse.builder()
        .id(user.getUserId())
        .name(user.getFullName())
        .email(user.getEmail())
        .role(user.getRole().getRoleName().name().toLowerCase())
        .avatar(user.getAvatarUrl())
        .build();

    AuthResponse response = AuthResponse.builder()
        .token(token)
        .user(userResponse)
        .build();

    return ResponseEntity.ok(response);
}
```

---

## 📝 KẾT LUẬN

### Tình trạng hiện tại:

1. ✅ **JWT Token generation** - Đã có, hoạt động tốt
2. ✅ **User Entity & Repository** - Đã có
3. ⚠️ **Auth endpoint** - Thiếu `/api/auth/google`, format response không khớp
4. ❌ **Student/Teacher/Admin APIs** - Hoàn toàn chưa có
5. ⚠️ **Response format** - Có ApiResponse wrapper nhưng FE mong đợi direct data

### Khối lượng công việc:

| Task                              | Estimate | Priority        |
| --------------------------------- | -------- | --------------- |
| Fix Auth endpoint + DTOs          | 2-3 giờ  | 🔴 Ngay lập tức |
| Student APIs (2 endpoints cơ bản) | 4-6 giờ  | 🔴 Ngay lập tức |
| Teacher APIs (4-5 endpoints)      | 8-10 giờ | 🟡 Tuần sau     |
| Admin APIs (3-4 endpoints)        | 6-8 giờ  | 🟡 Tuần sau     |
| Testing + Polish                  | 4-5 giờ  | 🟢 Sau khi xong |

**Tổng:** ~24-32 giờ (3-4 ngày làm việc)

---

## 🎯 NEXT STEPS

1. **NGAY BÂY GIỜ:** Sửa Auth response format
2. **HÔM NAY:** Implement `POST /api/auth/google` endpoint
3. **NGÀY MAI:** Tạo Student APIs cơ bản
4. **TUẦN SAU:** Teacher & Admin APIs
5. **CUỐI TUẦN:** Testing tích hợp với FE

---

_Báo cáo: 2026-02-20_
