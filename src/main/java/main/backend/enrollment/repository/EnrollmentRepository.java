package main.backend.enrollment.repository;

import main.backend.enrollment.entity.Enrollment;
import main.backend.common.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByClassEntity_ClassId(String classId);
    long countByClassEntity_ClassId(String classId);
    List<Enrollment> findByStudent_UserId(String studentId);


    Optional<Enrollment> findByStudent_UserIdAndClassEntity_ClassId(String studentId, String classId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :studentId AND e.classEntity.course.courseId = :courseId")
    Optional<Enrollment> findByStudentIdAndCourseId(
            @Param("studentId") String studentId,
            @Param("courseId") String courseId
    );

    boolean existsByStudent_UserIdAndClassEntity_ClassIdAndStatus(String userId, String classId, EnrollmentStatus status);
}