-- ==============================================================
-- V2: SEED DATA CHO TÀI KHOẢN THẬT (Thanh & Vinh)
-- ==============================================================
-- Thanh: TEACHER (GV00002) - dạy lớp Python
-- Vinh: STUDENT (HS00003) - học cả 3 lớp, có tiến độ và chứng chỉ
-- ==============================================================
-- ==============================================================
-- 1. CẬP NHẬT ROLE CHO THANH -> TEACHER
-- ==============================================================
UPDATE users
SET
  role_id = 2,
  user_code = 'GV00002',
  updated_at = NOW()
WHERE
  user_id = 'b464e72e-60d0-435d-b759-4d98293e8c46';

-- Cập nhật Vinh's user_code (đảm bảo consistent)
UPDATE users
SET
  user_code = 'HS00003',
  updated_at = NOW()
WHERE
  user_id = '20bde48c-c9b2-4d62-b086-137fd0a5ec60';

-- ==============================================================
-- 2. TẠO LỚP PYTHON DO THANH DẠY (3 quizzes)
-- ==============================================================
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
    'CLS-003',
    'CRS-003',
    'PY18301',
    'b464e72e-60d0-435d-b759-4d98293e8c46',
    '2026-01-10',
    '2026-05-10',
    3,
    'ACTIVE'
  );

-- ==============================================================
-- 3. TẠO QUIZZES CHO LỚP PYTHON (do Thanh dạy)
-- ==============================================================
INSERT INTO
  quizzes (
    quiz_id,
    class_id,
    title,
    description,
    duration_minutes,
    passing_score,
    status
  )
VALUES
  (
    'QZ-007',
    'CLS-003',
    'Quiz 1: Python Basics',
    'Biến, kiểu dữ liệu, operators',
    30,
    5.0,
    'PUBLISHED'
  ),
  (
    'QZ-008',
    'CLS-003',
    'Quiz 2: Control Flow',
    'if-else, loops, functions',
    30,
    5.0,
    'PUBLISHED'
  ),
  (
    'QZ-009',
    'CLS-003',
    'Quiz 3: OOP Python',
    'Class, inheritance, modules',
    45,
    5.0,
    'PUBLISHED'
  );

-- ==============================================================
-- 4. TẠO CÂU HỎI CHO QUIZZES PYTHON (5 câu mỗi quiz)
-- ==============================================================
-- QZ-007: Python Basics
INSERT INTO
  quiz_questions (
    quiz_id,
    question_text,
    option_a,
    option_b,
    option_c,
    option_d,
    correct_answer
  )
VALUES
  (
    'QZ-007',
    'Python là ngôn ngữ lập trình kiểu gì?',
    'Compiled',
    'Interpreted',
    'Assembly',
    'Machine',
    'B'
  ),
  (
    'QZ-007',
    'Cú pháp khai báo biến đúng?',
    'int x = 5',
    'x = 5',
    'var x = 5',
    'let x = 5',
    'B'
  ),
  (
    'QZ-007',
    'Hàm in ra màn hình?',
    'console.log()',
    'System.out.print()',
    'print()',
    'echo()',
    'C'
  ),
  (
    'QZ-007',
    'Kiểu dữ liệu list trong Python?',
    'Immutable',
    'Mutable',
    'Static',
    'Final',
    'B'
  ),
  (
    'QZ-007',
    'Comment 1 dòng dùng ký tự gì?',
    '//',
    '/*',
    '#',
    '--',
    'C'
  );

-- QZ-008: Control Flow
INSERT INTO
  quiz_questions (
    quiz_id,
    question_text,
    option_a,
    option_b,
    option_c,
    option_d,
    correct_answer
  )
VALUES
  (
    'QZ-008',
    'Vòng lặp duyệt list?',
    'for i in list',
    'foreach(list)',
    'for(i : list)',
    'loop list',
    'A'
  ),
  (
    'QZ-008',
    'Từ khóa định nghĩa hàm?',
    'function',
    'func',
    'def',
    'fn',
    'C'
  ),
  (
    'QZ-008',
    'Kết quả: range(1,5)?',
    '[1,2,3,4]',
    '[1,2,3,4,5]',
    '[0,1,2,3,4]',
    '[1,5]',
    'A'
  ),
  (
    'QZ-008',
    'Hàm lambda là gì?',
    'Named function',
    'Anonymous function',
    'Class method',
    'Module',
    'B'
  ),
  (
    'QZ-008',
    'Kiểm tra x trong list?',
    'x.in(list)',
    'list.contains(x)',
    'x in list',
    'in(x, list)',
    'C'
  );

-- QZ-009: OOP Python
INSERT INTO
  quiz_questions (
    quiz_id,
    question_text,
    option_a,
    option_b,
    option_c,
    option_d,
    correct_answer
  )
