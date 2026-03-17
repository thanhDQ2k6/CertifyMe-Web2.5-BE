package main.backend.certificate.service;

import lombok.RequiredArgsConstructor;
import main.backend.certificate.dto.response.CertificateResponse;
import main.backend.certificate.entity.Certificate;
import main.backend.certificate.repository.CertificateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;

    // FIX CB3: trả đầy đủ fields: certificateId, averageScore, status, blockchainInfo
    // FIX CB5: courseCode lấy từ course.courseCode thay vì classCode
    public List<CertificateResponse> getCertificatesByStudentId(String studentId) {
        List<Certificate> certificates = certificateRepository.findByStudent_UserId(studentId);

        return certificates.stream().map(this::mapToResponse).toList();
    }

    private CertificateResponse mapToResponse(Certificate cert) {
        var classEntity = cert.getClassEntity();
        String courseName = classEntity != null && classEntity.getCourse() != null
                ? classEntity.getCourse().getCourseName() : null;
        String courseCode = classEntity != null && classEntity.getCourse() != null
                ? classEntity.getCourse().getCourseCode() : null;

        CertificateResponse.BlockchainInfo blockchainInfo = null;
        if (cert.getTransactionHash() != null) {
            blockchainInfo = CertificateResponse.BlockchainInfo.builder()
                    .hash(cert.getCertificateHash())
                    .txHash(cert.getTransactionHash())
                    .block(cert.getBlockNumber() != null ? String.valueOf(cert.getBlockNumber()) : null)
                    .contract(cert.getContractAddress())
                    .build();
        }

        return CertificateResponse.builder()
                .certificateId(cert.getCertificateId())
                .courseName(courseName)
                .courseCode(courseCode)
                .verificationHash(cert.getCertificateHash())
                .status(cert.getStatus() != null ? cert.getStatus().name().toLowerCase() : "pending")
                .issuedAt(cert.getIssueDate() != null ? cert.getIssueDate().toString() : null)
                .blockchainInfo(blockchainInfo)
                .build();
    }
}
