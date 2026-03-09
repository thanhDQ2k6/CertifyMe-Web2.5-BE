package main.backend.LMSLearningService.service;

import lombok.RequiredArgsConstructor;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.LMSCertificateBlockchainService.dto.response.CertificateResponse;
import main.backend.LMSCourseService.dto.response.CourseResponse;
import main.backend.LMSLearningService.model.Enrollment;
import main.backend.LMSQuizService.model.QuizAttempt;
import main.backend.LMSLearningService.repository.EnrollmentRepository;
import main.backend.LMSQuizService.repository.QuizAttemptRepository;
import main.backend.LMSCertificateBlockchainService.repository.CertificateRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CertificateRepository certificateRepository;

    public List<CourseResponse> getStudentDashboard(String studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudent_UserId(studentId);

        return enrollments.stream().map(enroll -> {
            var classEntity = enroll.getClassEntity();
            var course = classEntity.getCourse();
            var teacher = classEntity.getTeacher();

            List<QuizAttempt> allAttempts = quizAttemptRepository.findByStudent_UserId(studentId);

            Map<String, Optional<QuizAttempt>> bestAttemptsPerQuiz = allAttempts.stream()
                    .filter(a -> a.getQuiz() != null &&
                            a.getQuiz().getClassEntity().getClassId().equals(classEntity.getClassId()))
                    .filter(a -> a.getScore() != null)
                    .collect(Collectors.groupingBy(
                            a -> a.getQuiz().getQuizId(),
                            Collectors.maxBy(Comparator.comparingDouble(QuizAttempt::getScore))
                    ));

            int realPassedCount = (int) bestAttemptsPerQuiz.values().stream()
                    .filter(opt -> opt.isPresent() && opt.get().getScore() >= 5.0)
                    .count();

            double realAvgScore = bestAttemptsPerQuiz.values().stream()
                    .mapToDouble(opt -> opt.map(QuizAttempt::getScore).orElse(0.0))
                    .average()
                    .orElse(0.0);

            int totalQuizzes = (classEntity.getTotalQuizzes() != null) ? classEntity.getTotalQuizzes() : 0;
            int realProgress = (totalQuizzes > 0) ? (realPassedCount * 100 / totalQuizzes) : 0;
            boolean isAllQuizzesPassed = (totalQuizzes > 0) && (realPassedCount == totalQuizzes);

            return CourseResponse.builder()
                    .courseId(course.getCourseId())
                    .courseCode(classEntity.getClassCode())
                    .courseName(course.getCourseName())
                    .teacherName(teacher != null ? teacher.getFullName() : "Chưa có giáo viên")
                    .progress(realProgress)
                    .totalQuizzes(totalQuizzes)
                    .completedQuizzes(realPassedCount)
                    .averageScore(realAvgScore)
                    .isCompleted(isAllQuizzesPassed)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<CertificateResponse> getStudentCertificates(String studentId) {
        List<main.backend.LMSCertificateBlockchainService.model.Certificate> certificates = certificateRepository.findByStudent_UserId(studentId);

        return certificates.stream()
                .map(c -> CertificateResponse.builder()
                        .courseName(c.getClassEntity() != null && c.getClassEntity().getCourse() != null ? c.getClassEntity().getCourse().getCourseName() : "N/A")
                        .courseCode(c.getClassEntity() != null ? c.getClassEntity().getClassCode() : "N/A")
                        .certHash(c.getCertificateHash())
                        .certDate(c.getIssueDate() != null ? c.getIssueDate().toString() : "N/A")
                        .build())
                .collect(Collectors.toList());
    }
}