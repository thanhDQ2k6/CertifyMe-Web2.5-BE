package main.backend.lms.repository;
import main.backend.lms.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface CertificateRepository extends JpaRepository<Certificate, String> {
    // Tìm tất cả chứng chỉ mà cột student_id khớp với id truyền vào
    List<Certificate> findByStudent_UserId(String studentId);
}