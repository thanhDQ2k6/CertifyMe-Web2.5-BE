package main.backend.quiz.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.backend.common.dto.ApiResponse;
import main.backend.quiz.dto.request.QuizRequestDTO;
import main.backend.quiz.dto.response.QuizResultResponse;
import main.backend.course.dto.response.QuizResponseDTO;
import main.backend.course.dto.response.QuizSubmissionResponseDTO;
import main.backend.quiz.service.QuizService;
import main.backend.quiz.dto.request.QuizSubmissionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/quizzes/{quizId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<QuizResultResponse>> submit(
            @PathVariable String quizId, @Valid @RequestBody QuizSubmissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(quizService.submitQuiz(quizId, request)));
    }

    @GetMapping("/classes/{classId}/quizzes")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<QuizResponseDTO>>> getQuizzesByClass(@PathVariable String classId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizzesByClassResponse(classId)));
    }

    @PostMapping("/quizzes")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<QuizResponseDTO>> createQuiz(@Valid @RequestBody QuizRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(quizService.createQuiz(dto)));
    }

    @PutMapping("/quizzes/{quizId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<QuizResponseDTO>> updateQuiz(
            @PathVariable String quizId, @Valid @RequestBody QuizRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(quizService.updateQuiz(quizId, dto)));
    }

    @DeleteMapping("/quizzes/{quizId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable String quizId) {
        quizService.softDeleteQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/quizzes/{quizId}/submissions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<QuizSubmissionResponseDTO>>> getQuizSubmissions(@PathVariable String quizId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizSubmissions(quizId)));
    }

    @GetMapping("/quizzes/{quizId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<QuizResponseDTO>> getQuizDetail(@PathVariable String quizId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizDetail(quizId)));
    }
}
