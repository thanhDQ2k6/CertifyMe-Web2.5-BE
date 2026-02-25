package main.backend.lms.repository;

import main.backend.lms.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// QuestionRepository.java
public interface QuestionRepository extends JpaRepository<QuizQuestion, Long> {
    // Lấy bộ câu hỏi của một bài Quiz
    List<QuizQuestion> findByQuiz_QuizId(String quizId);
}
