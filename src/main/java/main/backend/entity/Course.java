package main.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor // Thêm cái này để JPA khởi tạo được object
@AllArgsConstructor // Thêm cái này để tiện tạo mới bằng code
public class Course {

    @Id
    @Column(name = "course_id")
    private String courseId;

    @Column(name = "course_code", nullable = false, unique = true)
    private String courseCode; // VD: JAVA6

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}