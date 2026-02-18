package main.backend.repository;

import main.backend.constant.QuizStatus;
import main.backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, String> {
    // Lấy danh sách Quiz của một lớp mà chưa bị đóng (Xóa mềm)
    List<Quiz> findByClazz_ClassIdAndStatusNot(String classId, QuizStatus status);
}
