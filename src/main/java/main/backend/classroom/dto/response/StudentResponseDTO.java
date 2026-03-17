package main.backend.classroom.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentResponseDTO {
    private String studentId;
    private String fullName;
    private String email;
    private String avatarUrl;
    private Integer completedQuizzes;
    private Integer totalQuizzes;
    private Double averageScore;
    private String status;
    private LocalDateTime enrolledAt;
}