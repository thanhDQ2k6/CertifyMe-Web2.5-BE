# FE API Integration Guide (Vue.js - Port 5173)

> Mục tiêu: tài liệu tích hợp cụ thể BE -> FE cho Vue, bao gồm endpoint, data wrapper, field request/response, auth flow, và xử lý lỗi chuẩn.
> Scope: bám endpoint hiện có trong backend, không mở rộng nghiệp vụ ngoài API hiện tại.

## Nguồn chuẩn dùng để tích hợp

- **Source of truth:** `docs/API-DOCUMENTATION.md`
- `docs/Archive/*` là tài liệu cũ, chỉ tham khảo lịch sử, **không dùng làm chuẩn tích hợp hiện tại**.

---

## 1) Environment & base URL

## 1.1 FE (Vue) chạy ở port 5173

- FE URL: `http://localhost:5173`
- BE URL: `http://localhost:8080`

## 1.2 BE cần cấu hình đúng cho FE 5173

Do backend đang dùng biến `FRONTEND_URL` cho cả CORS và OAuth redirect:

- `FRONTEND_URL=http://localhost:5173`

Liên quan trong BE:

- CORS allow origin: `@Value("${FRONTEND_URL:...}")` trong `SecurityConfig`
- OAuth redirect-uri: `app.oauth2.redirect-uri=${FRONTEND_URL...}/oauth2/redirect`

Nếu không set `FRONTEND_URL=5173`, callback có thể bị redirect sai cổng hoặc lỗi CORS.

---

## 2) Contract chuẩn BE -> FE

Tất cả business API trả theo wrapper chung:

```json
{
  "success": true,
  "message": "Optional",
  "data": {},
  "error": null
}
```

## 2.1 Rule đọc dữ liệu

- Axios response: `response.data`
- Business payload: `response.data.data`

```ts
// Sai
const x = response.data;

// Đúng
const x = response.data.data;
```

## 2.2 Type wrapper chuẩn FE

```ts
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  error?: string;
}
```

---

## 3) HTTP client chuẩn (Vue)

```ts
import axios, { AxiosResponse } from "axios";

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

apiClient.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  },
);

export async function api<T>(
  req: Promise<AxiosResponse<ApiResponse<T>>>,
): Promise<T> {
  const response = await req;
  if (!response.data.success) {
    throw new Error(
      response.data.error || response.data.message || "API Error",
    );
  }
  return response.data.data;
}
```

---

## 4) OAuth login dialogue (BE <-> FE)

## 4.1 Luồng chuẩn

1. FE điều hướng user tới OAuth endpoint BE (`/oauth2/authorization/google`).
2. Google auth thành công -> BE callback `/login/oauth2/code/google`.
3. BE tạo JWT và redirect về FE callback với query params.
4. FE callback đọc params, lưu token, gọi `/api/auth/me` để lấy user profile thật.
5. FE route theo role hoặc theo `redirect` BE trả về.

## 4.2 Query params BE trả về callback

- `token`: JWT
- `role`: `STUDENT | TEACHER | ADMIN`
- `redirect`: đường dẫn gợi ý (`/student/dashboard`, `/teacher/dashboard`, `/admin/dashboard`)
- Trường hợp lỗi: `error=authentication_failed`

Lưu ý: route FE nội bộ có thể khác `redirect` gợi ý (ví dụ `/teacher/classes`). FE nên ưu tiên kiểm tra `me.role` từ `/api/auth/me` rồi map về route nội bộ của FE.

## 4.3 FE callback handling (bắt buộc)

- Nếu có `error` -> báo lỗi + về login.
- Nếu thiếu `token` -> coi là lỗi xác thực.
- Nếu có `token` -> lưu localStorage -> gọi `/api/auth/me`.
- Ưu tiên route theo `me.role` (nguồn thật từ API), dùng `redirect` làm fallback.

---

## 5) Auth endpoints

## 5.1 GET `/api/auth/me`

- Auth: Bearer token
- `data` fields:
  - `userId: string`
  - `email: string`
  - `fullName: string`
  - `avatarUrl: string`
  - `role: "STUDENT" | "TEACHER" | "ADMIN"`
  - `isActive: boolean`

