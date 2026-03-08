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
import org.springframework.stereotype.Service;
import java.util.Comparator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class StudentService {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public List<CourseResponse> getStudentDashboard(String studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudent_UserId(studentId);

        return enrollments.stream().map(enroll -> {
            var classEntity = enroll.getClassEntity();
            var course = classEntity.getCourse();
            var teacher = classEntity.getTeacher();

            // 1. Lấy tất cả lần thi của student
            List<QuizAttempt> allAttempts = quizAttemptRepository.findByStudent_UserId(studentId);

            // 2. Lọc theo ClassId và Gom nhóm để lấy điểm CAO NHẤT cho mỗi Quiz
            // (Mục đích: Nếu thi 3 lần thì chỉ lấy lần điểm cao nhất để tính toán)
            Map<String, Optional<QuizAttempt>> bestAttemptsPerQuiz = allAttempts.stream()
                    .filter(a -> a.getQuiz() != null &&
                            a.getQuiz().getClassEntity().getClassId().equals(classEntity.getClassId()))
                    .filter(a -> a.getScore() != null)
                    .collect(Collectors.groupingBy(
                            a -> a.getQuiz().getQuizId(),
                            Collectors.maxBy(Comparator.comparingDouble(QuizAttempt::getScore))
                    ));

            // 3. Đếm số Quiz THỰC SỰ PASS (Điểm cao nhất phải >= 5.0)
            int realPassedCount = (int) bestAttemptsPerQuiz.values().stream()
                    .filter(opt -> opt.isPresent() && opt.get().getScore() >= 5.0)
                    .count();

            // 4. Tính điểm TRUNG BÌNH THỰC (Lấy trung bình của các đầu điểm cao nhất)
            double realAvgScore = bestAttemptsPerQuiz.values().stream()
                    .mapToDouble(opt -> opt.map(QuizAttempt::getScore).orElse(0.0))
                    .average()
                    .orElse(0.0);

            // 5. Lấy tổng số Quiz quy định của lớp
            int totalQuizzes = (classEntity.getTotalQuizzes() != null) ? classEntity.getTotalQuizzes() : 0;

            // 6. Tính Progress dựa trên số lượng quiz đạt điểm (>= 5đ)
            int realProgress = (totalQuizzes > 0) ? (realPassedCount * 100 / totalQuizzes) : 0;

            // 7. Logic "Đạt" khóa học: Pass sạch sành sanh tất cả các bài quiz
            boolean isAllQuizzesPassed = (totalQuizzes > 0) && (realPassedCount == totalQuizzes);

            // ĐỔ DỮ LIỆU MỚI VÀO BUILDER
            return CourseResponse.builder()
                    .courseId(course.getCourseId())
                    .courseCode(classEntity.getClassCode())
                    .courseName(course.getCourseName())
                    .courseIcon(course.getCourseIcon())
                    .teacherName(teacher != null ? teacher.getFullName() : "Chưa có giáo viên")
                    .progress(realProgress)             // TRẢ VỀ: Progress thực tế
                    .totalQuizzes(totalQuizzes)         // TRẢ VỀ: Tổng bài
                    .completedQuizzes(realPassedCount)  // TRẢ VỀ: Số bài >= 5đ
                    .averageScore(realAvgScore)         // TRẢ VỀ: Điểm TB (Không bị null)
                    .isCompleted(isAllQuizzesPassed)    // TRẢ VỀ: Chỉ true khi pass hết
                    .build();
        }).collect(Collectors.toList());
    }

    public List<CertificateResponse> getStudentCertificates(String studentId) {
        // Lấy dữ liệu từ MySQL thông qua Repository
        List<Enrollment> enrollments = enrollmentRepository.findByStudent_UserId(studentId);

        return enrollments.stream()
                .filter(e -> e.getCertHash() != null) // Lọc dữ liệu thật có mã hash
                .map(e -> CertificateResponse.builder()
                        .courseName(e.getClassEntity().getCourse().getCourseName())
                        .courseCode(e.getClassEntity().getClassCode())
                        .certHash(e.getCertHash())
                        // SỬA Ở ĐÂY: Phải gọi qua ClassEntity vì Enrollment không có field này
                        .certDate(e.getClassEntity().getEndDate())
                        .build())
                .collect(Collectors.toList());
    }
}