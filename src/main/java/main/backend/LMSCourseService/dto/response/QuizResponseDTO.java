package main.backend.LMSCourseService.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
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
    private String status; // "active", "draft", "archived"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}