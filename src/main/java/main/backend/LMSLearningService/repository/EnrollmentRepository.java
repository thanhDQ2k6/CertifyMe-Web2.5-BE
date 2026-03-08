package main.backend.LMSLearningService.repository;

import main.backend.LMSLearningService.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 1. Hàm này phục vụ cho Dashboard (Lấy toàn bộ danh sách khóa học của 1 sv)
    // ĐÂY LÀ HÀM ÔNG ĐANG THIẾU DẪN ĐẾN LỖI compile
    List<Enrollment> findByStudent_UserId(String studentId);

    // 2. Hàm này phục vụ cho việc Update sau khi nộp bài Quiz
    Optional<Enrollment> findByStudent_UserIdAndClassEntity_ClassId(String studentId, String classId);

    // 3. Hàm này phục vụ cho trang chi tiết khóa học (Course Detail)
    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :studentId AND e.classEntity.course.courseId = :courseId")
    Optional<Enrollment> findByStudentIdAndCourseId(
            @Param("studentId") String studentId,
            @Param("courseId") String courseId
    );

    // 4. Hàm kiểm tra trạng thái (Dùng Enum chuẩn)
    boolean existsByStudent_UserIdAndClassEntity_ClassIdAndStatus(String userId, String classId, Enrollment.EnrollmentStatus status);
}