-- ==============================================================
-- SEED DATA CHO DEMO/SHOWCASE
-- ==============================================================
-- LUU Y:
-- - Email THẬT (team members) -> có thể đăng nhập qua Google OAuth
-- - Email FAKE (demo-*) -> chỉ hiển thị trong danh sách, không đăng nhập được
-- ==============================================================
-- ==============================================================
-- 1. USERS - Thay email thật của team vào đây
-- ==============================================================
-- Admin account (THAY EMAIL THẬT CỦA BẠN)
INSERT INTO
  users (
    user_id,
    email,
    full_name,
    avatar_url,
    role_id,
    is_active
  )
VALUES
  (
    'USR-ADMIN-001',
    'admin@gmail.com',
    'Admin LMS',
    NULL,
    3,
    TRUE
  );

-- Teacher accounts (THAY EMAIL THẬT CỦA TEAM)
INSERT INTO
  users (
    user_id,
    email,
    full_name,
    avatar_url,
    role_id,
    is_active
  )
VALUES
  (
    'USR-TCH-001',
    'teacher1@gmail.com',
    'Nguyễn Văn Thành',
    NULL,
    2,
    TRUE
  ),
  (
    'USR-TCH-002',
    'teacher2@gmail.com',
    'Trần Thị Hương',
    NULL,
    2,
    TRUE
  );

-- Student accounts (THAY EMAIL THẬT HOẶC ĐỂ FAKE)
INSERT INTO
  users (
    user_id,
    email,
    full_name,
    avatar_url,
    role_id,
    is_active
  )
VALUES
  (
    'USR-STU-001',
    'student1@gmail.com',
    'Lê Minh Tuấn',
    NULL,
    1,
    TRUE
  ),
  (
    'USR-STU-002',
    'student2@gmail.com',
    'Phạm Thị Lan',
    NULL,
    1,
    TRUE
  ),
  (
    'USR-STU-003',
    'student3@gmail.com',
    'Hoàng Văn Nam',
    NULL,
    1,
    TRUE
  ),
  (
    'USR-STU-004',
    'demo-student4@example.com',
    'Ngô Thị Mai',
    NULL,
    1,
    TRUE
  ),
  (
    'USR-STU-005',
    'demo-student5@example.com',
    'Đặng Văn Hùng',
    NULL,
    1,
    TRUE
  );

-- ==============================================================
-- 2. COURSES - Khóa học
-- ==============================================================
INSERT INTO
  courses (
    course_id,
    course_code,
    course_name,
    description,
    is_active
  )
VALUES
  (
    'CRS-001',
    'JAVA6',
    'Lập trình Java 6',
    'Khóa học Java từ cơ bản đến nâng cao, bao gồm OOP, Collections, JDBC, Spring Boot',
    TRUE
  ),
  (
    'CRS-002',
    'REACT3',
    'Lập trình React 3',
    'Khóa học React.js: Components, Hooks, Redux, React Router',
    TRUE
  ),
  (
    'CRS-003',
    'PYTHON',
    'Lập trình Python',
    'Python cơ bản và ứng dụng trong Data Science',
    TRUE
  );

-- ==============================================================
-- 3. CLASSES - Lớp học
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
    'CLS-001',
    'CRS-001',
    'SD18301',
    'USR-TCH-001',
    '2026-01-15',
    '2026-06-15',
    4,
    'ACTIVE'
  ),
  (
    'CLS-002',
    'CRS-001',
    'SD18302',
    'USR-TCH-001',
    '2026-02-01',
    '2026-07-01',
    3,
    'ACTIVE'
  ),
  (
    'CLS-003',
    'CRS-002',
    'FE18301',
    'USR-TCH-002',
    '2026-01-20',
    '2026-05-20',
    3,
    'ACTIVE'
  ),
  (
    'CLS-004',
    'CRS-003',
    'DS18301',
    'USR-TCH-002',
    '2025-09-01',
    '2026-01-31',
    4,
    'COMPLETED'
  );

-- ==============================================================
-- 4. ENROLLMENTS - Đăng ký học
-- ==============================================================
INSERT INTO
  enrollments (
    student_id,
    class_id,
    passed_quizzes,
    final_grade,
    status
  )
