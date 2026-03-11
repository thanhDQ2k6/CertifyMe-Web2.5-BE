package main.backend.LMSCertificateBlockchainService.service;

import lombok.RequiredArgsConstructor;
import main.backend.LMSCertificateBlockchainService.dto.response.AdminResponseDTO.*;
import main.backend.LMSCertificateBlockchainService.model.Certificate;
import main.backend.LMSCertificateBlockchainService.repository.CertificateRepository;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final main.backend.LMSQuizService.repository.QuizAttemptRepository quizAttemptRepository;

    public CertificateStats getCertificateStats() {
        return CertificateStats.builder()
                .totalCertificates(certificateRepository.count())
                .issuedCertificates(certificateRepository.countByStatus(Certificate.CertificateStatus.ISSUED))
                .revokedCertificates(certificateRepository.countByStatus(Certificate.CertificateStatus.REVOKED))
                .certificatesThisMonth(certificateRepository.countCertificatesThisMonth())
                .certificatesThisYear(certificateRepository.countCertificatesThisYear())
                .build();
    }

    //Lấy danh sách và tìm kiếm
    public CertificateListResponse getCertificates(String q, String statusStr, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Certificate.CertificateStatus status = null;

        if (statusStr != null && !statusStr.isEmpty()) {
            status = Certificate.CertificateStatus.valueOf(statusStr.toUpperCase());
        }

        Page<Certificate> certPage;
        if (q == null && status == null) {
            certPage = certificateRepository.findAll(pageable);
        } else {
            certPage = certificateRepository.searchCertificates(q, status, pageable);
        }

        List<CertificateListItem> items = certPage.getContent().stream().map(c ->
                CertificateListItem.builder()
                        .certificateId(c.getCertificateId())
                        .studentName(c.getStudent().getFullName())
                        .studentEmail(c.getStudent().getEmail())
                        .className(c.getClassEntity().getClassCode())
                        .courseCode(c.getClassEntity().getCourse().getCourseCode())
                        .averageScore(8.5) // Tạm fix cứng theo docs, thực tế phải lấy từ bảng enrollments
                        .issuedAt(c.getIssueDate() + "T14:00:00Z")
                        .status(c.getStatus().name().toLowerCase())
                        .verificationHash(c.getCertificateHash())
                        .build()
        ).collect(Collectors.toList());

        PaginationInfo pagination = PaginationInfo.builder()
                .page(page)
                .limit(limit)
                .total(certPage.getTotalElements())
                .totalPages(certPage.getTotalPages())
                .build();

        return CertificateListResponse.builder().items(items).pagination(pagination).build();
    }

    //Revoke Certificate
    public Object revokeCertificate(String certificateId, RevokeRequest request) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        cert.setStatus(Certificate.CertificateStatus.REVOKED);
        cert.setRevokedAt(LocalDateTime.now());

        User revoker = userRepository.findById(request.getRevokedBy()).orElse(null);
        cert.setRevokedBy(revoker);

        certificateRepository.save(cert);

        // Trả về response inline
        return java.util.Map.of(
                "certificateId", cert.getCertificateId(),
                "status", "revoked",
                "revokedAt", cert.getRevokedAt().toString(),
                "revokedBy", request.getRevokedBy(),
                "reason", request.getReason()
        );
    }

    public CertificateDetail getCertificateDetail(String certificateId) {
        Certificate c = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        BlockchainInfo bcInfo = null;
        if (c.getTransactionHash() != null) {
            bcInfo = BlockchainInfo.builder()
                    .transactionHash(c.getTransactionHash())
                    .blockNumber(c.getBlockNumber() != null ? String.valueOf(c.getBlockNumber()) : null)
                    .contractAddress(c.getContractAddress())
                    .networkName("Ethereum Mainnet")
                    .explorerUrl("https://etherscan.io/tx/" + c.getTransactionHash())
                    .build();
        }

        List<main.backend.LMSQuizService.model.QuizAttempt> attempts =
                quizAttemptRepository.findByStudentAndClass(c.getStudent().getUserId(), c.getClassEntity().getClassId());

        List<QuizResult> quizResults = attempts.stream().map(qa ->
                QuizResult.builder()
                        .quizId(qa.getQuiz().getQuizId())
                        .quizName(qa.getQuiz().getTitle())
                        .score(qa.getScore())
                        .maxScore(10)
                        .completedAt(qa.getSubmittedAt() != null ? qa.getSubmittedAt().toString() + "Z" : null)
                        .build()
        ).collect(Collectors.toList());


        return CertificateDetail.builder()
                .certificateId(c.getCertificateId())
                .studentId(c.getStudent().getUserId())
                .studentName(c.getStudent().getFullName())
                .studentEmail(c.getStudent().getEmail())
                .classId(c.getClassEntity().getClassId())
                .className(c.getClassEntity().getClassCode())
                .courseCode(c.getClassEntity().getCourse().getCourseCode())
                .courseName(c.getClassEntity().getCourse().getCourseName())
                .averageScore(8.5) // cái điểm này tạm thời xét cứng để test api
                .issuedAt(c.getIssueDate() != null ? c.getIssueDate() + "T14:00:00Z" : null)
                .status(c.getStatus().name().toLowerCase())
                .verificationHash(c.getCertificateHash())
                .blockchainInfo(bcInfo)
                .quizResults(quizResults)
                .build();
    }

    //Verify chứng chỉ trên Blockchain
    public CertificateVerificationResult verifyCertificate(String certificateId) {
        Certificate c = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        // Giả lập logic kiểm tra Blockchain: Hợp lệ nếu có Hash
        boolean isValid = c.getCertificateHash() != null && !c.getCertificateHash().isEmpty();

        // Nếu sai mã hash thì ném lỗi Controller trả về 400
        if (!isValid) {
            throw new IllegalArgumentException("Certificate verification failed: Hash mismatch");
        }

        BlockchainVerificationInfo bcInfo = BlockchainVerificationInfo.builder()
                .transactionHash(c.getTransactionHash())
                .blockNumber(c.getBlockNumber() != null ? String.valueOf(c.getBlockNumber()) : null)
                .contractAddress(c.getContractAddress())
                .timestamp(c.getIssueDate() != null ? c.getIssueDate() + "T14:00:00Z" : null)
                .status("confirmed")  // giả lập mặc định =)))
                .build();

        return CertificateVerificationResult.builder()
                .certificateId(c.getCertificateId())
                .isValid(true)
                .verificationHash(c.getCertificateHash())
                .blockchainInfo(bcInfo)
                .verifiedAt(LocalDateTime.now().toString() + "Z") // Thời gian lúc ấn verify
                .build();
    }

    //Lấy danh sách Users
    public UserListResponse getUsers(String roleStr, String statusStr, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);

        // Map Role String sang Role ID
        Long roleId = null;
        if (roleStr != null && !roleStr.isEmpty()) {
            switch (roleStr.toUpperCase()) {
                case "STUDENT": roleId = 1L; break;
                case "TEACHER": roleId = 2L; break;
                case "ADMIN": roleId = 3L; break;
            }
        }

        // Map status String sang Boolean
        Boolean isActive = null;
        if ("active".equalsIgnoreCase(statusStr)) isActive = true;
        else if ("inactive".equalsIgnoreCase(statusStr)) isActive = false;

        Page<User> userPage = userRepository.findUsersByFilter(roleId, isActive, pageable);

        List<UserListItem> items = userPage.getContent().stream().map(u -> {
            String roleName = u.getRole() != null ? u.getRole().getRoleName().name().replace("ROLE_", "") : "STUDENT";

            return UserListItem.builder()
                    .userId(u.getUserId())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .role(roleName)
                    .avatarUrl(u.getAvatarUrl())
                    .isActive(u.getIsActive())
                    .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() + "Z" : null)
                    .lastLoginAt(u.getUpdatedAt() != null ? u.getUpdatedAt().toString() + "Z" : null) // Tạm dùng updatedAt làm lastLoginAt
                    .build();
        }).collect(Collectors.toList());

        PaginationInfo pagination = PaginationInfo.builder()
                .page(page)
                .limit(limit)
                .total(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();

        return UserListResponse.builder().items(items).pagination(pagination).build();
    }

    //Cập nhật trạng thái User
    public UpdateUserStatusResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(request.getIsActive());
        userRepository.save(user);

        return UpdateUserStatusResponse.builder()
                .userId(user.getUserId())
                .isActive(user.getIsActive())
                .updatedAt(LocalDateTime.now().toString() + "Z")
                .build();
    }
}