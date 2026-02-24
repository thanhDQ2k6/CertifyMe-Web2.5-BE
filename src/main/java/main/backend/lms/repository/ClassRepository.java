package main.backend.lms.repository;

import main.backend.lms.constant.ClassStatus;
import main.backend.lms.entity.Clazz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Đánh dấu đây là một Repository của Spring
public interface ClassRepository extends JpaRepository<Clazz, String> {

    // 1. Lấy lớp theo trạng thái (ACTIVE, COMPLETED)
    List<Clazz> findByStatus (ClassStatus status);

    // 2. Lấy danh sách lớp của một Giáo viên và lọc bỏ các lớp đã bị xóa mềm (CANCELED)
    List<Clazz> findByTeacher_UserIdAndStatusNot(String teacherId, ClassStatus status);

    // 3. Tìm lớp bằng Code (Vì UI thường search theo mã lớp như JAVA6, v.v.)
    Optional<Clazz> findByClassCode(String classCode);

    // 4. Kiểm tra xem một mã lớp đã tồn tại chưa trước khi tạo mới
    boolean existsByClassCode(String classCode);
}