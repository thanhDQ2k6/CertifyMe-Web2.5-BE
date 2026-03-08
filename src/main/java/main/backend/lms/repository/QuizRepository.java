package main.backend.lms.repository;

import main.backend.lms.model.ClassEntity;
import main.backend.lms.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, String> {

    // Lấy tất cả quiz của một lớp học
    List<Quiz> findByClassEntity(ClassEntity classEntity);
}