package main.backend.lms.repository;

import main.backend.lms.constant.EnrollmentStatus;
import main.backend.lms.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Dòng này cực kỳ quan trọng
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Lấy theo Student ID (Đã sửa tên method cho khớp với Entity User)
    List<Enrollment> findByStudent_UserId(String studentId);

    // Lấy danh sách đang học
    List<Enrollment> findByStudent_UserIdAndStatus(String studentId, EnrollmentStatus status);

    // Tìm bản ghi cụ thể để check SV có trong lớp không
    Optional<Enrollment> findByStudent_UserIdAndClazz_ClassId(String studentId, String classId);

    // Tự động tăng số Quiz đã pass - Đã fix Params
    @Modifying
    @Transactional
    @Query("UPDATE Enrollment e SET e.passedQuizzes = e.passedQuizzes + 1 " +
            "WHERE e.student.userId = :studentId AND e.clazz.classId = :classId")
    void incrementPassedQuizzes(@Param("studentId") String studentId, @Param("classId") String classId);
}