package main.backend.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import main.backend.auth.entity.User;
import main.backend.common.enums.EnrollmentStatus;
import main.backend.classroom.entity.ClassEntity;

@Entity
@Table(name = "enrollments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Column(name = "passed_quizzes")
    private Integer passedQuizzes = 0;

    @Column(name = "final_grade")
    private Double finalGrade;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EnrollmentStatus status = EnrollmentStatus.LEARNING;
}