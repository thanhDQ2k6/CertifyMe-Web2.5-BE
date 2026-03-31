package main.backend.quiz.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizResultDetailResponse {
    private String quizId;
    private String quizTitle;
    private Double score;
    private Double maxScore;
    private Double passingScore;
    private String status;
    private Integer attemptCount;
    private String submittedAt;
}
