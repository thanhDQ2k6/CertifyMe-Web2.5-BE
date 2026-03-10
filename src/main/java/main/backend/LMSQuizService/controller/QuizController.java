package main.backend.LMSQuizService.controller;

import lombok.RequiredArgsConstructor;
import main.backend.common.dto.ApiResponse;
import main.backend.lms.dto.request.QuizRequestDTO;
import main.backend.LMSQuizService.dto.response.QuizResultResponse;
import main.backend.LMSCourseService.dto.response.QuizResponseDTO;
import main.backend.LMSCourseService.dto.response.QuizSubmissionResponseDTO;
import main.backend.LMSQuizService.service.QuizService;
import main.backend.LMSLearningService.service.StudentService;
import main.backend.lms.dto.request.QuizSubmissionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final StudentService studentService;

    @PostMapping("/quizzes/{quizId}/submit")
    public ResponseEntity<?> submit(@PathVariable String quizId, @RequestBody QuizSubmissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Nộp bài thành công", quizService.submitQuiz(quizId, request)));
    }

    @GetMapping("/classes/{classId}/quizzes")
    public ResponseEntity<ApiResponse<List<QuizResponseDTO>>> getQuizzesByClass(@PathVariable String classId) {
        return ResponseEntity.ok(ApiResponse.success("Success", quizService.getQuizzesByClassResponse(classId)));
    }

    @PostMapping("/quizzes")
    public ResponseEntity<ApiResponse<QuizResponseDTO>> createQuiz(@RequestBody QuizRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz created successfully", quizService.createQuiz(dto)));
    }

    @PutMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponseDTO>> updateQuiz(@PathVariable String quizId, @RequestBody QuizRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Quiz updated successfully", quizService.updateQuiz(quizId, dto)));
    }

    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable String quizId) {
        quizService.softDeleteQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.success("Quiz deleted successfully", null));
    }

    @GetMapping("/quizzes/{quizId}/submissions")
    public ResponseEntity<ApiResponse<List<QuizSubmissionResponseDTO>>> getQuizSubmissions(@PathVariable String quizId) {
        return ResponseEntity.ok(ApiResponse.success("Success", quizService.getQuizSubmissions(quizId)));
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponseDTO>> getQuizDetail(@PathVariable String quizId) {
        QuizResponseDTO quizDetail = quizService.getQuizDetail(quizId);
        return ResponseEntity.ok(ApiResponse.success("Success", quizDetail));
    }
}