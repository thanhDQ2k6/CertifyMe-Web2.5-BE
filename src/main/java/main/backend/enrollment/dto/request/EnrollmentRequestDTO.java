package main.backend.enrollment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnrollmentRequestDTO {
    @NotBlank
    private String studentId;
    @NotBlank
    private String classId;
}
