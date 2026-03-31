# UI Direction Roadmap (Định hướng giao diện, chưa phải hướng dẫn)

> Mục tiêu: giúp chốt "nên làm màn hình gì trước" và "trải nghiệm nên ưu tiên điều gì" dựa trên API hiện có.
> Phạm vi: định hướng sản phẩm/UI cho FE team, không đi vào chi tiết triển khai kỹ thuật.

---

## 1) Bối cảnh ra quyết định

- Backend đã có endpoint rõ theo role: Student, Teacher, Admin.
- Module chứng chỉ đang chạy DB-first; blockchain Sepolia mới ở trạng thái placeholder.
- Vì vậy, UI nên tối ưu cho **độ rõ nghiệp vụ**, tránh phụ thuộc vào blockchain real-time ở giai đoạn hiện tại.

---

## 2) Design mindset đề xuất (theo profile INFJ 5w6, Tritype 514)

Mục tiêu của mindset này là: **rõ ràng, có chiều sâu, và nhất quán**, thay vì nhiều hiệu ứng hoặc nhiều điểm tương tác không cần thiết.

- **Clarity-first**: mỗi màn hình trả lời 1 câu hỏi nghiệp vụ chính.
- **Low-noise**: giảm thành phần trang trí; ưu tiên thông tin quan trọng và trạng thái.
- **Decision support**: hiển thị dữ liệu giúp ra quyết định (tiến độ, trạng thái pass/fail, trạng thái chứng chỉ).
- **Traceability**: mọi hành động quan trọng có trạng thái trước/sau rõ ràng (verify, revoke, update role/status).
- **Predictable flow**: cùng một pattern điều hướng/list/detail/form cho toàn hệ thống.

---

## 3) Information Architecture (IA) nên chốt

## 3.1 Shared shell

- Login / OAuth2 callback
- App shell (header, user profile, role badge, logout)
- Unauthorized / Forbidden / Not found

## 3.2 Student zone

- Student Dashboard (courses)
- Course Detail (progress + quiz list + certificate summary)
- Quiz Detail / Do Quiz
- Quiz Result
- My Certificates

## 3.3 Teacher zone

- Teacher Classes (list)
- Class Detail
- Class Students
- Quiz Management (list/create/edit/delete)
- Quiz Submissions
- Enrollment action (add/remove student)

## 3.4 Admin zone

- Certificate Overview (stats + recent)
- Certificate Search & Filter
- Certificate Detail (verify/revoke)
- User Management (list/filter/update status/update role)

---

## 4) Ưu tiên theo phase (để không vỡ scope)

## Phase 1 - Core usability (bắt buộc)

Tập trung vào luồng học tập và vận hành chính:

1. Auth + role-based routing
2. Student Dashboard + Course Detail
3. Quiz flow cho Student (view -> submit -> result)
4. Teacher Classes + Class Students
5. Admin Certificate list/detail cơ bản

**Kết quả mong muốn:** mọi role có thể hoàn thành nhiệm vụ chính end-to-end.

## Phase 2 - Operational control

Tăng năng lực quản trị:

1. Teacher Quiz CRUD đầy đủ + submissions
2. Enrollment create/delete trong ngữ cảnh lớp
3. Admin User management (status/role)
4. Search/filter/pagination hoàn chỉnh cho Admin

**Kết quả mong muốn:** giảm thao tác thủ công, tăng tốc xử lý nghiệp vụ.

## Phase 3 - Trust layer (khi blockchain ready)

1. Bật hiển thị trạng thái on-chain rõ ràng
2. Verify/revoke có timeline giao dịch (submitted/confirmed/failed)
3. Deep-link explorer Sepolia tại màn certificate detail

**Kết quả mong muốn:** tính xác thực cao hơn mà không phá vỡ UX hiện có.

---

## 5) Định hướng từng nhóm màn hình (không phải hướng dẫn)

## 5.1 Student

- Trọng tâm: "Tôi cần học gì tiếp theo?"
- KPI màn: progress minh bạch, quiz pending nổi bật, certificate status dễ hiểu.
- Tránh: nhồi thông tin admin/teacher vào student view.

## 5.2 Teacher

- Trọng tâm: "Lớp nào cần can thiệp?"
- KPI màn: sĩ số, tiến độ hoàn thành quiz, danh sách học viên có trạng thái rõ.
- Tránh: form dài nhiều bước nếu chỉ cần 1 thao tác CRUD đơn.

## 5.3 Admin

- Trọng tâm: "Chứng chỉ có hợp lệ và user có đúng quyền không?"
- KPI màn: tốc độ tra cứu cert, độ rõ khi verify/revoke, auditability hành động.
- Tránh: xác nhận mơ hồ cho hành động rủi ro (revoke, đổi role).

---

## 6) Role flows sau OAuth login (Teacher & Student)

## 6.1 Flow chung sau login OAuth

1. User bấm login Google.
2. OAuth success -> BE tạo token + role -> redirect FE callback.
3. FE lưu token, gọi `GET /api/auth/me` để lấy hồ sơ + role thật.
4. FE route theo role:
   - `STUDENT` -> Student Dashboard
   - `TEACHER` -> Teacher Classes
