package main.backend.enrollment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import main.backend.auth.entity.User;
import main.backend.classroom.entity.ClassEntity;
import main.backend.common.enums.EnrollmentStatus;

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

  @Builder.Default
  @Column(name = "passed_quizzes")
  private Integer passedQuizzes = 0;

  @Column(name = "final_grade")
  private Double finalGrade;

  @Builder.Default
  @Column(name = "joined_at")
  private LocalDateTime joinedAt = LocalDateTime.now();

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private EnrollmentStatus status = EnrollmentStatus.LEARNING;
}
