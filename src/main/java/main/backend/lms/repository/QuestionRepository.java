package main.backend.lms.repository;

import main.backend.lms.model.Question;
import main.backend.lms.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    // Lấy tất cả câu hỏi thuộc về một bài Quiz cụ thể
    List<Question> findByQuiz(Quiz quiz);

    // Đếm xem bài Quiz đó có bao nhiêu câu hỏi
    long countByQuiz(Quiz quiz);
}