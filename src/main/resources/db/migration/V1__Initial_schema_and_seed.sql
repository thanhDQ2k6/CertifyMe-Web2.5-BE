-- ==============================================================
-- LMS DATABASE - COMPLETE SCHEMA + SEED DATA
-- ==============================================================
-- Version: 1.0
-- Gộp tất cả: Schema + Roles + Demo Data
-- ==============================================================
-- ==============================================================
-- 1. ROLES
-- ==============================================================
CREATE TABLE
    roles (
        role_id INT AUTO_INCREMENT PRIMARY KEY,
        role_name VARCHAR(50) NOT NULL UNIQUE
    );

INSERT INTO
    roles (role_id, role_name)
VALUES
    (1, 'STUDENT'),
    (2, 'TEACHER'),
    (3, 'ADMIN');

-- ==============================================================
-- 2. USERS
-- ==============================================================
CREATE TABLE
    users (
        user_id VARCHAR(255) PRIMARY KEY,
        email VARCHAR(255) NOT NULL UNIQUE,
        google_subject_id VARCHAR(255) UNIQUE,
        full_name NVARCHAR (100) NOT NULL,
        avatar_url TEXT,
        role_id INT NOT NULL,
        user_code VARCHAR(20) UNIQUE,
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NULL,
        CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (role_id)
    );

CREATE INDEX idx_users_email ON users (email);

CREATE INDEX idx_users_code ON users (user_code);

