package main.backend.enrollment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrollmentResponseDTO {

  private Long enrollmentId;
  private String studentId;
  private String studentCode;
  private String studentName;
  private String classId;
  private String className;
  private String status;
  private String enrolledAt;
}
