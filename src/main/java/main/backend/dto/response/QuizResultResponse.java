package main.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizResultResponse {
    private double score;
    private boolean isPassed;
    private String message;
}