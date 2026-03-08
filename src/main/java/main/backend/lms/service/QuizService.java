package main.backend.lms.service;

import lombok.RequiredArgsConstructor;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.lms.dto.response.QuizResultResponse;
import main.backend.lms.model.Question;
import main.backend.lms.model.Quiz;
import main.backend.lms.model.QuizAttempt;
import main.backend.lms.repository.QuizAttemptRepository;
import main.backend.lms.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuizResultResponse submitQuiz(String quizId, String studentId, List<String> userAnswers) {
        // 1. Tìm Quiz trong DB
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz không tồn tại"));

        // 2. Lấy danh sách câu hỏi
        List<Question> questions = quiz.getQuestions();
        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("Bài thi này chưa có câu hỏi nào!");
        }

        // 3. Tính điểm
        int correctCount = 0;
        int totalQuestions = questions.size();

        for (int i = 0; i < totalQuestions; i++) {
            if (i < userAnswers.size()) {
                // Lấy đáp án đúng (Enum A, B, C, D) chuyển thành String để so sánh
                String correctAns = questions.get(i).getCorrectAnswer().name();
                String userAns = userAnswers.get(i); // User gửi lên "A", "B",...

                if (correctAns.equalsIgnoreCase(userAns)) {
                    correctCount++;
                }
            }
        }

        // Tính toán Score dựa trên maxScore trong DB (thường là 10)
        double maxScore = (quiz.getMaxScore() != null) ? quiz.getMaxScore() : 10.0;
        double finalScore = ((double) correctCount / totalQuestions) * maxScore;

        // Làm tròn 1 chữ số thập phân
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

        // 5. Trả về Response đầy đủ cho Postman
        return QuizResultResponse.builder()
                .score(finalScore)
                .maxScore(maxScore)
                .status(passed ? "PASSED" : "FAILED")
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }
}