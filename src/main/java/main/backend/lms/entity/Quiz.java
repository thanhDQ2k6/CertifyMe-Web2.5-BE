package main.backend.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.backend.lms.constant.QuizStatus;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    @Id
    @Column(name = "quiz_id")
    private String quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Clazz clazz;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "passing_score")
    private Double passingScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private QuizStatus status = QuizStatus.DRAFT; // Các trạng thái: DRAFT, PUBLISHED, CLOSED (Xóa mềm)
}