5. FE có thể gọi thêm `GET /api/auth/check-role` để kiểm tra nhanh trạng thái phân quyền.

## 6.2 Student flow (Course -> Quiz -> Certificate)

Mục tiêu người học: biết mình đang học gì, làm quiz nào tiếp theo, và có/không có chứng chỉ.

1. **Student Dashboard**
   - API: `GET /api/student/{studentId}/courses`
   - Quyết định UI: course nào cần ưu tiên (progress thấp, quiz chưa làm).

2. **Course Detail**
   - API: `GET /api/courses/{courseId}`
   - Quyết định UI: quiz nào pending/completed, tình trạng hoàn thành khóa.

3. **Quiz Detail / Làm bài**
   - API: `GET /api/quizzes/{quizId}`
   - Quyết định UI: hiển thị câu hỏi rõ ràng, trạng thái đã chọn đáp án.

4. **Submit + Result**
   - API submit: `POST /api/quizzes/{quizId}/submit`
   - API kết quả: `GET /api/quizzes/{quizId}/result`
   - Quyết định UI: pass/fail, score, bước tiếp theo.

5. **My Certificates**
   - API: `GET /api/student/{studentId}/certificates`
   - Quyết định UI: chứng chỉ đã cấp/chưa cấp, thông tin xác minh.

## 6.3 Teacher flow (Classes -> Enrollment -> Quiz -> Class outcomes)

Mục tiêu giảng viên: quản lớp, quản người học trong lớp, và kiểm soát chất lượng quiz.

1. **Teacher Classes (list)**
   - API: `GET /api/teacher/{teacherId}/classes`
   - Quyết định UI: lớp nào cần xử lý ngay (quy mô, trạng thái).

2. **Class Detail**
   - API: `GET /api/classes/{classId}`
   - Quyết định UI: context lớp trước khi thao tác học viên/quiz.

3. **Class Students**
   - API: `GET /api/classes/{classId}/students?status=&sort=&order=`
   - Quyết định UI: ai đang LEARNING/PASSED, ai cần can thiệp.

4. **Enrollment actions (trong ngữ cảnh lớp)**
   - API thêm: `POST /api/enrollments`
   - API xóa: `DELETE /api/enrollments/{enrollmentId}`
   - Quyết định UI: thêm/bỏ học viên nhanh, có xác nhận rõ ràng.

5. **Quiz Management cho lớp**
   - API danh sách quiz lớp: `GET /api/classes/{classId}/quizzes`
   - API tạo/sửa/xóa quiz: `POST /api/quizzes`, `PUT /api/quizzes/{quizId}`, `DELETE /api/quizzes/{quizId}`
   - Quyết định UI: nội dung quiz nhất quán, giảm lỗi thao tác CRUD.

6. **Quiz Submissions & chất lượng lớp**
   - API: `GET /api/quizzes/{quizId}/submissions`
   - Quyết định UI: nhìn nhanh phân bố kết quả để điều chỉnh dạy học.

7. **Liên hệ Certificate (góc nhìn giảng viên)**
   - Teacher không có endpoint cert quản trị riêng trong scope hiện tại.
   - Chứng chỉ nên được xem là outcome cuối, phản ánh qua trạng thái học tập/quiz trong lớp; nghiệp vụ verify/revoke nằm ở Admin.

---

## 7) Chuẩn trải nghiệm chung cần đồng nhất

- Cùng format dữ liệu API wrapper trên toàn app.
- Cùng pattern list -> detail -> action -> feedback.
- Cùng từ điển trạng thái (PENDING/ISSUED/REVOKED, PASSED/FAILED...).
- Cùng cơ chế xử lý lỗi 401/403/404/500 và empty state.

---

## 8) Anti-goals (để giữ sản phẩm sắc)

- Không thêm tính năng ngoài endpoint hiện có chỉ vì "đẹp".
- Không làm dashboard quá nhiều chart khi chưa có quyết định nghiệp vụ từ chart đó.
- Không phụ thuộc blockchain UX trong giai đoạn chưa bật on-chain.
- Không trộn mục tiêu Student/Teacher/Admin trên cùng một màn hình.

---

## 9) Definition of Direction Done

Tài liệu định hướng được xem là "đủ để bắt đầu UI spec" khi:

1. Chốt được danh sách màn hình theo role và phase.
2. Mỗi màn có mục tiêu ra quyết định rõ (user question -> answer).
3. Không có màn nào vượt quá endpoint hiện có.
4. Có chỗ mở cho Sepolia nhưng không làm block Phase 1/2.

---

## 10) Tài liệu liên quan

- API map: `docs/ENDPOINT-SUMMARY.md`
- Full API: `docs/API-DOCUMENTATION.md`
- Blockchain placeholder: `docs/BLOCKCHAIN-TODO-SEPOLIA.md`
- FE implementation (simple): `docs/FE-IMPLEMENTATION-GUIDE-SIMPLE.md`
