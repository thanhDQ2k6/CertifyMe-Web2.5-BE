package main.backend.lms.model;
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
    @Column(name = "course_icon")
    private String courseIcon; // Sẽ nhận giá trị "icon_java"
    @Column(name = "course_code")
    private String courseCode;

    @Column(name = "course_name")
    private String courseName;

    private String description;

    // THÊM DÒNG NÀY VÀO LÀ HẾT LỖI
    @Column(name = "is_active")
    private boolean isActive;

}