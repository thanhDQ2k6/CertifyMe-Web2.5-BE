package main.backend.enrollment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnrollmentRequestDTO {

  // Có thể dùng studentId (UUID) HOẶC studentCode (HS00001)
  private String studentId;
  private String studentCode;

  @NotBlank
  private String classId;

  /**
   * Lấy identifier để tìm student (ưu tiên code nếu có)
   */
  public String getStudentIdentifier() {
    if (studentCode != null && !studentCode.isBlank()) {
      return studentCode;
    }
    return studentId;
  }
}