VALUES
  -- Lớp SD18301 (Java)
  ('USR-STU-001', 'CLS-001', 3, 8.5, 'LEARNING'),
  ('USR-STU-002', 'CLS-001', 4, 9.0, 'PASSED'),
  ('USR-STU-003', 'CLS-001', 2, 6.5, 'LEARNING'),
  -- Lớp SD18302 (Java)
  ('USR-STU-004', 'CLS-002', 1, NULL, 'LEARNING'),
  ('USR-STU-005', 'CLS-002', 0, NULL, 'LEARNING'),
  -- Lớp FE18301 (React)
  ('USR-STU-001', 'CLS-003', 2, 7.8, 'LEARNING'),
  ('USR-STU-003', 'CLS-003', 3, 8.2, 'PASSED'),
  -- Lớp DS18301 (Python - đã hoàn thành)
  ('USR-STU-002', 'CLS-004', 4, 9.2, 'PASSED'),
  ('USR-STU-004', 'CLS-004', 3, 7.0, 'PASSED');

-- ==============================================================
-- 5. QUIZZES - Bài kiểm tra
-- ==============================================================
-- Quizzes cho lớp SD18301 (Java)
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
    'QZ-001',
    'CLS-001',
    'Quiz 1: Biến và kiểu dữ liệu',
    'Kiểm tra kiến thức về biến, kiểu dữ liệu primitive và reference',
    30,
    5.0,
    'PUBLISHED'
  ),
  (
    'QZ-002',
    'CLS-001',
    'Quiz 2: Vòng lặp và điều kiện',
    'Kiểm tra if-else, switch-case, for, while, do-while',
    30,
    5.0,
    'PUBLISHED'
  ),
  (
    'QZ-003',
    'CLS-001',
    'Quiz 3: OOP cơ bản',
    'Class, Object, Encapsulation, Inheritance',
    45,
    5.0,
    'PUBLISHED'
  ),
  (
    'QZ-004',
    'CLS-001',
    'Quiz 4: Collections Framework',
    'List, Set, Map, Iterator',
    45,
    5.0,
    'DRAFT'
  ),
  -- Quizzes cho lớp FE18301 (React)
  (
    'QZ-005',
    'CLS-003',
    'Quiz 1: React Fundamentals',
    'JSX, Components, Props',
    30,
    5.0,
    'PUBLISHED'
  ),
  (
    'QZ-006',
    'CLS-003',
    'Quiz 2: React Hooks',
    'useState, useEffect, useContext',
    30,
    5.0,
    'PUBLISHED'
  ),
  (
    'QZ-007',
    'CLS-003',
    'Quiz 3: State Management',
    'Redux, Context API',
    45,
    5.0,
    'PUBLISHED'
  );

-- ==============================================================
-- 6. QUIZ QUESTIONS - Câu hỏi
-- ==============================================================
-- Questions cho Quiz 1 (Java - Biến và kiểu dữ liệu)
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
    'QZ-001',
    'Kiểu dữ liệu nào sau đây KHÔNG phải là primitive type trong Java?',
    'int',
    'boolean',
    'String',
    'char',
    'C'
  ),
  (
    'QZ-001',
    'Giá trị mặc định của biến boolean trong Java là gì?',
    'true',
    'false',
    'null',
    '0',
    'B'
  ),
  (
    'QZ-001',
    'Kích thước của kiểu int trong Java là bao nhiêu byte?',
    '2 bytes',
    '4 bytes',
    '8 bytes',
    '16 bytes',
    'B'
  ),
  (
    'QZ-001',
    'Câu lệnh nào sau đây khai báo đúng một biến String?',
    'string name = "Java";',
    'String name = "Java";',
    'STRING name = "Java";',
    'str name = "Java";',
    'B'
  ),
  (
    'QZ-001',
    'Toán tử nào dùng để so sánh bằng trong Java?',
    '=',
    '==',
    '===',
    'equals',
    'B'
  );

