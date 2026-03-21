# FE Implementation Guide (Simple)

> Mục tiêu: hướng dẫn ngắn để FE biết **phải làm gì trước/sau** theo API hiện có.
> Phạm vi: chỉ định hướng thực thi màn hình + luồng nghiệp vụ. Chưa đi sâu kỹ thuật data wrapper.

---

## 1) Thứ tự làm việc đề xuất

1. Auth + role routing
2. Student core flow
3. Teacher core flow
4. Admin core flow
5. Error/empty/loading states

---

## 2) Auth + Role Routing (bắt buộc)

## FE cần làm

- Tạo trang login với nút Google OAuth.
- Tạo trang callback nhận token/role từ BE redirect.
- Sau callback: gọi `GET /api/auth/me` để lấy user thật, lưu session.
- Route theo role:
  - `STUDENT` -> `/student/dashboard`
  - `TEACHER` -> `/teacher/classes`
  - `ADMIN` -> `/admin/certificates`
- Lưu ý: BE callback có `redirect` gợi ý (`/student/dashboard`, `/teacher/dashboard`, `/admin/dashboard`). FE có thể dùng route nội bộ khác, nhưng nên ưu tiên xác nhận role bằng `GET /api/auth/me`.
- Gắn logout bằng `POST /api/auth/logout` + clear session phía FE.

## Thành phần UI cần có

- **Login**: logo/app title, nút `Login with Google`, khối thông báo lỗi đăng nhập.
- **OAuth Callback**: loading indicator, xử lý thành công/thất bại, auto redirect theo role.
- **App Shell (sau login)**: header, user avatar + tên, role badge, nút logout.

## Endpoint

- `GET /api/auth/me`
- `POST /api/auth/logout`
- `GET /api/auth/check-role` (optional health-check role)

## Done khi

- Đăng nhập xong vào đúng dashboard theo role.
- Refresh trang vẫn giữ được phiên đăng nhập.
- Logout quay về login sạch session.

---

## 3) Student Core Flow

## Màn hình cần có

1. Student Dashboard
2. Course Detail
3. Quiz Detail / Do Quiz
4. Quiz Result
5. My Certificates

## Thành phần UI cần có

- **Student Dashboard**: danh sách course card, progress bar, badge trạng thái, CTA vào chi tiết course.
- **Course Detail**: course header (tên, giáo viên, tiến độ), danh sách quiz (pending/completed), block certificate summary.
- **Quiz Detail / Do Quiz**: tiêu đề quiz, timer, danh sách câu hỏi + lựa chọn A/B/C/D, nút submit.
- **Quiz Result**: score, trạng thái pass/fail, thời gian nộp, CTA quay lại course.
- **My Certificates**: bảng/list cert, status badge (pending/issued/revoked), hash/tx info (nếu có).

## Mapping API

- Dashboard: `GET /api/student/{studentId}/courses`
- Course detail: `GET /api/courses/{courseId}`
- Quiz detail: `GET /api/quizzes/{quizId}`
- Quiz submit: `POST /api/quizzes/{quizId}/submit`
- Quiz result: `GET /api/quizzes/{quizId}/result`
- Certificates: `GET /api/student/{studentId}/certificates`

## Done khi

- Student xem được toàn bộ course đang học.
- Vào 1 course xem được danh sách quiz + tiến độ.
- Làm quiz xong có kết quả pass/fail.
- Tab chứng chỉ hiển thị được trạng thái cert.

---

## 4) Teacher Core Flow

## Màn hình cần có

1. Teacher Classes (list)
2. Class Detail
3. Class Students
4. Quiz Management (list/create/edit/delete)
5. Quiz Submissions
6. Enrollment Action (add/remove student)

## Thành phần UI cần có

- **Teacher Classes**: bảng/list lớp, filter cơ bản theo trạng thái, CTA vào class detail.
- **Class Detail**: thông tin lớp (code, thời gian, sĩ số), action area (quản lý học viên/quiz).
- **Class Students**: bảng học viên, filter status, sort control, action enrollment.
- **Quiz Management**: danh sách quiz, nút tạo quiz, form create/edit quiz, action delete có confirm.
- **Quiz Submissions**: bảng submissions (student, score, submittedAt), trạng thái pass/fail rõ ràng.
- **Enrollment Action**: form thêm học viên (studentId/classId), nút remove enrollment, thông báo thành công/thất bại.

## Mapping API

- Classes: `GET /api/teacher/{teacherId}/classes`
- Class detail: `GET /api/classes/{classId}`
- Students: `GET /api/classes/{classId}/students?status=&sort=&order=`
- Quiz list by class: `GET /api/classes/{classId}/quizzes`
- Quiz CRUD: `POST /api/quizzes`, `PUT /api/quizzes/{quizId}`, `DELETE /api/quizzes/{quizId}`
- Submissions: `GET /api/quizzes/{quizId}/submissions`
- Enrollment: `POST /api/enrollments`, `DELETE /api/enrollments/{enrollmentId}`

## Done khi

- Teacher thấy danh sách lớp và vào được chi tiết lớp.
- Teacher xem/lọc được học viên theo trạng thái.
- Teacher tạo/sửa/xóa quiz hoạt động ổn.
- Teacher xem được danh sách nộp bài theo quiz.
- Teacher thêm/xóa enrollment được trong ngữ cảnh lớp.

---

## 5) Admin Core Flow

## Màn hình cần có

1. Certificate Overview (stats + recent)
2. Certificate Search
3. Certificate Detail
4. Verify/Revoke action
5. User Management

## Thành phần UI cần có

- **Certificate Overview**: stat cards (total/issued/revoked), danh sách recent certificates.
- **Certificate Search**: ô tìm kiếm keyword, filter status, danh sách kết quả + phân trang.
- **Certificate Detail**: thông tin cert, học viên, lớp, quiz results, blockchain info (nếu có).
- **Verify/Revoke**: nút verify, nút revoke + form reason + confirm dialog.
- **User Management**: bảng user, filter role/status, action update status, action update role.

## Mapping API

- Stats: `GET /api/admin/certificates/stats`
- Recent: `GET /api/certificates/recent?limit=&page=`
- Search: `GET /api/certificates/search?q=&status=&limit=`
- Detail: `GET /api/certificates/{certificateId}`
- Verify: `POST /api/certificates/{certificateId}/verify`
- Revoke: `POST /api/certificates/{certificateId}/revoke`
- Users: `GET /api/admin/users?role=&status=&page=&limit=`
- Update status: `PUT /api/admin/users/{userId}/status`
- Update role: `PUT /api/admin/users/{userId}/role`

## Done khi

- Admin tra cứu cert theo keyword + trạng thái được.
- Admin xem detail cert có thông tin verify/revoke.
- Admin đổi trạng thái user và role user được.

---

## 6) Trạng thái UI bắt buộc (mọi màn)

- Loading state
- Empty state
- Error state (401/403/404/500)
- Success feedback cho thao tác tạo/sửa/xóa/verify/revoke

---

## 7) Scope guard

- Chỉ làm theo endpoint hiện có.
- Không thêm feature ngoài API ở giai đoạn này.
- Blockchain UI chỉ hiển thị dữ liệu có sẵn; chưa giả lập luồng on-chain mới.

---

## 8) Tài liệu dùng kèm

- API tổng hợp nhanh: `docs/ENDPOINT-SUMMARY.md`
- Định hướng UX/UI: `docs/UI-DIRECTION-ROADMAP.md`
- API chi tiết: `docs/API-DOCUMENTATION.md`
- Tích hợp API chi tiết (Vue 5173): `docs/FE-API-INTEGRATION-GUIDE-VUE5173.md`
