package main.backend.lms.controller;

import main.backend.lms.dto.request.QuizSubmissionRequest;
import main.backend.lms.dto.response.QuizResultResponse;
import main.backend.lms.entity.Quiz;
import main.backend.lms.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<Quiz>> getByClass(@PathVariable String classId) {
        return ResponseEntity.ok(quizService.getQuizzesByClass(classId));
    }

    // NỘP BÀI VÀ NHẬN KẾT QUẢ TỨC THÌ
    @PostMapping("/submit")
    public ResponseEntity<QuizResultResponse> submit(@RequestBody QuizSubmissionRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        quizService.softDeleteQuiz(id);
        return ResponseEntity.noContent().build();
    }
}