-- ==============================================================
-- 3. COURSES
-- ==============================================================
CREATE TABLE
    courses (
        course_id VARCHAR(255) PRIMARY KEY,
        course_code VARCHAR(50) NOT NULL,
        course_name NVARCHAR (200) NOT NULL,
        description TEXT,
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_courses_code ON courses (course_code);

-- ==============================================================
-- 4. CLASSES
-- ==============================================================
CREATE TABLE
    classes (
        class_id VARCHAR(255) PRIMARY KEY,
        course_id VARCHAR(255) NOT NULL,
        class_code VARCHAR(50) NOT NULL,
        teacher_id VARCHAR(255),
        start_date DATE,
        end_date DATE,
        total_quizzes INT DEFAULT 0,
        status ENUM('ACTIVE', 'COMPLETED', 'CANCELED') DEFAULT 'ACTIVE',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_classes_course FOREIGN KEY (course_id) REFERENCES courses (course_id),
        CONSTRAINT fk_classes_teacher FOREIGN KEY (teacher_id) REFERENCES users (user_id)
    );

CREATE INDEX idx_classes_course ON classes (course_id);

CREATE INDEX idx_classes_teacher ON classes (teacher_id);

-- ==============================================================
-- 5. ENROLLMENTS
-- ==============================================================
CREATE TABLE
    enrollments (
        enrollment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        student_id VARCHAR(255) NOT NULL,
        class_id VARCHAR(255) NOT NULL,
        passed_quizzes INT DEFAULT 0,
        final_grade DOUBLE,
        joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        status ENUM('LEARNING', 'PASSED', 'FAILED', 'DROPPED') DEFAULT 'LEARNING',
        UNIQUE (student_id, class_id),
        CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES users (user_id),
        CONSTRAINT fk_enroll_class FOREIGN KEY (class_id) REFERENCES classes (class_id)
    );

CREATE INDEX idx_enrollments_student ON enrollments (student_id);

CREATE INDEX idx_enrollments_class ON enrollments (class_id);

-- ==============================================================
-- 6. QUIZZES
-- ==============================================================
CREATE TABLE
    quizzes (
        quiz_id VARCHAR(255) PRIMARY KEY,
        class_id VARCHAR(255) NOT NULL,
        title NVARCHAR (200) NOT NULL,
        description TEXT,
        duration_minutes INT DEFAULT 60,
        passing_score DOUBLE DEFAULT 5.0,
        max_attempts INT DEFAULT NULL,
        status ENUM('DRAFT', 'PUBLISHED', 'CLOSED') DEFAULT 'DRAFT',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_quiz_class FOREIGN KEY (class_id) REFERENCES classes (class_id)
    );

CREATE INDEX idx_quizzes_class ON quizzes (class_id);

-- ==============================================================
-- 7. QUIZ QUESTIONS
-- ==============================================================
CREATE TABLE
    quiz_questions (
        question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        quiz_id VARCHAR(255) NOT NULL,
        question_text TEXT NOT NULL,
        option_a TEXT NOT NULL,
        option_b TEXT NOT NULL,
        option_c TEXT NOT NULL,
        option_d TEXT NOT NULL,
        correct_answer ENUM('A', 'B', 'C', 'D') NOT NULL,
        CONSTRAINT fk_question_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (quiz_id) ON DELETE CASCADE
    );

-- ==============================================================
-- 8. QUIZ ATTEMPTS
-- ==============================================================
CREATE TABLE
    quiz_attempts (
        attempt_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        student_id VARCHAR(255) NOT NULL,
        quiz_id VARCHAR(255) NOT NULL,
        score DOUBLE NOT NULL,
        is_passed BOOLEAN DEFAULT FALSE,
        is_best_attempt BOOLEAN DEFAULT FALSE,
        started_at DATETIME,
        submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_attempt_student FOREIGN KEY (student_id) REFERENCES users (user_id),
        CONSTRAINT fk_attempt_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (quiz_id)
    );

CREATE INDEX idx_attempts_student_quiz ON quiz_attempts (student_id, quiz_id);

CREATE INDEX idx_attempts_best ON quiz_attempts (student_id, quiz_id, is_best_attempt);

-- ==============================================================
-- 9. CERTIFICATES
-- ==============================================================
CREATE TABLE
    certificates (
        certificate_id VARCHAR(255) PRIMARY KEY,
        student_id VARCHAR(255) NOT NULL,
        class_id VARCHAR(255) NOT NULL,
        issue_date DATE DEFAULT(CURRENT_DATE),
        expiration_date DATE,
        certificate_hash VARCHAR(66) NOT NULL,
        transaction_hash VARCHAR(66),
        block_number BIGINT,
        contract_address VARCHAR(255),
        status ENUM('PENDING', 'ISSUED', 'REVOKED') DEFAULT 'PENDING',
        revoked_at TIMESTAMP NULL,
        revoked_by VARCHAR(255) NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_cert_student FOREIGN KEY (student_id) REFERENCES users (user_id),
        CONSTRAINT fk_cert_class FOREIGN KEY (class_id) REFERENCES classes (class_id),
        CONSTRAINT fk_cert_revoker FOREIGN KEY (revoked_by) REFERENCES users (user_id)
    );

CREATE INDEX idx_certificates_hash ON certificates (certificate_hash);

-- ==============================================================
-- 10. ORGANIZATION INFO
-- ==============================================================
CREATE TABLE
    organization_info (
        id INT PRIMARY KEY DEFAULT 1,
        org_name NVARCHAR (200) NOT NULL,
        org_code VARCHAR(50),
        logo_url TEXT,
        website_url VARCHAR(255),
        contact_email VARCHAR(255),
        wallet_address VARCHAR(255),
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- ==============================================================
-- SEED DATA: FAKE USERS (Demo only)
-- ==============================================================
INSERT INTO
    users (
        user_id,
        email,
        full_name,
        avatar_url,
        role_id,
        user_code,
        is_active
    )
VALUES
    (
        'USR-ADM-001',
        'admin@demo.lms',
        'Admin Hệ Thống',
        NULL,
        3,
        'AD00001',
        TRUE
    ),
    (
        'USR-TCH-001',
        'teacher@demo.lms',
        'Nguyễn Văn Thầy',
        NULL,
        2,
        'GV00001',
        TRUE
    ),
    (
        'USR-STU-001',
        'student1@demo.lms',
        'Lê Minh Tuấn',
        NULL,
        1,
        'HS00001',
        TRUE
    ),
    (
        'USR-STU-002',
        'student2@demo.lms',
        'Phạm Thị Lan',
        NULL,
        1,
        'HS00002',
        TRUE
    );

-- ==============================================================
-- SEED DATA: COURSES
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
        'Khóa học Java từ cơ bản đến nâng cao',
        TRUE
    ),
    (
        'CRS-002',
        'REACT3',
        'Lập trình React 3',
        'React.js: Components, Hooks, Redux',
        TRUE
    ),
    (
        'CRS-003',
        'PYTHON',
        'Lập trình Python',
        'Python cơ bản và Data Science',
        TRUE
    );

-- ==============================================================
-- SEED DATA: CLASSES (total_quizzes = số quiz thực tế)
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
        3,
        'ACTIVE'
    ),
    (
        'CLS-002',
        'CRS-002',
        'FE18301',
        'USR-TCH-001',
        '2026-02-01',
        '2026-07-01',
        3,
        'ACTIVE'
    );

