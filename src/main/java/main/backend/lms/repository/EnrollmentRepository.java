package main.backend.lms.repository;

import main.backend.lms.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> { // Đổi String thành Long nếu enrollment_id là bigint

    // Spring sẽ tự sinh SQL: SELECT * FROM enrollments WHERE student_id = ?
    List<Enrollment> findByStudent_UserId(String studentId);
    boolean existsByStudent_UserIdAndClassEntity_ClassIdAndStatus(String userId, String classId, String status);

    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :studentId " +
            "AND e.classEntity.course.courseId = :courseId")
    Optional<Enrollment> findByStudentIdAndCourseId(
            @Param("studentId") String studentId,
            @Param("courseId") String courseId
    );
}