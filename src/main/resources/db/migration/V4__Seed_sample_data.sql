-- ==============================================================
-- 1. SEED DATA CHO USERS (GIÁO VIÊN & SINH VIÊN)
-- ==============================================================
-- Tạo Giáo viên (teacher_id: t1) để test API 4.1
INSERT INTO users (user_id, email, full_name, role_id, is_active)
VALUES ('t1', 'teacher@fpt.edu.vn', 'Thầy Nguyễn Văn A', 2, TRUE);

-- Tạo Sinh viên mẫu (studentId cho API 4.3 & 4.10)
INSERT INTO users (user_id, email, full_name, role_id, is_active)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'annv@fpt.edu.vn', 'Nguyễn Văn An', 1, TRUE),
       ('660e8400-e29b-41d4-a716-446655440001', 'bichtt@fpt.edu.vn', 'Trần Thị Bích', 1, TRUE);

-- ==============================================================
-- 2. SEED DATA CHO LMS CORE (KHÓA HỌC & LỚP HỌC)
-- ==============================================================
-- Tạo Khóa học (course_id: c1)
INSERT INTO courses (course_id, course_code, course_name, description, is_active)
VALUES ('c1', 'JAVA6', 'Lập trình Java 6', 'Khóa học Java nâng cao với Spring Boot', TRUE);

-- Tạo Lớp học (class_id: cl1) khớp với API 4.1 & 4.2
INSERT INTO classes (class_id, course_id, class_code, teacher_id, start_date, end_date, total_quizzes, status)
VALUES ('cl1', 'c1', 'SD18301', 't1', '2026-01-01', '2026-04-01', 5, 'ACTIVE');

-- Đăng ký sinh viên vào lớp (Enrollments cho API 4.3)
INSERT INTO enrollments (student_id, class_id, passed_quizzes, joined_at, status)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'cl1', 5, '2026-01-01 08:00:00', 'PASSED'),
       ('660e8400-e29b-41d4-a716-446655440001', 'cl1', 3, '2026-01-01 08:00:00', 'LEARNING');

-- ==============================================================
-- 3. SEED DATA CHO QUIZZES (BÀI KIỂM TRA & BÀI NỘP)
-- ==============================================================
-- Tạo Quiz (quiz_id: q1) cho API 4.6 & 4.7
INSERT INTO quizzes (quiz_id, class_id, title, duration_minutes, passing_score, status)
VALUES ('q1', 'cl1', 'Lab 1: Biến và kiểu dữ liệu', 60, 5.0, 'PUBLISHED');

-- Tạo câu hỏi cho Quiz (Khớp Request 4.7)
INSERT INTO quiz_questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer)
VALUES ('q1', 'Java là gì?', 'Ngôn ngữ lập trình', 'Hệ điều hành', 'Cơ sở dữ liệu', 'Trình duyệt web', 'A');

-- Tạo bài nộp mẫu (Attempts cho API 4.10)
INSERT INTO quiz_attempts (student_id, quiz_id, score, is_passed, submitted_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'q1', 8.5, TRUE, '2026-02-24 10:30:00');