-- ==============================================================
-- SEED DATA: QUIZZES (3 quiz mỗi lớp)
-- ==============================================================
-- Java class quizzes
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
        'Quiz 1: Biến và Kiểu dữ liệu',
        'Kiến thức cơ bản về biến',
        30,
        5.0,
        'PUBLISHED'
    ),
    (
        'QZ-002',
        'CLS-001',
        'Quiz 2: Vòng lặp',
        'for, while, do-while',
        30,
        5.0,
        'PUBLISHED'
    ),
    (
        'QZ-003',
        'CLS-001',
        'Quiz 3: OOP',
        'Class, Object, Inheritance',
        45,
        5.0,
        'PUBLISHED'
    );

-- React class quizzes
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
        'QZ-004',
        'CLS-002',
        'Quiz 1: React Basics',
        'JSX, Components, Props',
        30,
        5.0,
        'PUBLISHED'
    ),
    (
        'QZ-005',
        'CLS-002',
        'Quiz 2: React Hooks',
        'useState, useEffect',
        30,
        5.0,
        'PUBLISHED'
    ),
    (
        'QZ-006',
        'CLS-002',
        'Quiz 3: State Management',
        'Redux, Context',
        45,
        5.0,
        'PUBLISHED'
    );

-- ==============================================================
-- SEED DATA: QUIZ QUESTIONS (5 câu mỗi quiz)
-- ==============================================================
-- QZ-001: Java Basics
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
        'Kiểu dữ liệu nào KHÔNG phải primitive?',
        'int',
        'boolean',
        'String',
        'char',
        'C'
    ),
    (
        'QZ-001',
        'Giá trị mặc định của boolean?',
        'true',
        'false',
        'null',
        '0',
        'B'
    ),
    (
        'QZ-001',
        'Kích thước của int?',
        '2 bytes',
        '4 bytes',
        '8 bytes',
        '16 bytes',
        'B'
    ),
    (
        'QZ-001',
        'Khai báo String đúng?',
        'string s = "a"',
        'String s = "a"',
        'STRING s = "a"',
        'str s = "a"',
        'B'
    ),
    (
        'QZ-001',
        'Toán tử so sánh bằng?',
        '=',
        '==',
        '===',
        'eq',
        'B'
    );

-- QZ-002: Java Loops
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
        'Vòng lặp nào luôn chạy ít nhất 1 lần?',
        'for',
        'while',
        'do-while',
        'foreach',
        'C'
    ),
    (
        'QZ-002',
        'Từ khóa thoát vòng lặp?',
        'exit',
        'break',
        'return',
        'stop',
        'B'
    ),
    (
        'QZ-002',
        'Từ khóa bỏ qua lần lặp hiện tại?',
        'skip',
        'pass',
        'continue',
        'next',
        'C'
    ),
    (
        'QZ-002',
        'switch-case dùng với kiểu nào?',
        'Chỉ int',
        'int, char, String',
        'Mọi kiểu',
        'Chỉ String',
        'B'
    ),
    (
        'QZ-002',
        'for(int i=0; i<5; i++) count++; count=?',
        '4',
        '5',
        '6',
        '0',
        'B'
    );

-- QZ-003: Java OOP
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
        'Tính chất OOP ẩn implementation?',
        'Inheritance',
        'Polymorphism',
        'Encapsulation',
        'Abstraction',
        'C'
    ),
    (
        'QZ-003',
        'Từ khóa kế thừa class?',
        'implements',
        'extends',
        'inherits',
        'super',
        'B'
    ),
    (
        'QZ-003',
        'Class có thể extends bao nhiêu class?',
        '0',
        '1',
        '2',
        'Không giới hạn',
        'B'
    ),
    (
        'QZ-003',
        'Access modifier truy cập từ mọi nơi?',
        'private',
        'default',
        'protected',
        'public',
        'D'
    ),
    (
        'QZ-003',
        'Constructor có return type không?',
        'void',
        'tên class',
        'Không',
        'Tùy',
        'C'
    );

-- QZ-004: React Basics
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
        'QZ-004',
        'JSX là viết tắt của?',
        'JavaScript XML',
        'Java Syntax',
        'JS Extension',
        'JSON XML',
        'A'
    ),
    (
        'QZ-004',
        'Props trong React là gì?',
        'Biến cục bộ',
        'Data từ parent',
        'State',
        'Event',
        'B'
    ),
    (
        'QZ-004',
        'Functional component return gì?',
        'Object',
        'String',
        'JSX',
        'Array',
        'C'
    ),
    (
        'QZ-004',
        'Render list dùng gì?',
        'for loop',
        'while',
        'map()',
        'forEach()',
        'C'
    ),
    (
        'QZ-004',
        'Key prop dùng để?',
        'Style',
        'Identify elements',
        'Pass data',
        'Events',
        'B'
    );

