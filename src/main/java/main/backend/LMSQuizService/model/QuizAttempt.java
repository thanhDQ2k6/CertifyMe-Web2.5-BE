package main.backend.LMSQuizService.model;

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
    private Long attemptId; // MySQL: bigint

    @Column(name = "score")
    private Double score; // MySQL: double

    @Column(name = "is_passed")
    private Boolean isPassed; // MySQL: tinyint(1)

    @Column(name = "is_best_attempt")
    private Boolean isBestAttempt; // MySQL: tinyint(1)

    @Column(name = "submitted_at")
    private java.time.LocalDateTime submittedAt; // MySQL: datetime

    // Khai báo mối quan hệ để JPA biết JOIN vào bảng nào
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
}