VALUES
  (
    'QZ-009',
    'Constructor trong Python?',
    '__init__',
    '__construct__',
    'constructor',
    'init',
    'A'
  ),
  (
    'QZ-009',
    'Tham chiếu đến instance?',
    'this',
    'self',
    'me',
    'instance',
    'B'
  ),
  (
    'QZ-009',
    'Kế thừa class Parent?',
    'class Child extends Parent',
    'class Child(Parent)',
    'class Child : Parent',
    'Child inherits Parent',
    'B'
  ),
  (
    'QZ-009',
    'Private attribute?',
    '_name',
    '__name',
    'private name',
    '#name',
    'B'
  ),
  (
    'QZ-009',
    'Import module math?',
    'include math',
    'import math',
    'require math',
    'using math',
    'B'
  );

-- ==============================================================
-- 5. GHI DANH VINH VÀO CẢ 3 LỚP
-- ==============================================================
INSERT INTO
  enrollments (
    student_id,
    class_id,
    passed_quizzes,
    final_grade,
    joined_at,
    status
  )
VALUES
  -- Java class: đã hoàn thành, pass 3/3 quizzes
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'CLS-001',
    3,
    8.5,
    '2026-01-16',
    'PASSED'
  ),
  -- React class: đang học, pass 2/3 quizzes
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'CLS-002',
    2,
    NULL,
    '2026-02-02',
    'LEARNING'
  ),
  -- Python class (của Thanh): vừa ghi danh
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'CLS-003',
    1,
    NULL,
    '2026-01-12',
    'LEARNING'
  );

-- ==============================================================
-- 6. QUIZ ATTEMPTS CỦA VINH
-- ==============================================================
-- Java class (CLS-001): Hoàn thành cả 3 quiz, tất cả đều Pass
INSERT INTO
  quiz_attempts (
    student_id,
    quiz_id,
    score,
    is_passed,
    is_best_attempt,
    started_at,
    submitted_at
  )
VALUES
  -- QZ-001: 2 lần thử, lần 2 tốt hơn
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'QZ-001',
    6.0,
    TRUE,
    FALSE,
    '2026-01-20 09:00:00',
    '2026-01-20 09:25:00'
  ),
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'QZ-001',
    9.0,
    TRUE,
    TRUE,
    '2026-01-22 10:00:00',
    '2026-01-22 10:20:00'
  ),
  -- QZ-002: 1 lần đậu ngay
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'QZ-002',
    8.0,
    TRUE,
    TRUE,
    '2026-02-01 14:00:00',
    '2026-02-01 14:28:00'
  ),
  -- QZ-003: 1 lần rớt, 1 lần đậu
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'QZ-003',
    4.0,
    FALSE,
    FALSE,
    '2026-02-10 09:00:00',
    '2026-02-10 09:40:00'
  ),
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'QZ-003',
    8.5,
    TRUE,
    TRUE,
    '2026-02-12 10:00:00',
    '2026-02-12 10:35:00'
  );

-- React class (CLS-002): Pass 2/3, đang làm quiz 3
INSERT INTO
  quiz_attempts (
    student_id,
    quiz_id,
    score,
    is_passed,
    is_best_attempt,
    started_at,
    submitted_at
  )
VALUES
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'QZ-004',
    7.5,
    TRUE,
    TRUE,
    '2026-02-15 08:00:00',
    '2026-02-15 08:25:00'
  ),
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'QZ-005',
    8.0,
    TRUE,
    TRUE,
    '2026-02-20 14:00:00',
    '2026-02-20 14:22:00'
  );

-- QZ-006: Chưa làm (để showcase "chưa hoàn thành")
-- Python class (CLS-003 - của Thanh): Pass 1/3, đang học
INSERT INTO
  quiz_attempts (
    student_id,
    quiz_id,
    score,
    is_passed,
    is_best_attempt,
    started_at,
    submitted_at
  )
VALUES
  (
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'QZ-007',
    7.0,
    TRUE,
    TRUE,
    '2026-01-25 10:00:00',
    '2026-01-25 10:28:00'
  );

-- QZ-008, QZ-009: Chưa làm
-- ==============================================================
-- 7. CHỨNG CHỈ CHO VINH (LỚP JAVA ĐÃ HOÀN THÀNH)
-- ==============================================================
INSERT INTO
  certificates (
    certificate_id,
    student_id,
    class_id,
    issue_date,
    certificate_hash,
    transaction_hash,
    status,
    created_at
  )
VALUES
  (
    'CERT-VINH-JAVA-001',
    '20bde48c-c9b2-4d62-b086-137fd0a5ec60',
    'CLS-001',
    '2026-02-15',
    '0x7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b',
    '0xabc123def456789abc123def456789abc123def456789abc123def456789abc1',
    'ISSUED',
    '2026-02-15 12:00:00'
  );

-- ==============================================================
-- SUMMARY:
-- ==============================================================
-- Thanh (GV00002): Teacher - dạy lớp PY18301 (Python)
-- Vinh (HS00003): Student
--   - CLS-001 (Java): PASSED, 3/3 quizzes, có certificate
--   - CLS-002 (React): LEARNING, 2/3 quizzes
--   - CLS-003 (Python): LEARNING, 1/3 quizzes
--   - Total attempts: 8 (với các trạng thái khác nhau)
-- ==============================================================