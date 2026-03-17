package main.backend.classroom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClassRequestDTO {
    @NotBlank
    private String classCode;
    private String courseName;
    @NotBlank
    private String courseId;
    @NotBlank
    private String teacherId;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private String description;
}
