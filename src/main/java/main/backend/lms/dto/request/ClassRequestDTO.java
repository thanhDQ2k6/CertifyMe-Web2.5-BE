package main.backend.lms.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ClassRequestDTO {
    private String classCode;
    private String teacherId;
    private LocalDate startDate;
    private LocalDate endDate;
}