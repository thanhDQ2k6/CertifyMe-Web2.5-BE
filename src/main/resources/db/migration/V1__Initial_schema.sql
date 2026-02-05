-- ==============================================================
-- 1. NHÓM QUẢN TRỊ NGƯỜI DÙNG (AUTH & USERS)
-- ==============================================================
CREATE TABLE
  roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
  );

CREATE TABLE
  users (
    user_id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    google_subject_id VARCHAR(255) UNIQUE,
    full_name NVARCHAR (100) NOT NULL,
    avatar_url TEXT,
    role_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (role_id)
  );

-- ==============================================================
-- 2. NHÓM ĐÀO TẠO (LMS CORE)
-- ==============================================================
CREATE TABLE
  classes (
    class_id VARCHAR(255) PRIMARY KEY,
    class_code VARCHAR(50) NOT NULL,
    class_name NVARCHAR (100),
    teacher_id VARCHAR(255),
    start_date DATE,
    end_date DATE,
    -- Cache: Tổng số quiz PUBLISHED của lớp
    total_quizzes INT DEFAULT 0,
    status ENUM('ACTIVE', 'COMPLETED', 'CANCELED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_classes_teacher FOREIGN KEY (teacher_id) REFERENCES users (user_id)
  );

CREATE TABLE
  enrollments (
    enrollment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    class_id VARCHAR(255) NOT NULL,
    -- Cache: Số quiz đã pass (update realtime khi submit)
    passed_quizzes INT DEFAULT 0,
    final_grade DOUBLE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('LEARNING', 'PASSED', 'FAILED', 'DROPPED') DEFAULT 'LEARNING',
    UNIQUE (student_id, class_id),
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES users (user_id),
    CONSTRAINT fk_enroll_class FOREIGN KEY (class_id) REFERENCES classes (class_id)
  );

-- ==============================================================
-- 3. NHÓM KIỂM TRA & ĐÁNH GIÁ (ASSESSMENT)
-- ==============================================================
CREATE TABLE
  quizzes (
    quiz_id VARCHAR(255) PRIMARY KEY,
    class_id VARCHAR(255) NOT NULL,
    title NVARCHAR (200) NOT NULL,
    description TEXT,
    duration_minutes INT DEFAULT 60,
    passing_score DOUBLE DEFAULT 5.0,
    max_attempts INT DEFAULT NULL, -- NULL = unlimited
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_class FOREIGN KEY (class_id) REFERENCES classes (class_id)
  );

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

CREATE TABLE
  quiz_attempts (
    attempt_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    quiz_id VARCHAR(255) NOT NULL,
    score DOUBLE NOT NULL,
    is_passed BOOLEAN DEFAULT FALSE,
    -- Best attempt: Lần làm có điểm cao nhất
    is_best_attempt BOOLEAN DEFAULT FALSE,
    started_at DATETIME,
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attempt_student FOREIGN KEY (student_id) REFERENCES users (user_id),
    CONSTRAINT fk_attempt_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (quiz_id)
  );

CREATE TABLE
  quiz_answers (
    answer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_answer ENUM('A', 'B', 'C', 'D'),
    is_correct BOOLEAN NOT NULL,
    UNIQUE (attempt_id, question_id),
    CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts (attempt_id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES quiz_questions (question_id)
  );

-- ==============================================================
-- 4. NHÓM CẤP BẰNG & BLOCKCHAIN (ISSUANCE)
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

-- ==============================================================
-- 5. NHÓM CẤU HÌNH HỆ THỐNG (SYSTEM)
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
-- 6. INDEX TỐI ƯU
-- ==============================================================
CREATE INDEX idx_users_email ON users (email);

CREATE INDEX idx_classes_teacher ON classes (teacher_id);

CREATE INDEX idx_enrollments_student ON enrollments (student_id);

CREATE INDEX idx_enrollments_class ON enrollments (class_id);

CREATE INDEX idx_quizzes_class ON quizzes (class_id);

CREATE INDEX idx_attempts_student_quiz ON quiz_attempts (student_id, quiz_id);

CREATE INDEX idx_attempts_best ON quiz_attempts (student_id, quiz_id, is_best_attempt);

CREATE INDEX idx_certificates_hash ON certificates (certificate_hash);