## 5.2 POST `/api/auth/logout`

- Auth: Bearer token
- `data`: `null`
- FE vẫn phải tự clear token/session (JWT stateless).

## 5.3 GET `/api/auth/check-role`

- Auth: Bearer token
- `message`: role hiện tại
- `data`: `null`

---

## 6) Student integration

Endpoint bổ sung (public):

- `GET /api/courses` -> danh sách khóa học public (`courseId, courseCode, courseName, description`).

## 6.1 GET `/api/student/{studentId}/courses`

- Role: STUDENT
- Path param: `studentId`
- `data`: `CourseResponse[]`
- Field chính mỗi item:
  - `courseId, courseCode, courseName, courseIcon`
  - `teacherName`
  - `progress, totalQuizzes, completedQuizzes, averageScore`
  - `isCompleted`

## 6.2 GET `/api/courses/{courseId}`

- Role: STUDENT
- Path param: `courseId`
- `data`: course detail gồm tiến độ, quizzes, certificate summary (nếu có)
- FE dùng cho màn Course Detail.

## 6.3 GET `/api/quizzes/{quizId}`

- Role: STUDENT hoặc TEACHER
- Path param: `quizId`
- `data`:
  - `quizId, quizName, duration, passingScore, status`
  - `questions[]` với field: `questionId, questionText, optionA, optionB, optionC, optionD`

Field behavior từ source code:

- `status` quiz đang trả chữ thường (`draft | published | closed` hoặc `active` theo dataset).

## 6.4 POST `/api/quizzes/{quizId}/submit`

- Role: STUDENT
- Body:

```json
{
  "studentId": "string",
  "answers": [{ "questionId": "1", "selectedOption": "A" }]
}
```

- `data`:
  - `score, maxScore`
  - `status: PASSED | FAILED`
  - `submittedAt`

## 6.5 GET `/api/quizzes/{quizId}/result`

- Role: STUDENT
- `data`:
  - `quizId, quizTitle`
  - `score, maxScore, passingScore`
  - `status, attemptCount, submittedAt`

## 6.6 GET `/api/student/{studentId}/certificates`

- Role: STUDENT
- `data`: `CertificateResponse[]`
- Field chính mỗi item:
  - `certificateId, courseName, courseCode`
  - `issuedAt, verificationHash, status`
  - `blockchainInfo?` (`hash, block, txHash, contract`)

Field behavior từ source code:

- `status` đang trả chữ thường (`pending | issued | revoked`).
- `issuedAt` hiện đang format `yyyy-MM-dd`.
- `averageScore` có trong DTO nhưng service hiện chưa map giá trị (có thể `null`).

---

## 7) Teacher integration

## 7.1 GET `/api/teacher/{teacherId}/classes`

- Role: TEACHER
- `data`: class list
- Field chính:
  - `classId, classCode, courseName, courseId`
  - `teacherName, studentCount, quizCount`
  - `status, startDate, endDate, description`

## 7.2 GET `/api/classes/{classId}`

- Role: TEACHER
- `data`: class detail (dùng cho context màn class)

## 7.3 GET `/api/classes/{classId}/students`

- Role: TEACHER
- Query optional: `status`, `sort`, `order`
- `data`: student list
- Field chính:
  - `studentId, fullName, email, avatarUrl`
  - `completedQuizzes, totalQuizzes, averageScore`
  - `status, enrolledAt`

## 7.4 POST `/api/classes`

- Role: TEACHER
- Body:

```json
{
  "classCode": "SD18303",
  "courseId": "CRS-001",
  "teacherId": "<teacherId>",
  "startDate": "2026-04-01",
  "endDate": "2026-09-01",
  "description": "Lớp Java buổi sáng"
}
```

## 7.5 PUT `/api/classes/{id}`

- Role: TEACHER
- Body tương tự create class.

## 7.6 GET `/api/classes/{classId}/quizzes`

- Role: STUDENT hoặc TEACHER
- `data`: quiz list của class.

## 7.7 POST `/api/quizzes`

- Role: TEACHER
- Body (rút gọn):

