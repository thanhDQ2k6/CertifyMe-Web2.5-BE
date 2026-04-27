package main.backend.certificate.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

public class PublicCertificateResponseDTO {

  @Data
  @Builder
  public static class PaginationInfo {

    private int page;
    private int limit;
    private long total;
    private int totalPages;
  }

  @Data
  @Builder
  public static class PublicStudentItem {

    private String studentId;
    private String studentCode;
    private String studentName;
    private String avatarUrl;
  }

  @Data
  @Builder
  public static class PublicStudentSearchResponse {

    private List<PublicStudentItem> items;
    private PaginationInfo pagination;
  }

  @Data
  @Builder
  public static class PublicCertificateItem {

    private String certificateId;
    private String classId;
    private String classCode;
    private String courseCode;
    private String courseName;
    private String issuedAt;
    private String status;
    private String verificationHash;
    private String transactionHash;
    private String contractAddress;
  }

  @Data
  @Builder
  public static class PublicStudentDetailResponse {

    private PublicStudentItem student;
    private List<PublicCertificateItem> certificates;
  }

  @Data
  @Builder
  public static class BlockchainInfo {

    private String transactionHash;
    private String contractAddress;
    private String networkName;
    private String explorerUrl;
    private String onChainStatus;
  }

  @Data
  @Builder
  public static class PublicCertificateVerificationResponse {

    private String certificateId;
    private Boolean isValid;
    private String verificationHash;
    private String verificationStatus;
    private String verifiedAt;
    private PublicStudentItem student;
    private PublicCertificateItem certificate;
    private BlockchainInfo blockchainInfo;
  }
}
