package main.backend.service.impl;

import main.backend.constant.QuizStatus;
import main.backend.dto.request.QuizSubmissionRequest;
import main.backend.dto.response.QuizResultResponse;
import main.backend.entity.Quiz;
import main.backend.entity.QuizQuestion;
import main.backend.repository.*;
import main.backend.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class QuizServiceImpl implements QuizService {
    @Autowired private QuizRepository quizRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private EnrollmentRepository enrollmentRepo;

    @Override
    @Transactional
    public QuizResultResponse submitQuiz(QuizSubmissionRequest request) {
        List<QuizQuestion> questions = questionRepo.findByQuiz_QuizId(request.getQuizId());
        long correct = 0;

        for (QuizQuestion q : questions) {
            String studentAns = request.getAnswers().get(q.getQuestionId());
            if (q.getCorrectAnswer().equalsIgnoreCase(studentAns)) {
                correct++;
            }
        }

        double score = (double) correct / questions.size() * 10;
        boolean passed = score >= 5.0;

        // Cập nhật tiến độ vào bảng Enrollment nếu qua môn
        if (passed) {
            enrollmentRepo.incrementPassedQuizzes(request.getStudentId(), request.getClassId());
        }

        return new QuizResultResponse(score, passed, passed ? "Chúc mừng!" : "Rất tiếc!");
    }

    @Override
    public void softDeleteQuiz(String quizId) {
        Quiz quiz = quizRepo.findById(quizId).orElseThrow();
        quiz.setStatus(QuizStatus.CLOSED); // Xóa kiểu cập nhật
        quizRepo.save(quiz);
    }

    @Override
    public List<Quiz> getQuizzesByClass(String classId) {
        return quizRepo.findByClazz_ClassIdAndStatusNot(classId, QuizStatus.CLOSED);
    }
}