package main.backend.LMSQuizService.repository;

import main.backend.LMSQuizService.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, String> {

    // Thêm dòng này vào để JPA tự sinh SQL lọc theo StudentId và ClassId
    List<QuizAttempt> findByStudent_UserIdAndQuiz_ClassEntity_ClassId(String studentId, String classId);

    List<QuizAttempt> findByStudent_UserId(String studentId);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.student.userId = :studentId AND qa.quiz.quizId = :quizId ORDER BY qa.score DESC LIMIT 1")
    Optional<QuizAttempt> findTopByStudentIdAndQuizIdOrderByScoreDesc(
            @Param("studentId") String studentId,
            @Param("quizId") String quizId
    );
}