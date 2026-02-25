package main.backend.lms.service;

import main.backend.lms.dto.request.QuizSubmissionRequest;
import main.backend.lms.dto.response.QuizResultResponse;
import main.backend.lms.entity.Quiz;
import java.util.List;

public interface QuizService {
    List<Quiz> getQuizzesByClass(String classId);
    QuizResultResponse submitQuiz(QuizSubmissionRequest request);
    void softDeleteQuiz(String quizId); // Cập nhật status = CLOSED
}