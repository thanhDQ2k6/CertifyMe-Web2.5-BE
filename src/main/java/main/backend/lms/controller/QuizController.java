package main.backend.lms.controller;

import lombok.RequiredArgsConstructor;
import main.backend.lms.dto.response.QuizResultResponse;
import main.backend.lms.service.QuizService;
import main.backend.lms.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import main.backend.lms.dto.response.CertificateResponse;
import java.util.stream.Collectors;

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