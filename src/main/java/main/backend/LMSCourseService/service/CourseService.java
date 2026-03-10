package main.backend.LMSCourseService.service;

import lombok.RequiredArgsConstructor;
import main.backend.LMSCourseService.dto.response.CourseDetailResponse;
import main.backend.LMSLearningService.model.Enrollment;
import main.backend.LMSQuizService.model.Quiz;
import main.backend.LMSQuizService.model.QuizAttempt;
import main.backend.LMSCourseService.repository.CourseRepository;
import main.backend.LMSLearningService.repository.EnrollmentRepository;
import main.backend.LMSQuizService.repository.QuizAttemptRepository;
import main.backend.LMSQuizService.repository.QuizRepository;
import main.backend.LMSCertificateBlockchainService.repository.CertificateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CertificateRepository certificateRepository;

    public CourseDetailResponse getCourseDetail(String courseId, String studentId) {

        Enrollment enrollment = enrollmentRepository.findByStudent_UserId(studentId).stream()
                .filter(e -> e.getClassEntity().getCourse() != null && e.getClassEntity().getCourse().getCourseId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bạn chưa tham gia khóa học này"));

        var clazz = enrollment.getClassEntity();
        var course = clazz.getCourse();
        var teacher = clazz.getTeacher();
        var student = enrollment.getStudent();

        List<Quiz> allQuizzes = quizRepository.findAll().stream()
                .filter(q -> q.getClassEntity().getClassId().equals(clazz.getClassId()))
                .collect(Collectors.toList());

        List<CourseDetailResponse.QuizDTO> quizDTOs = allQuizzes.stream().map(quiz -> {
            var attempt = quizAttemptRepository.findByStudent_UserId(studentId).stream()
                    .filter(a -> a.getQuiz().getQuizId().equals(quiz.getQuizId()))
                    .max((a1, a2) -> Double.compare(a1.getScore(), a2.getScore()))
                    .orElse(null);

            String status = determineQuizStatus(attempt, quiz);

            return CourseDetailResponse.QuizDTO.builder()
                    .id(quiz.getQuizId())
                    .name(quiz.getTitle())
                    .score(attempt != null ? attempt.getScore() : null)
                    .maxScore(10.0) // Cố định 10 điểm vì DB không lưu maxScore
                    .status(status)
                    .build();
        }).collect(Collectors.toList());

        int totalQuizzes = quizDTOs.size();
        int completedQuizzes = (int) quizDTOs.stream().filter(q -> "completed".equals(q.getStatus())).count();
        int progress = (totalQuizzes > 0) ? (completedQuizzes * 100 / totalQuizzes) : 0;

        boolean isCompleted = "PASSED".equalsIgnoreCase(enrollment.getStatus().name());

        var response = CourseDetailResponse.builder()
                .courseName(course.getCourseName())
                .courseCode(clazz.getClassCode())
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .startDate(clazz.getStartDate() != null ? clazz.getStartDate().toString() : null)
                .endDate(clazz.getEndDate() != null ? clazz.getEndDate().toString() : null)
                .progress(progress)
                .totalQuizzes(totalQuizzes)
                .completedQuizzes(completedQuizzes)
                .isCompleted(isCompleted)
                .studentName(student.getFullName())
                .averageScore(enrollment.getFinalGrade())
                .quizzes(quizDTOs)
                .build();

        // Lấy chứng chỉ trực tiếp từ bảng Certificate
        if (isCompleted) {
            certificateRepository.findByStudent_UserId(studentId).stream()
                    .filter(c -> c.getClassEntity().getClassId().equals(clazz.getClassId()))
                    .findFirst()
                    .ifPresent(cert -> {
                        response.setCertificate(CourseDetailResponse.CertificateDTO.builder()
                                .verificationHash(cert.getCertificateHash())
                                .blockchainInfo(java.util.Map.of(
                                        "hash", cert.getCertificateHash(),
                                        "block", cert.getBlockNumber() != null ? cert.getBlockNumber().toString() : "",
                                        "txHash", cert.getTransactionHash() != null ? cert.getTransactionHash() : "",
                                        "contract", cert.getContractAddress() != null ? cert.getContractAddress() : ""
                                ))
                                .build());
                    });
        }

        return response;
    }

    private String determineQuizStatus(QuizAttempt attempt, Quiz quiz) {
        if (attempt != null) return "completed";
        return "pending";
    }
}