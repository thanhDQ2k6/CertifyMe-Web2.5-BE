package main.backend.LMSQuizService.service;

import lombok.RequiredArgsConstructor;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.LMSLearningService.model.Enrollment;
import main.backend.LMSQuizService.model.Question;
import main.backend.LMSQuizService.model.Quiz;
import main.backend.LMSQuizService.model.QuizAttempt;
import main.backend.LMSQuizService.dto.response.QuizResultResponse;
import main.backend.LMSLearningService.repository.EnrollmentRepository;
import main.backend.LMSQuizService.repository.QuizAttemptRepository;
import main.backend.LMSQuizService.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository; // Cần thêm cái này để update trạng thái

    @Transactional
    public QuizResultResponse submitQuiz(String quizId, String studentId, List<String> userAnswers) {
        // 1. Tìm Quiz trong DB
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz không tồn tại"));

        // 2. Lấy danh sách câu hỏi và check lỗi chia cho 0
        List<Question> questions = quiz.getQuestions();
        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("Bài thi này chưa có câu hỏi nào!");
        }

        // 3. Tính điểm
        int correctCount = 0;
        int totalQuestions = questions.size();

        for (int i = 0; i < totalQuestions; i++) {
            if (i < userAnswers.size()) {
                String correctAns = questions.get(i).getCorrectAnswer().name();
                String userAns = userAnswers.get(i);

                if (correctAns.equalsIgnoreCase(userAns)) {
                    correctCount++;
                }
            }
        }

        double maxScore = (quiz.getMaxScore() != null) ? quiz.getMaxScore() : 10.0;
        double finalScore = ((double) correctCount / totalQuestions) * maxScore;
        finalScore = Math.round(finalScore * 10.0) / 10.0;

        boolean passed = finalScore >= quiz.getPassingScore();

        // 4. Lưu vào bảng quiz_attempts
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên ID: " + studentId));

        QuizAttempt attempt = QuizAttempt.builder()
                .student(student)
                .quiz(quiz)
                .score(finalScore)
                .isPassed(passed)
                .submittedAt(LocalDateTime.now())
                .build();

        quizAttemptRepository.save(attempt);

        // 5. TỰ ĐỘNG CẬP NHẬT TRẠNG THÁI ENROLLMENT
        updateStudentProgress(studentId, quiz.getClassEntity().getClassId());

        return QuizResultResponse.builder()
                .score(finalScore)
                .maxScore(maxScore)
                .status(passed ? "PASSED" : "FAILED")
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }

    /**
     * Logic đồng bộ trạng thái: Tính toán lại toàn bộ quá trình học của lớp này
     */
    private void updateStudentProgress(String studentId, String classId) {
        Enrollment enrollment = enrollmentRepository.findByStudent_UserIdAndClassEntity_ClassId(studentId, classId)
                .orElse(null);
        if (enrollment == null) return;

        // 1. Tối ưu: Chỉ lấy những bài thi thuộc lớp này từ Database
        // Giả sử ông đã thêm hàm này vào QuizAttemptRepository
        List<QuizAttempt> classAttempts = quizAttemptRepository.findByStudent_UserIdAndQuiz_ClassEntity_ClassId(studentId, classId);

        // 2. Gom nhóm lấy điểm cao nhất
        Map<String, Optional<QuizAttempt>> bestAttempts = classAttempts.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuiz().getQuizId(),
                        Collectors.maxBy(Comparator.comparingDouble(QuizAttempt::getScore))
                ));

        // 3. Tính toán các chỉ số
        long passedCount = bestAttempts.values().stream()
                .filter(opt -> opt.map(a -> a.getScore() >= 5.0).orElse(false))
                .count();

        double avgScore = bestAttempts.values().stream()
                .flatMap(Optional::stream)
                .mapToDouble(QuizAttempt::getScore)
                .average()
                .orElse(0.0);

        int totalQuizzesInClass = Optional.ofNullable(enrollment.getClassEntity().getTotalQuizzes()).orElse(0);

        // 4. Cập nhật và lưu
        enrollment.setFinalGrade(Math.round(avgScore * 10.0) / 10.0);

        // Nếu pass đủ 100% số quiz và điểm mỗi bài đều >= 5
        if (totalQuizzesInClass > 0 && passedCount == totalQuizzesInClass) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.PASSED);
        } else {
            enrollment.setStatus(Enrollment.EnrollmentStatus.LEARNING);
        }

        enrollmentRepository.save(enrollment);
    }
}