package main.backend.LMSCourseService.model;

import jakarta.persistence.*;
import lombok.*;

// Import đúng User từ gói của nhóm trưởng
import main.backend.auth.entity.User;

@Entity
@Table(name = "classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Thêm Builder để sau này tạo dữ liệu mẫu cho dễ
public class ClassEntity {
    @Id
    @Column(name = "class_id")
    private String classId;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private CourseEntity course; // Một lớp thuộc về một khóa học

    @Column(name = "class_code")
    private String classCode;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher; // Bây giờ đã nhận diện được User
    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;
    @Column(name = "total_quizzes")
    private Integer totalQuizzes;
}