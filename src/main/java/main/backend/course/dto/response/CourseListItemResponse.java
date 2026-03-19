package main.backend.course.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseListItemResponse {
    private String courseId;
    private String courseCode;
    private String courseName;
    private String description;
}
