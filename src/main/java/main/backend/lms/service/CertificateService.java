package main.backend.lms.service;

import lombok.RequiredArgsConstructor;
import main.backend.auth.entity.User;
import main.backend.lms.dto.response.CertificateResponse;
import main.backend.lms.model.Certificate;
import main.backend.lms.model.ClassEntity;
import main.backend.lms.model.CourseEntity;
import main.backend.lms.model.Enrollment;
import main.backend.lms.repository.CertificateRepository;
import main.backend.lms.repository.ClassRepository;
import main.backend.lms.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {
    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository; // Giả sử bảng này lưu trạng thái học tập
    private final ClassRepository classRepository;
    private final main.backend.auth.repository.UserRepository userRepository;
    private final main.backend.lms.repository.CourseRepository courseRepository;

    public boolean canIssueCertificate(String studentId, String courseId) {
        // 1. Lấy danh sách tất cả các Lớp (Class) thuộc Khóa học (Course) này
        List<ClassEntity> allInfoClasses = classRepository.findByCourse_CourseId(courseId);

        // 2. Kiểm tra xem sinh viên đã Pass hết đống Class này chưa
        for (ClassEntity classEntity : allInfoClasses) {
            boolean isPassed = enrollmentRepository.existsByStudent_UserIdAndClassEntity_ClassIdAndStatus(
                    studentId,
                    classEntity.getClassId(),
                    Enrollment.EnrollmentStatus.PASSED // Sử dụng Enum thay vì String
            );
            if (!isPassed) return false; // Chỉ cần 1 lớp chưa pass là nghỉ khỏe, chưa có bằng!
        }

        return true; // Pass hết rồi, cấp bằng thôi!
    }
    public List<CertificateResponse> getCertificatesByStudentId(String studentId) {
        // 1. Lấy danh sách Entity từ Database
        List<Certificate> certificates = certificateRepository.findByStudent_UserId(studentId);

        // 2. Chuyển đổi sang danh sách DTO để trả về Client
        return certificates.stream()
                .map(cert -> CertificateResponse.builder()
                        .courseName(cert.getCourse() != null ? cert.getCourse().getCourseName() : "N/A")
                        .courseCode(cert.getCourse() != null ? cert.getCourse().getCourseCode() : "N/A")
                        .certHash(cert.getCertificateHash()) // Lấy mã hash từ DB của ông
                        .certDate(cert.getIssueDate() != null ? cert.getIssueDate().toString() : "N/A")
                        .build())
                .collect(Collectors.toList());
    }
}
