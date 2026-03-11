package main.backend.LMSCertificateBlockchainService.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

public class AdminResponseDTO {

    @Data @Builder
    public static class CertificateStats {
        private Long totalCertificates;
        private Long issuedCertificates;
        private Long revokedCertificates;
        private Long certificatesThisMonth;
        private Long certificatesThisYear;
    }

    // phân trang
    @Data @Builder
    public static class PaginationInfo {
        private int page;
        private int limit;
        private long total;
        private int totalPages;
    }

    // danh sách chứng chỉ
    @Data @Builder
    public static class CertificateListResponse {
        private List<CertificateListItem> items;
        private PaginationInfo pagination;
    }

    @Data @Builder
    public static class CertificateListItem {
        private String certificateId;
        private String studentName;
        private String studentEmail;
        private String className;
        private String courseCode;
        private Double averageScore;
        private String issuedAt;
        private String status;
        private String verificationHash;
    }

    @Data
    public static class RevokeRequest {
        private String reason;
        private String revokedBy;
    }

    @Data
    public static class UpdateUserStatusRequest {
        private Boolean isActive;
        private String reason;
    }

    @Data @Builder
    public static class CertificateDetail {
        private String certificateId;
        private String studentId;
        private String studentName;
        private String studentEmail;
        private String classId;
        private String className;
        private String courseCode;
        private String courseName;
        private Double averageScore;
        private String issuedAt;
        private String status;
        private String verificationHash;
        private BlockchainInfo blockchainInfo;
        private List<QuizResult> quizResults;
    }

    @Data @Builder
    public static class BlockchainInfo {
        private String transactionHash;
        private String blockNumber;
        private String contractAddress;
        private String networkName;
        private String explorerUrl;
    }

    @Data @Builder
    public static class QuizResult {
        private String quizId;
        private String quizName;
        private Double score;
        private Integer maxScore;
        private String completedAt;
    }

    // (Verify)
    @Data @Builder
    public static class CertificateVerificationResult {
        private String certificateId;
        private Boolean isValid;
        private String verificationHash;
        private BlockchainVerificationInfo blockchainInfo;
        private String verifiedAt;
    }

    @Data @Builder
    public static class BlockchainVerificationInfo {
        private String transactionHash;
        private String blockNumber;
        private String contractAddress;
        private String timestamp;
        private String status;
    }

    //Get Users
    @Data @Builder
    public static class UserListResponse {
        private List<UserListItem> items;
        private PaginationInfo pagination;
    }

    @Data @Builder
    public static class UserListItem {
        private String userId;
        private String fullName;
        private String email;
        private String role;
        private String avatarUrl;
        private Boolean isActive;
        private String createdAt;
        private String lastLoginAt;
    }

    // User Status
    @Data @Builder
    public static class UpdateUserStatusResponse {
        private String userId;
        private Boolean isActive;
        private String updatedAt;
    }
}