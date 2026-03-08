package main.backend.lms.repository;

import main.backend.lms.model.ClassEntity;
// Trỏ về đúng thực thể User của nhóm trưởng
import main.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ClassRepository extends JpaRepository<ClassEntity, String> {
    // Tìm danh sách các lớp thuộc về một khóa học
    // Giả sử trong ClassEntity có field: CourseEntity course;
    List<ClassEntity> findByCourse_CourseId (String courseId);

    // Hoặc nếu field là courseId (dạng Long) thì dùng:
    // List<ClassEntity> findByCourseId(Long courseId);
}