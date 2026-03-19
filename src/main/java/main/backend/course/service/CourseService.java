package main.backend.course.service;

import lombok.RequiredArgsConstructor;
import main.backend.certificate.repository.CertificateRepository;
import main.backend.common.enums.QuizStatus;
import main.backend.common.exception.ResourceNotFoundException;
import main.backend.course.dto.response.CourseDetailResponse;
import main.backend.course.dto.response.CourseListItemResponse;
import main.backend.course.repository.CourseRepository;
import main.backend.enrollment.entity.Enrollment;
import main.backend.enrollment.repository.EnrollmentRepository;
import main.backend.quiz.entity.Quiz;
import main.backend.quiz.entity.QuizAttempt;
import main.backend.quiz.repository.QuizAttemptRepository;
import main.backend.quiz.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CertificateRepository certificateRepository;

    public List<CourseListItemResponse> getAllPublicCourses() {
        return courseRepository.findByIsActiveTrue().stream()
                .map(course -> CourseListItemResponse.builder()
                        .courseId(course.getCourseId())
                        .courseCode(course.getCourseCode())
                        .courseName(course.getCourseName())
                        .description(course.getDescription())
                        .build())
                .toList();
    }

    public CourseDetailResponse getCourseDetail(String courseId, String studentId) {

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for course: " + courseId));

        var clazz = enrollment.getClassEntity();
        var course = clazz.getCourse();
        var teacher = clazz.getTeacher();
        var student = enrollment.getStudent();

        // FIX CS5: dùng findByClassEntity_ClassId thay vì findAll() + filter
        List<Quiz> quizzes = quizRepository.findByClassEntity_ClassIdAndStatusNot(
                clazz.getClassId(), QuizStatus.CLOSED);

        // FIX CS6: load TẤT CẢ attempts 1 lần thay vì N+1
        List<QuizAttempt> studentAttempts = quizAttemptRepository.findByStudentAndClass(
                studentId, clazz.getClassId());

        List<CourseDetailResponse.QuizDTO> quizDTOs = quizzes.stream().map(quiz -> {
            QuizAttempt bestAttempt = studentAttempts.stream()
                    .filter(a -> a.getQuiz().getQuizId().equals(quiz.getQuizId()))
                    .max((a1, a2) -> Double.compare(a1.getScore(), a2.getScore()))
                    .orElse(null);

            String status = bestAttempt != null ? "completed" : "pending";

            return CourseDetailResponse.QuizDTO.builder()
                    .id(quiz.getQuizId())
                    .name(quiz.getTitle())
                    .score(bestAttempt != null ? bestAttempt.getScore() : null)
                    .maxScore(10.0)
                    .status(status)
                    .build();
        }).toList();

        int totalQuizzes = quizDTOs.size();
        int completedQuizzes = (int) quizDTOs.stream().filter(q -> "completed".equals(q.getStatus())).count();
        int progress = totalQuizzes > 0 ? (completedQuizzes * 100 / totalQuizzes) : 0;
        boolean isCompleted = "PASSED".equalsIgnoreCase(enrollment.getStatus().name());

        // FIX CS2: courseCode từ course.courseCode thay vì classCode
        var response = CourseDetailResponse.builder()
                .courseName(course.getCourseName())
                .courseCode(course.getCourseCode())
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

        if (isCompleted) {
            certificateRepository.findByStudent_UserId(studentId).stream()
                    .filter(c -> c.getClassEntity().getClassId().equals(clazz.getClassId()))
                    .findFirst()
                    .ifPresent(cert -> response.setCertificate(CourseDetailResponse.CertificateDTO.builder()
                            .verificationHash(cert.getCertificateHash())
                            .blockchainInfo(Map.of(
                                    "hash", cert.getCertificateHash() != null ? cert.getCertificateHash() : "",
                                    "block", cert.getBlockNumber() != null ? cert.getBlockNumber().toString() : "",
                                    "txHash", cert.getTransactionHash() != null ? cert.getTransactionHash() : "",
                                    "contract", cert.getContractAddress() != null ? cert.getContractAddress() : ""
                            ))
                            .build()));
        }

        return response;
    }
}
