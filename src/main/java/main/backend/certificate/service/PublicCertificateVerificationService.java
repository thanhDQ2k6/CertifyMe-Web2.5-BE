package main.backend.certificate.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import main.backend.auth.repository.UserRepository;
import main.backend.blockchain.service.BlockchainCertificateService;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.BlockchainInfo;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.PaginationInfo;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.PublicCertificateItem;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.PublicCertificateVerificationResponse;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.PublicStudentDetailResponse;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.PublicStudentItem;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.PublicStudentSearchResponse;
import main.backend.certificate.entity.Certificate;
import main.backend.certificate.repository.CertificateRepository;
import main.backend.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicCertificateVerificationService {

  private final UserRepository userRepository;
  private final CertificateRepository certificateRepository;
  private final BlockchainCertificateService blockchainCertificateService;

  @Value("${app.blockchain.network:sepolia}")
  private String blockchainNetwork;

  @Value("${app.blockchain.explorer-base-url:https://sepolia.etherscan.io/tx/}")
  private String blockchainExplorerBaseUrl;

  private static final DateTimeFormatter ISO_FORMATTER =
    DateTimeFormatter.ISO_INSTANT;

  public PublicStudentSearchResponse searchStudents(
    String keyword,
    int page,
    int limit
  ) {
    int safePage = Math.max(page, 1);
    int safeLimit = Math.min(Math.max(limit, 1), 100);
    Pageable pageable = PageRequest.of(safePage - 1, safeLimit);

    String normalizedKeyword = keyword != null ? keyword.trim() : "";

    Page<User> students = userRepository.searchPublicStudents(
      normalizedKeyword,
      RoleType.STUDENT,
      pageable
    );

    List<PublicStudentItem> items = students
      .getContent()
      .stream()
      .map(this::toPublicStudentItem)
      .toList();

    return PublicStudentSearchResponse.builder()
      .items(items)
      .pagination(
        PaginationInfo.builder()
          .page(safePage)
          .limit(safeLimit)
          .total(students.getTotalElements())
          .totalPages(students.getTotalPages())
          .build()
      )
      .build();
  }

  public PublicStudentDetailResponse getStudentDetail(String studentCode) {
    User student = userRepository
      .findByUserCode(studentCode)
      .filter(user -> user.getRole() != null)
      .filter(user -> user.getRole().getRoleName() == RoleType.STUDENT)
      .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
      .orElseThrow(() ->
        new ResourceNotFoundException("Student not found: " + studentCode)
      );

    List<Certificate> certificates = certificateRepository.findByStudent_UserId(
      student.getUserId()
    );

    certificates.sort(
      Comparator.comparing(
        Certificate::getIssueDate,
        Comparator.nullsLast(Comparator.reverseOrder())
      ).thenComparing(
        Certificate::getCreatedAt,
        Comparator.nullsLast(Comparator.reverseOrder())
      )
    );

    List<PublicCertificateItem> certItems = certificates
      .stream()
      .map(this::toPublicCertificateItem)
      .toList();

    return PublicStudentDetailResponse.builder()
      .student(toPublicStudentItem(student))
      .certificates(certItems)
      .build();
  }

  public PublicCertificateVerificationResponse verifyCertificate(
    String certificateId
  ) {
    Certificate certificate = certificateRepository
      .findById(certificateId)
      .orElseThrow(() ->
        new ResourceNotFoundException("Certificate not found: " + certificateId)
      );

    String dbHash = normalizeHash(certificate.getCertificateHash());
    String expectedHash = normalizeHash(
      generateCertificateHashFromDbData(certificate)
    );
    boolean dbHashMatches = dbHash != null && dbHash.equals(expectedHash);

    BlockchainCertificateService.OnChainCertificate onChainCert =
      blockchainCertificateService.getCertificateOnChain(certificateId);
    String onChainHash = normalizeHash(onChainCert.getCertHash());

    boolean chainRecordMatchesId = certificateId.equals(
      onChainCert.getCertId()
    );
    boolean chainHashMatches = dbHash != null && dbHash.equals(onChainHash);
    boolean onChainValid = onChainCert.isValid();

    boolean isValid =
      dbHashMatches && chainRecordMatchesId && chainHashMatches && onChainValid;

    String verificationStatus;
    if (isValid) {
      verificationStatus = "confirmed";
    } else if (
      dbHashMatches && chainRecordMatchesId && chainHashMatches && !onChainValid
    ) {
      verificationStatus = "revoked";
    } else {
      verificationStatus = "mismatch";
    }

    User student = certificate.getStudent();

    return PublicCertificateVerificationResponse.builder()
      .certificateId(certificate.getCertificateId())
      .isValid(isValid)
      .verificationHash(certificate.getCertificateHash())
      .verificationStatus(verificationStatus)
      .verifiedAt(formatDateTime(LocalDateTime.now()))
      .student(student != null ? toPublicStudentItem(student) : null)
      .certificate(toPublicCertificateItem(certificate))
      .blockchainInfo(
        BlockchainInfo.builder()
          .transactionHash(certificate.getTransactionHash())
          .contractAddress(certificate.getContractAddress())
          .networkName(formatNetworkName(blockchainNetwork))
          .explorerUrl(buildExplorerUrl(certificate.getTransactionHash()))
          .onChainStatus(verificationStatus)
          .build()
      )
      .build();
  }

  private PublicStudentItem toPublicStudentItem(User user) {
    return PublicStudentItem.builder()
      .studentId(user.getUserId())
      .studentCode(user.getUserCode())
      .studentName(user.getFullName())
      .avatarUrl(user.getAvatarUrl())
      .build();
  }

  private PublicCertificateItem toPublicCertificateItem(
    Certificate certificate
  ) {
    var classEntity = certificate.getClassEntity();
    var course =
      classEntity != null && classEntity.getCourse() != null
        ? classEntity.getCourse()
        : null;

    return PublicCertificateItem.builder()
      .certificateId(certificate.getCertificateId())
      .classId(classEntity != null ? classEntity.getClassId() : null)
      .classCode(classEntity != null ? classEntity.getClassCode() : null)
      .courseCode(course != null ? course.getCourseCode() : null)
      .courseName(course != null ? course.getCourseName() : null)
      .issuedAt(
        certificate.getIssueDate() != null
          ? certificate
              .getIssueDate()
              .atStartOfDay(ZoneOffset.UTC)
              .format(ISO_FORMATTER)
          : null
      )
      .status(
        certificate.getStatus() != null
          ? certificate.getStatus().name().toLowerCase(Locale.ROOT)
          : null
      )
      .verificationHash(certificate.getCertificateHash())
      .transactionHash(certificate.getTransactionHash())
      .contractAddress(certificate.getContractAddress())
      .build();
  }

  private String generateCertificateHashFromDbData(Certificate certificate) {
    if (
      certificate.getStudent() == null ||
      certificate.getClassEntity() == null ||
      certificate.getIssueDate() == null
    ) {
      return null;
    }

    String hashInput =
      certificate.getStudent().getUserId() +
      ":" +
      certificate.getClassEntity().getClassId() +
      ":" +
      certificate.getCertificateId() +
      ":" +
      certificate.getIssueDate().toEpochDay();

    return generateSha256Hash(hashInput);
  }

  private String generateSha256Hash(String input) {
    if (input == null || input.isBlank()) {
      return null;
    }

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedHash = digest.digest(
        input.getBytes(StandardCharsets.UTF_8)
      );
      StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
      hexString.append("0x");
      for (byte b : encodedHash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate certificate hash", e);
    }
  }

  private String normalizeHash(String hash) {
    if (hash == null || hash.isBlank()) {
      return null;
    }
    return hash.trim().toLowerCase(Locale.ROOT);
  }

  private String formatNetworkName(String network) {
    if (network == null || network.isBlank()) {
      return "Ethereum Sepolia";
    }

    String normalized = network.trim().toLowerCase(Locale.ROOT);
    if ("sepolia".equals(normalized)) {
      return "Ethereum Sepolia";
    }
    if ("mainnet".equals(normalized) || "ethereum".equals(normalized)) {
      return "Ethereum Mainnet";
    }

    return network;
  }

  private String buildExplorerUrl(String transactionHash) {
    if (transactionHash == null || transactionHash.isBlank()) {
      return null;
    }

    String base =
      blockchainExplorerBaseUrl != null
        ? blockchainExplorerBaseUrl.trim()
        : "https://sepolia.etherscan.io/tx/";

    if (!base.endsWith("/")) {
      base = base + "/";
    }

    return base + transactionHash;
  }

  private String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.toInstant(ZoneOffset.UTC).toString();
  }
}
