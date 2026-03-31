package main.backend.quiz.repository;

import java.util.List;
import main.backend.common.enums.QuizStatus;
import main.backend.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, String> {
  List<Quiz> findByClassEntity_ClassId(String classId);

  List<Quiz> findByClassEntity_ClassIdAndStatusNot(
    String classId,
    QuizStatus status
  );
}
