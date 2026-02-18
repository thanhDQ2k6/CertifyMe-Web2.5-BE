package main.backend.service;

import main.backend.dto.request.QuizSubmissionRequest;
import main.backend.dto.response.QuizResultResponse;
import main.backend.entity.Quiz;
import java.util.List;

public interface QuizService {
    List<Quiz> getQuizzesByClass(String classId);
    QuizResultResponse submitQuiz(QuizSubmissionRequest request);
    void softDeleteQuiz(String quizId); // Cập nhật status = CLOSED
}