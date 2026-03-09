package main.backend.LMSCourseService.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private String courseId;
    private String courseCode;
    private String courseName;
    private String courseIcon;
    private String teacherName;
    private Integer progress;
    private Integer totalQuizzes;
    private Integer completedQuizzes;
    private Double averageScore;
    private boolean isCompleted;
}