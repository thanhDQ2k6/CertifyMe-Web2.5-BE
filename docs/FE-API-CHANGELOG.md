# 📌 FE API Changelog (Cập nhật mới nhất từ Backend)

Tài liệu này tổng hợp các thay đổi, sửa lỗi và cập nhật Response API mới nhất từ Backend để team Frontend (FE) nắm thông tin và ghép nối chính xác.

## 🚀 1. Bổ sung trường dữ liệu (New Fields)

### 1.1. Bổ sung `studentCode` vào API Danh sách Học viên trong Lớp

- **Endpoint:** `GET /api/classes/{classId}/students`
- **Mô tả:** Đã bổ sung field `studentCode` vào từng object học viên trả về.
- **Mục đích:** Hỗ trợ FE lấy được Mã học sinh (`HS...`) để truyền trực tiếp vào API Xoá học viên khỏi lớp (`DELETE /api/enrollments?studentCode=...`).
- **Response mẫu (snippet):**
  ```json
  {
    "studentId": "550e8400-...",
    "studentCode": "HS00001", // <--- FIELD MỚI
    "fullName": "Nguyễn Văn A",
    "email": "a@gmail.com"
  }
  ```

### 1.2. Bổ sung `studentCode` vào API Chi tiết Chứng chỉ (Admin)

- **Endpoint:** `GET /api/certificates/{certificateId}`
- **Mô tả:** Đã bổ sung `studentCode` hiển thị mã học sinh bên trong object biểu diễn chi tiết chứng chỉ.
- **Response mẫu (snippet):**
  ```json
  {
    "certificateId": "CERT001",
    "studentId": "550e8400-...",
    "studentCode": "HS00001", // <--- FIELD MỚI
    "studentName": "Nguyễn Văn A",
    "studentEmail": "a@gmail.com",
    "classId": "CLS001"
  }
  ```

---

## 🐛 2. Sửa lỗi API (Bug Fixes - 500 Internal Server Error)

### 2.1. Lỗi crash hệ thống khi tìm kiếm Học viên / Giáo viên

- **Endpoints ảnh hưởng:**
  - `GET /api/users/search/students?q={keyword}`
  - `GET /api/users/search/teachers?q={keyword}`
- **Lý do lỗi cũ:** Lỗi map kiểu dữ liệu (Type Mismatch) khi truy vấn qua `RoleType`.
- **Trạng thái:** ✅ **Đã fix**. FE hiện tại có thể gọi API search trực tiếp trên thanh tìm kiếm khi thêm học viên vào lớp. Trả về đúng HTTP 200 kèm danh sách mảng obj kết quả.

### 2.2. Lỗi crash hệ thống khi tìm kiếm Chứng chỉ ở trang Admin

- **Endpoint ảnh hưởng:** `GET /api/certificates/search?q={keyword}&status={status}`
- **Lý do lỗi cũ:** Lỗi syntax JPA Query khi nhúng `LIKE %:q%`.
- **Trạng thái:** ✅ **Đã fix**. API đã tìm kiếm ổn định và tự động hỗ trợ **[Không phân biệt hoa/thường (Case-insensitive)]** đối với các dữ kiện bao gồm: Tên học viên, Email, Mã Lớp, Verification Hash. Màn hình quản lý chứng chỉ trên FE có thể dùng tìm kiếm ngay lúc này.
