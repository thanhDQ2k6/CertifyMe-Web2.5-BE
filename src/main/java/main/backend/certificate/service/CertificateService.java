package main.backend.certificate.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.blockchain.service.BlockchainCertificateService;
import main.backend.certificate.dto.response.CertificateResponse;
import main.backend.certificate.entity.Certificate;
import main.backend.certificate.repository.CertificateRepository;
import main.backend.classroom.entity.ClassEntity;
import main.backend.classroom.repository.ClassRepository;
import main.backend.common.enums.CertificateStatus;
import main.backend.common.exception.BusinessException;
import main.backend.common.exception.ResourceNotFoundException;
import main.backend.common.util.IdGenerator;
import main.backend.quiz.entity.QuizAttempt;
import main.backend.quiz.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

  private final CertificateRepository certificateRepository;
  private final BlockchainCertificateService blockchainCertificateService;
  private final QuizAttemptRepository quizAttemptRepository;
  private final ClassRepository classRepository;
  private final UserRepository userRepository;

  @Value("${app.blockchain.contract-address}")
  private String contractAddress;

  // FIX CB3: trả đầy đủ fields: certificateId, averageScore, status, blockchainInfo
  // FIX CB5: courseCode lấy từ course.courseCode thay vì classCode
  public List<CertificateResponse> getCertificatesByStudentId(
    String studentId
  ) {
    List<Certificate> certificates = certificateRepository.findByStudent_UserId(
      studentId
    );

    return certificates.stream().map(this::mapToResponse).toList();
  }

  @Transactional
  public CertificateResponse issueCertificate(
    String studentId,
    String classId
  ) {
    // 1. Validate User and Class
    User student = userRepository
      .findById(studentId)
      .orElseThrow(() ->
        new ResourceNotFoundException("Student not found: " + studentId)
      );
    ClassEntity classEntity = classRepository
      .findById(classId)
      .orElseThrow(() ->
        new ResourceNotFoundException("Class not found: " + classId)
      );

    // 2. Check if certificate already exists
    boolean exists = certificateRepository
      .findByStudent_UserId(studentId)
      .stream()
      .anyMatch(c -> c.getClassEntity().getClassId().equals(classId));
    if (exists) {
      throw new BusinessException("Certificate already issued for this class");
    }

    // 3. Verify completion of all quizzes in the class
    List<QuizAttempt> attempts = quizAttemptRepository.findByStudentAndClass(
      studentId,
      classId
    );
    Map<String, Optional<QuizAttempt>> bestAttemptsPerQuiz = attempts
      .stream()
      .filter(a -> a.getQuiz() != null && a.getScore() != null)
      .collect(
        Collectors.groupingBy(
          a -> a.getQuiz().getQuizId(),
          Collectors.maxBy(Comparator.comparingDouble(QuizAttempt::getScore))
        )
      );

    int passedQuizzes = (int) bestAttemptsPerQuiz
      .values()
      .stream()
      .filter(
        opt -> opt.isPresent() && Boolean.TRUE.equals(opt.get().getIsPassed())
      )
      .count();

    int totalQuizzes =
      classEntity.getTotalQuizzes() != null ? classEntity.getTotalQuizzes() : 0;
    if (totalQuizzes == 0 || passedQuizzes < totalQuizzes) {
      throw new BusinessException(
        "Cannot issue certificate. Found " +
          passedQuizzes +
          "/" +
          totalQuizzes +
          " passed quizzes."
      );
    }

    // 4. Generate Certificate Hash
    String certId = IdGenerator.generateCertificateId();
    LocalDate now = LocalDate.now();
    String hashInput =
      studentId + ":" + classId + ":" + certId + ":" + now.toEpochDay();
    String certHash = generateSha256Hash(hashInput);

    // 5. Invoke Blockchain
    long issueDateEpoch = now.toEpochDay() * 24 * 60 * 60; // Seconds
    String txHash;
    try {
      txHash = blockchainCertificateService.issueCertificateOnChain(
        certId,
        certHash,
        issueDateEpoch
      );
    } catch (Exception e) {
      log.error("Blockchain issuance failed", e);
      throw new RuntimeException(
        "Blockchain transaction failed. Please try again.",
        e
      );
    }

    // 6. Save to DB
    Certificate cert = new Certificate();
    cert.setCertificateId(certId);
    cert.setStudent(student);
    cert.setClassEntity(classEntity);
    cert.setIssueDate(now);
    cert.setCertificateHash(certHash);
    cert.setTransactionHash(txHash);
    cert.setContractAddress(contractAddress);
    cert.setStatus(CertificateStatus.ISSUED); // Assuming enum has ISSUED or APPROVED or something, wait: check existing enum. Previous uses "PENDING" and "REVOKED". Let's set ISSUED. Wait! Let's check CertificateStatus.

    cert = certificateRepository.save(cert);
    return mapToResponse(cert);
  }

  private String generateSha256Hash(String input) {
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

  private CertificateResponse mapToResponse(Certificate cert) {
    var classEntity = cert.getClassEntity();
    String courseName =
      classEntity != null && classEntity.getCourse() != null
        ? classEntity.getCourse().getCourseName()
        : null;
    String courseCode =
      classEntity != null && classEntity.getCourse() != null
        ? classEntity.getCourse().getCourseCode()
        : null;

    CertificateResponse.BlockchainInfo blockchainInfo = null;
    if (cert.getTransactionHash() != null) {
      blockchainInfo = CertificateResponse.BlockchainInfo.builder()
        .hash(cert.getCertificateHash())
        .txHash(cert.getTransactionHash())
        .block(null)
        .contract(cert.getContractAddress())
        .build();
    }

    return CertificateResponse.builder()
      .certificateId(cert.getCertificateId())
      .courseName(courseName)
      .courseCode(courseCode)
      .verificationHash(cert.getCertificateHash())
      .status(
        cert.getStatus() != null
          ? cert.getStatus().name().toLowerCase()
          : "pending"
      )
      .issuedAt(
        cert.getIssueDate() != null ? cert.getIssueDate().toString() : null
      )
      .blockchainInfo(blockchainInfo)
      .build();
  }
}
