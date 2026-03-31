-- ==============================================================
-- V3: TRIGGER TỰ ĐỘNG CẬP NHẬT total_quizzes
-- ==============================================================
-- Mỗi lần INSERT/DELETE/UPDATE quiz → tự động update classes.total_quizzes
-- ==============================================================
-- Trigger: Khi thêm quiz mới
DELIMITER / /
CREATE TRIGGER trg_quiz_insert AFTER
INSERT
    ON quizzes FOR EACH ROW BEGIN
UPDATE classes
SET
    total_quizzes = (
        SELECT
            COUNT(*)
        FROM
            quizzes
        WHERE
            class_id = NEW.class_id
            AND status != 'CLOSED'
    )
WHERE
    class_id = NEW.class_id;

END / / DELIMITER;

-- Trigger: Khi xóa quiz
DELIMITER / /
CREATE TRIGGER trg_quiz_delete AFTER DELETE ON quizzes FOR EACH ROW BEGIN
UPDATE classes
SET
    total_quizzes = (
        SELECT
            COUNT(*)
        FROM
            quizzes
        WHERE
            class_id = OLD.class_id
            AND status != 'CLOSED'
    )
WHERE
    class_id = OLD.class_id;

END / / DELIMITER;

-- Trigger: Khi update quiz (đổi class hoặc status)
DELIMITER / /
CREATE TRIGGER trg_quiz_update AFTER
UPDATE ON quizzes FOR EACH ROW BEGIN
-- Update old class (nếu đổi class)
IF OLD.class_id != NEW.class_id THEN
UPDATE classes
SET
    total_quizzes = (
        SELECT
            COUNT(*)
        FROM
            quizzes
        WHERE
            class_id = OLD.class_id
            AND status != 'CLOSED'
    )
WHERE
    class_id = OLD.class_id;

END IF;

-- Update new/current class
UPDATE classes
SET
    total_quizzes = (
        SELECT
            COUNT(*)
        FROM
            quizzes
        WHERE
            class_id = NEW.class_id
            AND status != 'CLOSED'
    )
WHERE
    class_id = NEW.class_id;

END / / DELIMITER;

-- ==============================================================
-- Sync lại total_quizzes cho data hiện tại
-- ==============================================================
UPDATE classes c
SET
    total_quizzes = (
        SELECT
            COUNT(*)
        FROM
            quizzes q
        WHERE
            q.class_id = c.class_id
            AND q.status != 'CLOSED'
    );