-- Questions cho Quiz 2 (Java - Vòng lặp)
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
    'QZ-002',
    'Vòng lặp nào luôn thực hiện ít nhất 1 lần?',
    'for',
    'while',
    'do-while',
    'foreach',
    'C'
  ),
  (
    'QZ-002',
    'Từ khóa nào dùng để thoát khỏi vòng lặp?',
    'exit',
    'break',
    'return',
    'stop',
    'B'
  ),
  (
    'QZ-002',
    'Từ khóa nào dùng để bỏ qua lần lặp hiện tại?',
    'skip',
    'pass',
    'continue',
    'next',
    'C'
  ),
  (
    'QZ-002',
    'switch-case có thể sử dụng với kiểu dữ liệu nào?',
    'Chỉ int',
    'int, char, String',
    'Mọi kiểu dữ liệu',
    'Chỉ String',
    'B'
  ),
  (
    'QZ-002',
    'Kết quả của: for(int i=0; i<5; i++) count++; với count=0 ban đầu?',
    '4',
    '5',
    '6',
    'Lỗi',
    'B'
  );

-- Questions cho Quiz 3 (Java - OOP)
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
    'QZ-003',
    'Tính chất nào của OOP giúp ẩn chi tiết implementation?',
    'Inheritance',
    'Polymorphism',
    'Encapsulation',
    'Abstraction',
    'C'
  ),
  (
    'QZ-003',
    'Từ khóa nào dùng để kế thừa class trong Java?',
    'implements',
    'extends',
    'inherits',
    'super',
    'B'
  ),
  (
    'QZ-003',
    'Một class có thể extends bao nhiêu class khác?',
    '0',
    '1',
    '2',
    'Không giới hạn',
    'B'
  ),
  (
    'QZ-003',
    'Access modifier nào cho phép truy cập từ mọi nơi?',
    'private',
    'default',
    'protected',
    'public',
    'D'
  ),
  (
    'QZ-003',
    'Constructor có thể có return type không?',
    'Có, phải là void',
    'Có, phải là tên class',
    'Không',
    'Tùy trường hợp',
    'C'
  );

-- Questions cho Quiz 5 (React Fundamentals)
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
    'QZ-005',
    'JSX là viết tắt của gì?',
    'JavaScript XML',
    'Java Syntax Extension',
    'JavaScript Extension',
    'JSON XML',
    'A'
  ),
  (
    'QZ-005',
    'Props trong React là gì?',
    'Biến cục bộ',
    'Dữ liệu truyền từ parent sang child',
    'State của component',
    'Event handler',
    'B'
  ),
  (
    'QZ-005',
    'Functional component return gì?',
    'Object',
    'String',
    'JSX',
    'Array',
    'C'
  ),
  (
    'QZ-005',
    'Cách nào đúng để render list trong React?',
    'for loop',
    'while loop',
    'map()',
    'forEach()',
    'C'
  ),
  (
    'QZ-005',
    'Key prop dùng để làm gì?',
    'Style element',
    'Identify elements in list',
    'Pass data',
    'Handle events',
    'B'
  );

