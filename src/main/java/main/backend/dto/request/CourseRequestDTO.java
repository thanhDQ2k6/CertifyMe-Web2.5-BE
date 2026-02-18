package main.backend.dto.request;

import lombok.Data;

@Data
public class CourseRequestDTO {
    private String courseCode;
    private String courseName;
    private String description;
}