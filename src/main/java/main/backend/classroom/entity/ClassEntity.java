package main.backend.classroom.entity;

import jakarta.persistence.*;
import lombok.*;
import main.backend.auth.entity.User;
import main.backend.common.enums.ClassStatus;
import main.backend.course.entity.CourseEntity;

@Entity
@Table(name = "classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassEntity {
    @Id
    @Column(name = "class_id")
    private String classId;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    @Column(name = "class_code")
    private String classCode;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Column(name = "start_date")
    private java.time.LocalDate startDate;

    @Column(name = "end_date")
    private java.time.LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ClassStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "total_quizzes")
    private Integer totalQuizzes;
}