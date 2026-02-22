package main.backend.lms.dto.response;

import lombok.Data;

@Data
public class ClassResponseDTO {
    private String classId;
    private String classCode;
    private String courseName;
    private String teacherName;
    private int totalQuizzes;
    private int passedQuizzes;
    private double progressPercentage; // Tính toán: (passed/total)*100
    private String status; // ACTIVE, COMPLETED, CANCELED
}