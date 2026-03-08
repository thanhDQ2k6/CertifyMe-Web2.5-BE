package main.backend.LMSQuizService.controller;

import lombok.RequiredArgsConstructor;
import main.backend.LMSQuizService.dto.response.QuizResultResponse;
import main.backend.LMSQuizService.service.QuizService;
import main.backend.LMSLearningService.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // Đảm bảo có prefix /api nếu Postman gọi /api/...
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final StudentService studentService;

    // POST /api/quizzes/{quizId}/submit
    @PostMapping("/quizzes/{quizId}/submit")
    public ResponseEntity<QuizResultResponse> submitQuiz(
            @PathVariable String quizId,
            @RequestBody List<String> answers) {

        // Tạm thời fix cứng studentId là "u1" để test, sau này lấy từ JWT
        String studentId = "u1";
        return ResponseEntity.ok(quizService.submitQuiz(quizId, studentId, answers));
    }


}