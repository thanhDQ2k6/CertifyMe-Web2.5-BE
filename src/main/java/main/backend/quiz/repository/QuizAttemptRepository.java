package main.backend.quiz.repository;

import java.util.List;
import main.backend.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// FIX QS2: generic type Long (khớp với QuizAttempt.attemptId)
public interface QuizAttemptRepository
  extends JpaRepository<QuizAttempt, Long>
{
  List<QuizAttempt> findByQuiz_QuizId(String quizId);

  List<QuizAttempt> findByStudent_UserId(String studentId);

  @Query(
    "SELECT qa FROM QuizAttempt qa WHERE qa.student.userId = :studentId AND qa.quiz.classEntity.classId = :classId"
  )
  List<QuizAttempt> findByStudentAndClass(
    @Param("studentId") String studentId,
    @Param("classId") String classId
  );

  @Query(
    "SELECT qa FROM QuizAttempt qa WHERE qa.student.userId = :studentId AND qa.quiz.quizId = :quizId ORDER BY qa.score DESC"
  )
  List<QuizAttempt> findByStudentAndQuizOrderByScoreDesc(
    @Param("studentId") String studentId,
    @Param("quizId") String quizId
  );
}
