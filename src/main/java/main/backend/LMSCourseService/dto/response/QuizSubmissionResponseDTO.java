package main.backend.LMSCourseService.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizSubmissionResponseDTO {
    private String submissionId;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private Double score;
    private Integer maxScore;
    private Boolean passed;
    private LocalDateTime submittedAt;
}