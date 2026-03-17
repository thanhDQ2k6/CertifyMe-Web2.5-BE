package main.backend.admin.service;

import lombok.RequiredArgsConstructor;
import main.backend.admin.dto.response.AdminResponseDTO.*;
import main.backend.auth.entity.Role;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import main.backend.auth.repository.RoleRepository;
import main.backend.auth.repository.UserRepository;
import main.backend.auth.service.UserQueryService;
import main.backend.certificate.entity.Certificate;
import main.backend.certificate.repository.CertificateRepository;
import main.backend.common.enums.CertificateStatus;
import main.backend.common.exception.ResourceNotFoundException;
import main.backend.enrollment.entity.Enrollment;
import main.backend.enrollment.repository.EnrollmentRepository;
import main.backend.quiz.entity.QuizAttempt;
import main.backend.quiz.repository.QuizAttemptRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserQueryService userQueryService;
    private final UserRepository userRepository; // admin needs write access to users
    private final RoleRepository roleRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    public CertificateStats getCertificateStats() {
        return CertificateStats.builder()
                .totalCertificates(certificateRepository.count())
                .issuedCertificates(certificateRepository.countByStatus(CertificateStatus.ISSUED))
                .revokedCertificates(certificateRepository.countByStatus(CertificateStatus.REVOKED))
                .certificatesThisMonth(certificateRepository.countCertificatesThisMonth())
                .certificatesThisYear(certificateRepository.countCertificatesThisYear())
                .build();
    }

    public CertificateListResponse getCertificates(String q, String statusStr, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        CertificateStatus status = null;

        if (statusStr != null && !statusStr.isEmpty()) {
            status = CertificateStatus.valueOf(statusStr.toUpperCase());
        }

        Page<Certificate> certPage;
        if (q == null && status == null) {
            certPage = certificateRepository.findAll(pageable);
        } else {
            certPage = certificateRepository.searchCertificates(q, status, pageable);
        }

        List<CertificateListItem> items = certPage.getContent().stream().map(c -> {
            Double avgScore = getAverageScoreForCertificate(c);

            return CertificateListItem.builder()
                    .certificateId(c.getCertificateId())
                    .studentName(c.getStudent().getFullName())
                    .studentEmail(c.getStudent().getEmail())
                    .className(c.getClassEntity().getClassCode())
                    .courseCode(c.getClassEntity().getCourse().getCourseCode())
                    .averageScore(avgScore)
                    .issuedAt(formatDate(c.getIssueDate()))
                    .status(c.getStatus().name().toLowerCase())
                    .verificationHash(c.getCertificateHash())
                    .build();
        }).collect(Collectors.toList());

        PaginationInfo pagination = PaginationInfo.builder()
                .page(page)
                .limit(limit)
                .total(certPage.getTotalElements())
                .totalPages(certPage.getTotalPages())
                .build();

        return CertificateListResponse.builder().items(items).pagination(pagination).build();
    }

    public RevokeResponse revokeCertificate(String certificateId, RevokeRequest request) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));

        User revoker = userQueryService.getByIdOrThrow(request.getRevokedBy());

        cert.setStatus(CertificateStatus.REVOKED);
        cert.setRevokedAt(LocalDateTime.now());
        cert.setRevokedBy(revoker);
        certificateRepository.save(cert);

        return RevokeResponse.builder()
                .certificateId(cert.getCertificateId())
                .status("revoked")
                .revokedAt(formatDateTime(cert.getRevokedAt()))
                .revokedByUserId(request.getRevokedBy())
                .reason(request.getReason())
                .build();
    }

    public CertificateDetail getCertificateDetail(String certificateId) {
        Certificate c = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));

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

        List<QuizAttempt> attempts =
                quizAttemptRepository.findByStudentAndClass(c.getStudent().getUserId(), c.getClassEntity().getClassId());

        List<QuizResult> quizResults = attempts.stream().map(qa ->
                QuizResult.builder()
                        .quizId(qa.getQuiz().getQuizId())
                        .quizName(qa.getQuiz().getTitle())
                        .score(qa.getScore())
                        .maxScore(10)
                        .completedAt(formatDateTime(qa.getSubmittedAt()))
                        .build()
        ).collect(Collectors.toList());

        Double avgScore = getAverageScoreForCertificate(c);

        return CertificateDetail.builder()
                .certificateId(c.getCertificateId())
                .studentId(c.getStudent().getUserId())
                .studentName(c.getStudent().getFullName())
                .studentEmail(c.getStudent().getEmail())
                .classId(c.getClassEntity().getClassId())
                .className(c.getClassEntity().getClassCode())
                .courseCode(c.getClassEntity().getCourse().getCourseCode())
                .courseName(c.getClassEntity().getCourse().getCourseName())
                .averageScore(avgScore)
                .issuedAt(formatDate(c.getIssueDate()))
                .status(c.getStatus().name().toLowerCase())
                .verificationHash(c.getCertificateHash())
                .blockchainInfo(bcInfo)
                .quizResults(quizResults)
                .build();
    }

    public CertificateVerificationResult verifyCertificate(String certificateId) {
        Certificate c = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));

        boolean isValid = c.getCertificateHash() != null && !c.getCertificateHash().isEmpty();

        if (!isValid) {
            throw new IllegalArgumentException("Certificate verification failed: Hash mismatch");
        }

        BlockchainVerificationInfo bcInfo = BlockchainVerificationInfo.builder()
                .transactionHash(c.getTransactionHash())
                .blockNumber(c.getBlockNumber() != null ? String.valueOf(c.getBlockNumber()) : null)
                .contractAddress(c.getContractAddress())
                .timestamp(formatDate(c.getIssueDate()))
                .status("confirmed")
                .build();

        return CertificateVerificationResult.builder()
                .certificateId(c.getCertificateId())
                .isValid(true)
                .verificationHash(c.getCertificateHash())
                .blockchainInfo(bcInfo)
                .verifiedAt(formatDateTime(LocalDateTime.now()))
                .build();
    }

    public UserListResponse getUsers(String roleStr, String statusStr, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);

        Long roleId = null;
        if (roleStr != null && !roleStr.isEmpty()) {
            RoleType roleType = RoleType.valueOf(roleStr.toUpperCase());
            roleId = roleRepository.findByRoleName(roleType)
                    .map(Role::getRoleId)
                    .orElse(null);
        }

        Boolean isActive = null;
        if ("active".equalsIgnoreCase(statusStr)) isActive = true;
        else if ("inactive".equalsIgnoreCase(statusStr)) isActive = false;

        Page<User> userPage = userQueryService.findUsersByFilter(roleId, isActive, pageable);

        List<UserListItem> items = userPage.getContent().stream().map(u -> {
            String roleName = u.getRole() != null ? u.getRole().getRoleName().name() : "STUDENT";

            return UserListItem.builder()
                    .userId(u.getUserId())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .role(roleName)
                    .avatarUrl(u.getAvatarUrl())
                    .isActive(u.getIsActive())
                    .createdAt(formatInstant(u.getCreatedAt()))
                    .lastLoginAt(formatInstant(u.getUpdatedAt()))
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

    public UpdateUserStatusResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {
        User user = userQueryService.getByIdOrThrow(userId);

        user.setIsActive(request.getIsActive());
        userRepository.save(user);

        return UpdateUserStatusResponse.builder()
                .userId(user.getUserId())
                .isActive(user.getIsActive())
                .updatedAt(formatDateTime(LocalDateTime.now()))
                .build();
    }

    private Double getAverageScoreForCertificate(Certificate cert) {
        return enrollmentRepository
                .findByStudent_UserIdAndClassEntity_ClassId(
                        cert.getStudent().getUserId(),
                        cert.getClassEntity().getClassId())
                .map(Enrollment::getFinalGrade)
                .orElse(null);
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay(ZoneOffset.UTC).format(ISO_FORMATTER);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toInstant(ZoneOffset.UTC).toString();
    }

    private String formatInstant(java.time.Instant instant) {
        if (instant == null) return null;
        return instant.toString();
    }
}
