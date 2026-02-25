package main.backend.lms.constant;

public enum QuizStatus {
    DRAFT,     // Đang soạn, SV chưa thấy
    PUBLISHED, // Đã xuất bản, SV có thể làm bài
    CLOSED     // Đã đóng bài Quiz (Xóa mềm)
}