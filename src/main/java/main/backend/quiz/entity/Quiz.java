package main.backend.quiz.entity;
import jakarta.persistence.*;
import lombok.*;
import main.backend.classroom.entity.ClassEntity;
import main.backend.common.enums.QuizStatus;

import java.util.List;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {
    @Id
    @Column(name = "quiz_id")
    private String quizId;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 60;

    @Column(name = "passing_score")
    private Double passingScore = 5.0;

    @Enumerated(EnumType.STRING)
    private QuizStatus status = QuizStatus.DRAFT;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<Question> questions;

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
}