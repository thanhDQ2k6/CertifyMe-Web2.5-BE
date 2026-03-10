package main.backend.LMSCertificateBlockchainService.service;

import lombok.RequiredArgsConstructor;
import main.backend.LMSCourseService.repository.CourseRepository;
import main.backend.LMSCertificateBlockchainService.dto.response.CertificateResponse;
import main.backend.LMSCertificateBlockchainService.model.Certificate;
import main.backend.LMSCourseService.model.ClassEntity;
import main.backend.LMSLearningService.model.Enrollment;
import main.backend.LMSCertificateBlockchainService.repository.CertificateRepository;
import main.backend.LMSCourseService.repository.ClassRepository;
import main.backend.LMSLearningService.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {
    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;
    private final main.backend.auth.repository.UserRepository userRepository;
    private final CourseRepository courseRepository;

    public boolean canIssueCertificate(String studentId, String courseId) {
        List<ClassEntity> allInfoClasses = classRepository.findAll().stream()
                .filter(c -> c.getCourse() != null && c.getCourse().getCourseId().equals(courseId))
                .collect(Collectors.toList());

        for (ClassEntity classEntity : allInfoClasses) {
            boolean isPassed = enrollmentRepository.existsByStudent_UserIdAndClassEntity_ClassIdAndStatus(
                    studentId,
                    classEntity.getClassId(),
                    Enrollment.EnrollmentStatus.PASSED
            );
            if (!isPassed) return false;
        }
        return true;
    }

    public List<CertificateResponse> getCertificatesByStudentId(String studentId) {
        List<Certificate> certificates = certificateRepository.findByStudent_UserId(studentId);
        return certificates.stream()
                .map(cert -> CertificateResponse.builder()
                        .courseName(cert.getClassEntity() != null && cert.getClassEntity().getCourse() != null ? cert.getClassEntity().getCourse().getCourseName() : "N/A")
                        .courseCode(cert.getClassEntity() != null ? cert.getClassEntity().getClassCode() : "N/A")
                        .verificationHash(cert.getCertificateHash())
                        .issuedAt(cert.getIssueDate() != null ? cert.getIssueDate().toString() : "N/A")
                        .build())
                .collect(Collectors.toList());
    }
}