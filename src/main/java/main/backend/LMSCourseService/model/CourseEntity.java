package main.backend.LMSCourseService.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity {
    @Id
    @Column(name = "course_id")
    private String courseId;

    @Column(name = "course_code")
    private String courseCode;

    @Column(name = "course_name")
    private String courseName;

    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "is_active")
    private boolean isActive;

}