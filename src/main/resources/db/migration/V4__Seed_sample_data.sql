
-- ==============================================================
-- 2. ĐỔ DỮ LIỆU USERS (Student, Teacher, Admin)
-- ==============================================================
INSERT INTO users (user_id, email, full_name, role_id, avatar_url, is_active, created_at) VALUES
 ('550e8400-e29b-41d4-a716-446655440000', 'annv@fpt.edu.vn', 'Nguyễn Văn An', 1, 'https://lh3.googleusercontent.com/...', TRUE, '2025-09-01 08:00:00'),
('660e8400-e29b-41d4-a716-446655440001', 'bichtt@fpt.edu.vn', 'Trần Thị Bích', 1, 'https://lh3.googleusercontent.com/...', TRUE, '2025-09-01 08:00:00'),
 ('770e8400-e29b-41d4-a716-446655440002', 'anva@fpt.edu.vn', 'Thầy Nguyễn Văn A', 2, 'http://googleusercontent.com/profile/picture/teacher...', TRUE, '2025-09-01 08:00:00'),
 ('admin-user-id', 'admin@fpt.edu.vn', 'Admin System', 3, NULL, TRUE, '2025-09-01 08:00:00');

-- ==============================================================
-- 3. ĐỔ DỮ LIỆU KHÓA HỌC VÀ LỚP HỌC (COURSES & CLASSES)
-- ==============================================================
INSERT INTO courses (course_id, course_code, course_name, description, is_active) VALUES
('c1', 'JAVA6', 'Lập trình Java 6', 'Khóa học lập trình Java cơ bản', TRUE);

INSERT INTO classes (class_id, course_id, class_code, teacher_id, start_date, end_date, total_quizzes, status, created_at) VALUES
 ('cl1', 'c1', 'SD18301', '770e8400-e29b-41d4-a716-446655440002', '2026-01-01', '2026-04-01', 5, 'ACTIVE', '2026-01-01 08:00:00');

-- ==============================================================
-- 4. ĐỔ DỮ LIỆU ĐĂNG KÝ HỌC (ENROLLMENTS)
-- ==============================================================
INSERT INTO enrollments (student_id, class_id, passed_quizzes, final_grade, joined_at, status) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'cl1', 5, 8.5, '2026-01-01 08:00:00', 'PASSED');
INSERT INTO enrollments (student_id, class_id, passed_quizzes, final_grade, joined_at, status) VALUES
('660e8400-e29b-41d4-a716-446655440001', 'cl1', 3, 7.0, '2026-01-01 08:00:00', 'LEARNING');

-- ==============================================================
-- 5. ĐỔ DỮ LIỆU BÀI KIỂM TRA (QUIZZES & QUESTIONS)
-- ==============================================================
INSERT INTO quizzes (quiz_id, class_id, title, description, duration_minutes, passing_score, max_attempts, status, created_at) VALUES
 ('q1', 'cl1', 'Lab 1: Biến và kiểu dữ liệu', 'Bài kiểm tra số 1', 60, 5.0, 3, 'PUBLISHED', '2026-01-05 10:00:00'),
 ('q2', 'cl1', 'Lab 2: Vòng lặp', 'Bài kiểm tra số 2', 60, 5.0, 3, 'PUBLISHED', '2026-01-10 10:00:00'),
 ('q3', 'cl1', 'Lab 3: OOP', 'Bài kiểm tra số 3', 60, 5.0, 3, 'DRAFT', '2026-01-15 10:00:00');

INSERT INTO quiz_questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
 ('q1', 'Java là gì?', 'Ngôn ngữ lập trình', 'Hệ điều hành', 'Cơ sở dữ liệu', 'Trình duyệt web', 'A'),
 ('q1', 'Kiểu dữ liệu nào không phải kiểu nguyên thủy (primitive) trong Java?', 'int', 'boolean', 'String', 'char', 'C');

-- ==============================================================
-- 6. ĐỔ DỮ LIỆU BÀI LÀM CỦA SINH VIÊN (ATTEMPTS)
-- ==============================================================
INSERT INTO quiz_attempts (student_id, quiz_id, score, is_passed, is_best_attempt, started_at, submitted_at) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'q1', 8.5, TRUE, TRUE, '2026-01-15 09:30:00', '2026-01-15 10:30:00');

-- ==============================================================
-- 7. ĐỔ DỮ LIỆU CHỨNG CHỈ BLOCKCHAIN (CERTIFICATES)
-- ==============================================================
INSERT INTO certificates (certificate_id, student_id, class_id, issue_date, expiration_date, certificate_hash, transaction_hash, block_number, contract_address, status, created_at) VALUES
('CERT-156', '550e8400-e29b-41d4-a716-446655440000', 'cl1', '2026-04-01', NULL, '0x7a8b9c1d2e3f4a5b6c7d8e9f0a1b2c3d', '0x999888777666555444333222111000', 12345678, '0xABC123DEF456789', 'ISSUED', '2026-04-01 14:00:00'),
('CERT-123', '550e8400-e29b-41d4-a716-446655440000', 'cl1', '2026-04-01', NULL, '0x7a8b9c1d2e3f4a5b6c7d8e9f0a1b2c3d', '0x999888777666555444333222111000', 12345678, '0xABC123DEF456789', 'ISSUED', '2026-04-01 14:00:00');