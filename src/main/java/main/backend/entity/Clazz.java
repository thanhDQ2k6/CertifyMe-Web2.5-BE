package main.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import main.backend.constant.ClassStatus;
import java.time.LocalDate;

@Entity
@Table(name = "classes")
@Data
public class Clazz {
    @Id
    private String classId;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private String classCode;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalQuizzes = 0;

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.ACTIVE; // Logic xóa mềm: CANCELED
}