```json
{
  "classId": "CLS-005",
  "quizName": "Quiz 2: Functions",
  "duration": 45,
  "passingScore": 5.0,
  "questions": [
    {
      "questionText": "...",
      "questionType": "multiple_choice",
      "options": [{ "optionId": "A", "optionText": "...", "isCorrect": true }]
    }
  ]
}
```

## 7.8 PUT `/api/quizzes/{quizId}` / DELETE `/api/quizzes/{quizId}`

- Role: TEACHER
- Update body giống create; delete không có body.

## 7.9 GET `/api/quizzes/{quizId}/submissions`

- Role: TEACHER
- `data`: submission list
- Field chính:
  - `submissionId, studentId, studentName, studentEmail`
  - `score, maxScore, passed, submittedAt`

## 7.10 Enrollment actions

- `POST /api/enrollments`

```json
{
  "studentId": "3d505413-8993-45b2-8d41-3018ba80e0b1",
  "classId": "CLS-005"
}
```

- `DELETE /api/enrollments/{enrollmentId}`

---

## 8) Admin integration

## 8.1 GET `/api/admin/certificates/stats`

- Role: ADMIN
- `data`:
  - `totalCertificates, issuedCertificates, revokedCertificates`
  - `certificatesThisMonth, certificatesThisYear`

## 8.2 GET `/api/certificates/recent?limit=&page=`

- Role: ADMIN
- `data`:
  - `items[]` + `pagination`

Field behavior từ source code:

- `items[].status` hiện đang trả chữ thường (`issued | revoked | pending`).
- `items[].issuedAt` đang ở ISO instant (ví dụ `2026-03-15T00:00:00Z`).

## 8.3 GET `/api/certificates/search?q=&status=&limit=`

- Role: ADMIN
- `data`: cùng cấu trúc list/pagination.

## 8.4 GET `/api/certificates/{certificateId}`

- Role: ADMIN
- `data`:
  - cert info: `certificateId, studentId, studentName, classId, className, courseCode, courseName`
  - status info: `averageScore, issuedAt, status, verificationHash`
  - `blockchainInfo?`: `transactionHash, blockNumber, contractAddress, networkName, explorerUrl`
  - `quizResults[]`: `quizId, quizName, score, maxScore, completedAt`

Field behavior từ source code:

- `status` hiện đang trả chữ thường.
- `networkName` hiện hard-code `Ethereum Mainnet`.

## 8.5 POST `/api/certificates/{certificateId}/revoke`

- Role: ADMIN
- Body:

```json
{
  "reason": "Academic integrity violation",
  "revokedBy": "admin-user-id"
}
```

- `data`:
  - `certificateId, status, revokedAt, revokedByUserId, reason`

## 8.6 POST `/api/certificates/{certificateId}/verify`

- Role: ADMIN
- `data`:
  - `certificateId, isValid, verificationHash, verifiedAt`
  - `blockchainInfo`: `transactionHash, blockNumber, contractAddress, timestamp, status`

Field behavior từ source code:

- `blockchainInfo.status` hiện trả `confirmed` (chữ thường).

## 8.7 User management

- `GET /api/admin/users?role=&status=&page=&limit=`
- `PUT /api/admin/users/{userId}/status`

```json
{ "isActive": false, "reason": "Account suspended" }
```

- `PUT /api/admin/users/{userId}/role`

```json
{ "role": "TEACHER" }
```

---

## 9) Error dialogue chuẩn từ BE -> FE

## 9.1 Error wrapper

```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": "Error message"
}
```

## 9.2 FE handling rule

- `401`: clear token + redirect login.
- `403`: show message "không có quyền".
- `404`: show empty/not found.
- `400`: show validation error từ `error`.
- `500`: show generic error + retry option.

---

## 10) Runtime checklist (Vue 5173)

1. Set FE `.env`:
   - `VITE_API_BASE_URL=http://localhost:8080`
2. Set BE env:
   - `FRONTEND_URL=http://localhost:5173`
3. Start BE, xác nhận CORS nhận origin `5173`.
4. Test OAuth callback nhận đủ `token`, `role`, `redirect`.
5. Test `GET /api/auth/me` thành công sau callback.
6. Test role routing cho 3 role.
7. Smoke test Student/Teacher/Admin endpoints chính.

