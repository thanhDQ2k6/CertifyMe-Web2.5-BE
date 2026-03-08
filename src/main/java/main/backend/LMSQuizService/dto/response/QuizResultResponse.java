package main.backend.LMSQuizService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuizResultResponse {
    private Double score;
    private Double maxScore;
    private String status;
    private LocalDateTime submittedAt;
}