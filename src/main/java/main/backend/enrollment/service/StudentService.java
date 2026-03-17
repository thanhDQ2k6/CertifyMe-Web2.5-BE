package main.backend.enrollment.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import main.backend.auth.service.UserQueryService;
import main.backend.course.dto.response.CourseResponse;
import main.backend.enrollment.entity.Enrollment;
import main.backend.enrollment.repository.EnrollmentRepository;
import main.backend.quiz.entity.QuizAttempt;
import main.backend.quiz.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final EnrollmentRepository enrollmentRepository;
  private final QuizAttemptRepository quizAttemptRepository;
  private final UserQueryService userQueryService;

  public List<CourseResponse> getStudentDashboard(String studentId) {
    userQueryService.getByIdOrThrow(studentId);

    List<Enrollment> enrollments = enrollmentRepository.findByStudent_UserId(
      studentId
    );

    // FIX LS2: load TẤT CẢ attempts 1 lần thay vì N+1 trong loop
    List<QuizAttempt> allAttempts = quizAttemptRepository.findByStudent_UserId(
      studentId
    );

    return enrollments
      .stream()
      .map(enroll -> {
        var classEntity = enroll.getClassEntity();
        var course = classEntity.getCourse();
        var teacher = classEntity.getTeacher();

        Map<String, Optional<QuizAttempt>> bestAttemptsPerQuiz = allAttempts
          .stream()
          .filter(
            a ->
              a.getQuiz() != null &&
              a.getScore() != null &&
              a
                .getQuiz()
                .getClassEntity()
                .getClassId()
                .equals(classEntity.getClassId())
          )
          .collect(
            Collectors.groupingBy(
              a -> a.getQuiz().getQuizId(),
              Collectors.maxBy(
                Comparator.comparingDouble(QuizAttempt::getScore)
              )
            )
          );

        int passedCount = (int) bestAttemptsPerQuiz
          .values()
          .stream()
          .filter(
            opt ->
              opt.isPresent() &&
              opt.get().getIsPassed() != null &&
              opt.get().getIsPassed()
          )
          .count();

        double avgScore = bestAttemptsPerQuiz
          .values()
          .stream()
          .mapToDouble(opt -> opt.map(QuizAttempt::getScore).orElse(0.0))
          .average()
          .orElse(0.0);

        int totalQuizzes =
          classEntity.getTotalQuizzes() != null
            ? classEntity.getTotalQuizzes()
            : 0;
        int progress =
          totalQuizzes > 0 ? ((passedCount * 100) / totalQuizzes) : 0;
        boolean isCompleted = totalQuizzes > 0 && passedCount >= totalQuizzes;

        return CourseResponse.builder()
          .courseId(course.getCourseId())
          .courseCode(course.getCourseCode())
          .courseName(course.getCourseName())
          .teacherName(teacher != null ? teacher.getFullName() : null)
          .progress(progress)
          .totalQuizzes(totalQuizzes)
          .completedQuizzes(passedCount)
          .averageScore(Math.round(avgScore * 10.0) / 10.0)
          .isCompleted(isCompleted)
          .build();
      })
      .toList();
  }
}
