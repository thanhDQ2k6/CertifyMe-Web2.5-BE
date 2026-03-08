package main.backend.LMSQuizService.model;
import jakarta.persistence.*;
import lombok.*;
import main.backend.LMSCourseService.model.ClassEntity;

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
    private ClassEntity classEntity; // Đổi từ Course thành ClassEntity

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
    @Column(name = "max_score")
    private Double maxScore;
    public enum QuizStatus { DRAFT, PUBLISHED, CLOSED }
    // Trong main.backend.lms.LMSQuizService.model.Quiz
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<Question> questions; // Danh sách câu hỏi PHẢI nằm ở đây
}