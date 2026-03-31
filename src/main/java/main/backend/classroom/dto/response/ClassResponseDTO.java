package main.backend.classroom.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ClassResponseDTO {
    private String classId;
    private String classCode;
    private String courseName;
    private String courseId;
    private String teacherName;
    private int studentCount;
    private int quizCount;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}