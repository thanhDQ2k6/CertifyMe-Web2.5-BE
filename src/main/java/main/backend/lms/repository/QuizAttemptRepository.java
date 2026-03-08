package main.backend.lms.repository;

import main.backend.lms.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, String> {
    List<QuizAttempt> findByStudent_UserId(String studentId);
    // Cách 1: Dùng Query thuần để chắc chắn không bị lỗi đặt tên hàm
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.student.userId = :studentId AND qa.quiz.quizId = :quizId ORDER BY qa.score DESC LIMIT 1")
    Optional<QuizAttempt> findTopByStudentIdAndQuizIdOrderByScoreDesc(
            @Param("studentId") String studentId,
            @Param("quizId") String quizId
    );

}