package main.backend.LMSCourseService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDTO {
    private String quizId;
    private String classId;
    private String quizName;
    private Integer duration;
    private Double passingScore;
    private Integer maxScore;
    private Integer questionCount;
    private Integer completionRate;
    private Double averageScore;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuestionResponseDTO> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResponseDTO {
        private Long questionId;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
    }
}