---

## 11) Ghi chú về blockchain field

- Hiện tại cert đang DB-first, blockchain info có thể có hoặc null tùy record.
- FE phải render an toàn khi thiếu `transactionHash/blockNumber/contractAddress`.
- Mạng mục tiêu: Ethereum Sepolia (placeholder phase).

---

## 12) Mẫu Request/Response + Axios (copy-paste)

## 12.1 Auth: lấy user hiện tại

```ts
// Cách 1: dùng wrapper api()
const me = await apiClient.get("/api/auth/me");
const user = me.data.data;

// Cách 2: dùng helper api<T>()
const user2 = await api<User>(apiClient.get("/api/auth/me"));
```

Response shape:

```json
{
  "success": true,
  "data": {
    "userId": "...",
    "email": "...",
    "fullName": "...",
    "avatarUrl": "...",
    "role": "TEACHER",
    "isActive": true
  }
}
```

## 12.2 Student: dashboard courses

```ts
const me = await api<User>(apiClient.get("/api/auth/me"));
const courses = await api<CourseResponse[]>(
  apiClient.get(`/api/student/${me.userId}/courses`),
);
```

## 12.3 Student: submit quiz

Request body:

```json
{
  "studentId": "3d505413-8993-45b2-8d41-3018ba80e0b1",
  "answers": [
    { "questionId": "1", "selectedOption": "A" },
    { "questionId": "2", "selectedOption": "C" }
  ]
}
```

Axios:

```ts
const payload = {
  studentId,
  answers: [
    { questionId: "1", selectedOption: "A" },
    { questionId: "2", selectedOption: "C" },
  ],
};

const result = await api<QuizResultResponse>(
  apiClient.post(`/api/quizzes/${quizId}/submit`, payload),
);
```

Response shape:

```json
{
  "success": true,
  "data": {
    "score": 7.5,
    "maxScore": 10,
    "status": "PASSED",
    "submittedAt": "2026-03-20T10:30:00"
  }
}
```

## 12.4 Teacher: create class

Request body:

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

Axios:

```ts
const created = await api<ClassResponse>(
  apiClient.post("/api/classes", payload),
);
```

## 12.5 Teacher/Admin: create enrollment

Request body:

```json
{
  "studentId": "3d505413-8993-45b2-8d41-3018ba80e0b1",
  "classId": "CLS-005"
}
```

Axios:

```ts
const enrollment = await api<EnrollmentResponse>(
  apiClient.post("/api/enrollments", {
    studentId,
    classId,
  }),
);
```

## 12.6 Admin: revoke certificate

Request body:

```json
{
  "reason": "Academic integrity violation",
  "revokedBy": "admin-user-id"
}
```

Axios:

```ts
const revoked = await api<RevokeResponse>(
  apiClient.post(`/api/certificates/${certificateId}/revoke`, {
    reason,
    revokedBy: adminUserId,
  }),
);
```

## 12.7 Admin: update user status/role

```ts
await api(
  apiClient.put(`/api/admin/users/${userId}/status`, {
    isActive: false,
    reason: "Account suspended",
  }),
);

await api(
  apiClient.put(`/api/admin/users/${userId}/role`, {
    role: "TEACHER",
  }),
);
```

## 12.8 Error handling với raw axios (khi không dùng helper)

```ts
try {
  const response = await apiClient.get("/api/auth/me");
  const user = response.data.data;
} catch (error: any) {
  const status = error?.response?.status;
  const message = error?.response?.data?.error || "Có lỗi xảy ra";

  if (status === 401) {
    localStorage.removeItem("token");
    window.location.href = "/login";
  } else {
    console.error(message);
  }
}
```

---

## 13) Tài liệu tham chiếu

- `docs/FE-IMPLEMENTATION-GUIDE-SIMPLE.md`
- `docs/UI-DIRECTION-ROADMAP.md`
- `docs/ENDPOINT-SUMMARY.md`
- `docs/API-DOCUMENTATION.md`
- `docs/BLOCKCHAIN-TODO-SEPOLIA.md`
