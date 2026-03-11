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

    List<QuizAttempt> findByStudent_UserIdAndQuiz_ClassEntity_ClassId(String studentId, String classId);
    List<QuizAttempt> findByQuiz_QuizId(String quizId);
    List<QuizAttempt> findByStudent_UserId(String studentId);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.student.userId = :studentId AND qa.quiz.quizId = :quizId ORDER BY qa.score DESC LIMIT 1")
    Optional<QuizAttempt> findTopByStudentIdAndQuizIdOrderByScoreDesc(
            @Param("studentId") String studentId,
            @Param("quizId") String quizId
    );
    @org.springframework.data.jpa.repository.Query("SELECT qa FROM QuizAttempt qa WHERE qa.student.userId = :studentId AND qa.quiz.classEntity.classId = :classId")
    java.util.List<QuizAttempt> findByStudentAndClass(
            @org.springframework.data.repository.query.Param("studentId") String studentId,
            @org.springframework.data.repository.query.Param("classId") String classId);
}