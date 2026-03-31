package main.backend.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import main.backend.auth.entity.User;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;
    @Column(name = "score")
    private Double score;
    @Column(name = "is_passed")
    private Boolean isPassed;
    @Column(name = "is_best_attempt")
    private Boolean isBestAttempt;
    @Column(name = "started_at")
    private java.time.LocalDateTime startedAt;
    @Column(name = "submitted_at")
    private java.time.LocalDateTime submittedAt;
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
}