-- QZ-005: React Hooks
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
        'useState trả về gì?',
        'Object',
        'Array [value, setter]',
        'Function',
        'String',
        'B'
    ),
    (
        'QZ-005',
        'useEffect chạy khi nào?',
        'Chỉ mount',
        'Mỗi render',
        'Tùy dependencies',
        'Chỉ unmount',
        'C'
    ),
    (
        'QZ-005',
        'Dependency array rỗng [] nghĩa là?',
        'Chạy mỗi render',
        'Chỉ mount',
        'Không chạy',
        'Error',
        'B'
    ),
    (
        'QZ-005',
        'Hook nào để context?',
        'useState',
        'useEffect',
        'useContext',
        'useReducer',
        'C'
    ),
    (
        'QZ-005',
        'useRef dùng để?',
        'State',
        'DOM reference',
        'Side effects',
        'Context',
        'B'
    );

-- QZ-006: React State Management
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
        'QZ-006',
        'Redux store chứa gì?',
        'Components',
        'Application state',
        'Routes',
        'Styles',
        'B'
    ),
    (
        'QZ-006',
        'Action trong Redux là?',
        'Function',
        'Object với type',
        'Component',
        'Hook',
        'B'
    ),
    (
        'QZ-006',
        'Reducer là?',
        'Pure function',
        'Component',
        'Hook',
        'Middleware',
        'A'
    ),
    (
        'QZ-006',
        'Context API dùng khi?',
        'Luôn dùng',
        'Prop drilling',
        'Chỉ forms',
        'Chỉ API',
        'B'
    ),
    (
        'QZ-006',
        'useSelector dùng để?',
        'Dispatch action',
        'Select state',
        'Create store',
        'Combine reducers',
        'B'
    );

-- ==============================================================
-- SEED DATA: ENROLLMENTS
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
    ('USR-STU-001', 'CLS-001', 2, 7.5, 'LEARNING'),
    ('USR-STU-002', 'CLS-001', 3, 8.5, 'PASSED'),
    ('USR-STU-001', 'CLS-002', 1, 6.0, 'LEARNING'),
    ('USR-STU-002', 'CLS-002', 2, 7.0, 'LEARNING');

-- ==============================================================
-- SEED DATA: QUIZ ATTEMPTS
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
        7.0,
        TRUE,
        TRUE,
        '2026-02-10 10:00:00',
        '2026-02-10 10:28:00'
    ),
    (
        'USR-STU-002',
        'QZ-001',
        9.0,
        TRUE,
        TRUE,
        '2026-02-01 09:00:00',
        '2026-02-01 09:20:00'
    ),
    (
        'USR-STU-002',
        'QZ-002',
        8.0,
        TRUE,
        TRUE,
        '2026-02-10 10:00:00',
        '2026-02-10 10:22:00'
    ),
    (
        'USR-STU-002',
        'QZ-003',
        8.5,
        TRUE,
        TRUE,
        '2026-02-20 14:00:00',
        '2026-02-20 14:40:00'
    ),
    (
        'USR-STU-001',
        'QZ-004',
        6.0,
        TRUE,
        TRUE,
        '2026-03-01 10:00:00',
        '2026-03-01 10:28:00'
    ),
    (
        'USR-STU-002',
        'QZ-004',
        7.0,
        TRUE,
        TRUE,
        '2026-03-01 10:00:00',
        '2026-03-01 10:25:00'
    ),
    (
        'USR-STU-002',
        'QZ-005',
        7.0,
        TRUE,
        TRUE,
        '2026-03-10 11:00:00',
        '2026-03-10 11:28:00'
    );

-- ==============================================================
-- SEED DATA: CERTIFICATE (1 sample)
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
    (
        'CERT-001',
        'USR-STU-002',
        'CLS-001',
        '2026-03-15',
        '0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef',
        '0xabc123def456789abc123def456789abc123def456789abc123def456789abc1',
        12345678,
        '0x742d35Cc6634C0532925a3b844Bc9e7595f8Ab34',
        'ISSUED'
    );

-- ==============================================================
-- SEED DATA: ORGANIZATION
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