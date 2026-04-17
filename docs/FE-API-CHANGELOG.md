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

---

## 🔗 3. Cập nhật API Blockchain & Tự cấp chứng chỉ (Quan trọng)

### 3.1. API Mới: Học sinh tự cấp chứng chỉ (Mint Certificate)

- **Endpoint:** `POST /api/student/{studentId}/classes/{classId}/certificates/issue`
- **Mô tả:** API gọi smart contract để khắc chứng chỉ lên blockchain Sepolia. Điều kiện: Học sinh phải pass 100% tất cả các bài Quizzes của lớp học `classId`.
- **Response mẫu:**
  ```json
  {
    "status": "success",
    "data": {
      "certificateId": "CERT_123456",
      "courseName": "Xây dựng dApp với Web3",
      "courseCode": "WEB3_101",
      "verificationHash": "0xabc123...",
      "status": "issued",
      "issuedAt": "2026-04-17",
      "blockchainInfo": {
        "hash": "0xabc123...",
        "txHash": "0xdef456...",
        "block": null,
        "contract": "0x89564412a6949D8696D6D89EdF967920b61903AE"
      }
    }
  }
  ```

### 3.2. Cập nhật Data: Thu hồi chứng chỉ bởi Admin (Revoke Certificate)

- **Endpoint:** `POST /api/certificates/{certificateId}/revoke`
- **Mô tả:** API hiện tại đã được liên kết với Blockchain. Khi thu hồi, backend sẽ gọi contract để huỷ chứng chỉ trên mạng Sepolia. Giao dịch mạng sẽ mất một chút thời gian (khoảng 5-15s). Yêu cầu FE thêm hiệu ứng loading khi gọi API.

### 3.3. Cập nhật Model: Xoá bỏ field `blockNumber`

- **Endpoints ảnh hưởng:**
  - `GET /api/student/{studentId}/certificates`
  - `GET /api/certificates/{certificateId}`
  - `GET /api/courses/{courseId}` (Trong phần chi tiết chứng chỉ)
- **Mô tả:** Cột `blockNumber` đã bị xoá khỏi cơ sở dữ liệu. Do đó thuộc tính `block` hoặc `blockNumber` trong object `blockchainInfo` ở các API này hiện tại sẽ luôn trả về giá trị `null` hoặc rỗng `""`. FE có thể ẩn thông tin Block trên giao diện hoặc báo "N/A".