-- ==============================================================
-- 7. QUIZ ATTEMPTS - Lịch sử làm bài
-- ==============================================================
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
  -- Student 1 làm Quiz 1, 2, 3
  (
    'USR-STU-001',
    'QZ-001',
    8.0,
    TRUE,
    TRUE,
    '2026-02-01 09:00:00',
    '2026-02-01 09:25:00'
  ),
  (
    'USR-STU-001',
    'QZ-002',
    6.0,
    TRUE,
    FALSE,
    '2026-02-05 10:00:00',
    '2026-02-05 10:28:00'
  ),
  (
    'USR-STU-001',
    'QZ-002',
    8.0,
    TRUE,
    TRUE,
    '2026-02-06 14:00:00',
    '2026-02-06 14:22:00'
  ),
  (
    'USR-STU-001',
    'QZ-003',
    9.0,
    TRUE,
    TRUE,
    '2026-02-15 08:30:00',
    '2026-02-15 09:10:00'
  ),
  -- Student 2 làm Quiz 1, 2, 3, 4 (đã pass all)
  (
    'USR-STU-002',
    'QZ-001',
    10.0,
    TRUE,
    TRUE,
    '2026-02-01 09:00:00',
    '2026-02-01 09:20:00'
  ),
  (
    'USR-STU-002',
    'QZ-002',
    9.0,
    TRUE,
    TRUE,
    '2026-02-05 10:00:00',
    '2026-02-05 10:25:00'
  ),
  (
    'USR-STU-002',
    'QZ-003',
    8.0,
    TRUE,
    TRUE,
    '2026-02-15 08:30:00',
    '2026-02-15 09:05:00'
  ),
  -- Student 3 làm Quiz 1, 2
  (
    'USR-STU-003',
    'QZ-001',
    7.0,
    TRUE,
    TRUE,
    '2026-02-02 11:00:00',
    '2026-02-02 11:28:00'
  ),
  (
    'USR-STU-003',
    'QZ-002',
    4.0,
    FALSE,
    FALSE,
    '2026-02-06 09:00:00',
    '2026-02-06 09:30:00'
  ),
  (
    'USR-STU-003',
    'QZ-002',
    6.0,
    TRUE,
    TRUE,
    '2026-02-07 15:00:00',
    '2026-02-07 15:25:00'
  ),
  -- Student 1 làm React Quiz
  (
    'USR-STU-001',
    'QZ-005',
    8.0,
    TRUE,
    TRUE,
    '2026-02-20 10:00:00',
    '2026-02-20 10:25:00'
  ),
  (
    'USR-STU-001',
    'QZ-006',
    7.0,
    TRUE,
    TRUE,
    '2026-02-25 14:00:00',
    '2026-02-25 14:28:00'
  ),
  -- Student 3 làm React Quiz (passed all)
  (
    'USR-STU-003',
    'QZ-005',
    9.0,
    TRUE,
    TRUE,
    '2026-02-21 09:00:00',
    '2026-02-21 09:22:00'
  ),
  (
    'USR-STU-003',
    'QZ-006',
    8.0,
    TRUE,
    TRUE,
    '2026-02-26 11:00:00',
    '2026-02-26 11:25:00'
  ),
  (
    'USR-STU-003',
    'QZ-007',
    8.5,
    TRUE,
    TRUE,
    '2026-03-01 10:00:00',
    '2026-03-01 10:40:00'
  );

-- ==============================================================
-- 8. CERTIFICATES - Chứng chỉ
-- ==============================================================
INSERT INTO
  certificates (
    certificate_id,
    student_id,
    class_id,
    issue_date,
    certificate_hash,
    transaction_hash,
    block_number,
    contract_address,
    status
  )
VALUES
  -- Student 2 hoàn thành Java
  (
    'CERT-001',
    'USR-STU-002',
    'CLS-001',
    '2026-03-01',
    '0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef',
    '0xabc123def456789abc123def456789abc123def456789abc123def456789abc1',
    12345678,
    '0x742d35Cc6634C0532925a3b844Bc9e7595f8Ab34',
    'ISSUED'
  ),
  -- Student 3 hoàn thành React
  (
    'CERT-002',
    'USR-STU-003',
    'CLS-003',
    '2026-03-05',
    '0xfedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321',
    '0x789def123abc456789def123abc456789def123abc456789def123abc456789d',
    12345890,
    '0x742d35Cc6634C0532925a3b844Bc9e7595f8Ab34',
    'ISSUED'
  ),
  -- Student 2 hoàn thành Python (đã revoke)
  (
    'CERT-003',
    'USR-STU-002',
    'CLS-004',
    '2026-01-20',
    '0x5555666677778888999900001111222233334444555566667777888899990000',
    '0x111222333444555666777888999000111222333444555666777888999000111a',
    12340000,
    '0x742d35Cc6634C0532925a3b844Bc9e7595f8Ab34',
    'ISSUED'
  ),
  -- Student 4 hoàn thành Python
  (
    'CERT-004',
    'USR-STU-004',
    'CLS-004',
    '2026-01-25',
    '0xaaabbbcccdddeeefffaaabbbcccdddeeefffaaabbbcccdddeeefffaaabbbccc0',
    '0x222333444555666777888999000111222333444555666777888999000111222b',
    12341111,
    '0x742d35Cc6634C0532925a3b844Bc9e7595f8Ab34',
    'ISSUED'
  );

-- ==============================================================
-- 9. ORGANIZATION INFO
-- ==============================================================
INSERT INTO
  organization_info (
    id,
    org_name,
    org_code,
    logo_url,
    website_url,
    contact_email,
    wallet_address
  )
VALUES
  (
    1,
    'FPT Polytechnic',
    'FPOLY',
    'https://fpoly.vn/logo.png',
    'https://fpoly.vn',
    'contact@fpoly.vn',
    '0x742d35Cc6634C0532925a3b844Bc9e7595f8Ab34'
  );