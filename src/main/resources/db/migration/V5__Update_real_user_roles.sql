-- ==============================================================
-- UPDATE ROLES FOR REAL TEST ACCOUNTS
-- ==============================================================
-- Hai tài khoản thật của team để test OAuth flow
-- ==============================================================
-- Thanh -> TEACHER (role_id = 2)
UPDATE users
SET
    role_id = 2,
    updated_at = CURRENT_TIMESTAMP
WHERE
    email = 'thanhdqts00628@fpt.edu.vn';

-- Vinh -> STUDENT (role_id = 1) - giữ nguyên, chỉ cần đảm bảo
UPDATE users
SET
    role_id = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE
    email = 'vinhdqts00629@fpt.edu.vn';

-- ==============================================================
-- ENROLL VINH (STUDENT) VÀO CÁC LỚP ĐỂ TEST
-- ==============================================================
-- Đăng ký vào lớp SD18301 (Java) của teacher USR-TCH-001
INSERT INTO
    enrollments (
        student_id,
        class_id,
        passed_quizzes,
        final_grade,
        status
    )
SELECT
    '3d505413-8993-45b2-8d41-3018ba80e0b1', -- Vinh's user_id
    'CLS-001', -- Lớp Java SD18301
    0,
    NULL,
    'LEARNING'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            enrollments
        WHERE
            student_id = '3d505413-8993-45b2-8d41-3018ba80e0b1'
            AND class_id = 'CLS-001'
    );

-- Đăng ký vào lớp FE18301 (React) của teacher USR-TCH-002
INSERT INTO
    enrollments (
        student_id,
        class_id,
        passed_quizzes,
        final_grade,
        status
    )
SELECT
    '3d505413-8993-45b2-8d41-3018ba80e0b1', -- Vinh's user_id
    'CLS-003', -- Lớp React FE18301
    0,
    NULL,
    'LEARNING'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            enrollments
        WHERE
            student_id = '3d505413-8993-45b2-8d41-3018ba80e0b1'
            AND class_id = 'CLS-003'
    );

-- ==============================================================
-- ASSIGN THANH (TEACHER) LÀM GIÁO VIÊN MỘT LỚP
-- ==============================================================
-- Tạo lớp mới cho Thanh dạy (Python course)
INSERT INTO
    classes (
        class_id,
        course_id,
        class_code,
        teacher_id,
        start_date,
        end_date,
        total_quizzes,
        status
    )
VALUES
    (
        'CLS-005',
        'CRS-003', -- Python course
        'DS18302',
        'c167d02c-260e-464a-9443-ebfbc212f300', -- Thanh's user_id
        '2026-03-01',
        '2026-08-01',
        3,
        'ACTIVE'
    );

-- Enroll Vinh vào lớp của Thanh để test teacher-student interaction
INSERT INTO
    enrollments (
        student_id,
        class_id,
        passed_quizzes,
        final_grade,
        status
    )
VALUES
    (
        '3d505413-8993-45b2-8d41-3018ba80e0b1', -- Vinh's user_id
        'CLS-005', -- Thanh's class
        0,
        NULL,
        'LEARNING'
    );