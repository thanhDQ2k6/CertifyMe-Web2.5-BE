package main.backend.classroom.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StudentResponseDTO {

  private String studentId;
  private String studentCode;
  private String fullName;
  private String email;
  private String avatarUrl;
  private Integer completedQuizzes;
  private Integer totalQuizzes;
  private Double averageScore;
  private String status;
  private LocalDateTime enrolledAt;
}
