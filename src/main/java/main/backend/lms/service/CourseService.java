package main.backend.lms.service;

import lombok.RequiredArgsConstructor;
import main.backend.lms.dto.response.CourseDetailResponse;
import main.backend.lms.model.*;
import main.backend.lms.model.CourseEntity;
import main.backend.lms.repository.CourseRepository;
import main.backend.lms.repository.EnrollmentRepository;
import main.backend.lms.repository.QuizAttemptRepository;
import main.backend.lms.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public CourseDetailResponse getCourseDetail(String courseId, String studentId) {
        // 1. Lấy Enrollment (Nếu không có trả về lỗi luôn)
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa tham gia khóa học này"));

        var clazz = enrollment.getClassEntity();
        var course = clazz.getCourse();
        var teacher = clazz.getTeacher();
        var student = enrollment.getStudent();

        // 2. Lấy danh sách Quiz từ DB
        List<Quiz> allQuizzes = quizRepository.findByClassEntity(clazz);

        List<CourseDetailResponse.QuizDTO> quizDTOs = allQuizzes.stream().map(quiz -> {
            var attempt = quizAttemptRepository.findTopByStudentIdAndQuizIdOrderByScoreDesc(studentId, quiz.getQuizId())
                    .orElse(null);

            // Logic status lấy từ DB hoặc trạng thái bài làm
            String status = determineQuizStatus(attempt, quiz);

            return CourseDetailResponse.QuizDTO.builder()
                    .id(quiz.getQuizId())
                    .name(quiz.getTitle())
                    .score(attempt != null ? attempt.getScore() : null)
                    .maxScore(quiz.getMaxScore()) // Lấy từ cột max_score trong DB
                    .status(status)
                    .build();
        }).collect(Collectors.toList());

        // 3. Tính toán dựa trên dữ liệu thật
        int totalQuizzes = quizDTOs.size();
        int completedQuizzes = (int) quizDTOs.stream().filter(q -> "completed".equals(q.getStatus())).count();
        int progress = (totalQuizzes > 0) ? (completedQuizzes * 100 / totalQuizzes) : 0;

        // Trạng thái hoàn thành lấy từ cột status của enrollment trong DB
        boolean isCompleted = "PASSED".equalsIgnoreCase(enrollment.getStatus().name());

        // 4. Build Response (Tất cả lấy từ Getter)
        var response = CourseDetailResponse.builder()
                .courseIcon(course.getCourseIcon())
                .courseName(course.getCourseName())
                .courseCode(clazz.getClassCode())
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .startDate(clazz.getStartDate()) // Lấy từ DB (kiểu String hoặc Date)
                .endDate(clazz.getEndDate())     // Lấy từ DB
                .progress(progress)
                .totalQuizzes(totalQuizzes)
                .completedQuizzes(completedQuizzes)
                .isCompleted(isCompleted)
                .studentName(student.getFullName())
                .averageScore(enrollment.getFinalGrade())
                .quizzes(quizDTOs)
                .build();

        // 5. Thêm Certificate lấy từ các cột blockchain trong bảng enrollment
        if (isCompleted && enrollment.getCertHash() != null) {
            response.setCertificate(CourseDetailResponse.CertificateDTO.builder()
                    .verificationHash(enrollment.getCertHash())
                    .blockchainInfo(java.util.Map.of(
                            "hash", enrollment.getCertHash(),
                            "block", enrollment.getCertBlock(),    // Cột mới trong DB
                            "txHash", enrollment.getCertTxHash(), // Cột mới trong DB
                            "contract", enrollment.getCertContract() // Cột mới trong DB
                    ))
                    .build());
        }

        return response;
    }

    // Hàm phụ để xử lý logic status, không viết cứng trong stream
    private String determineQuizStatus(QuizAttempt attempt, Quiz quiz) {
        if (attempt != null) return "completed";
        // Có thể thêm logic: nếu quiz.getIsLocked() return "locked" else "pending"
        return "pending";
    }
}