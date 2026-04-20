package main.backend.certificate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.blockchain.service.BlockchainCertificateService;
import main.backend.certificate.dto.response.CertificateResponse;
import main.backend.certificate.entity.Certificate;
import main.backend.certificate.repository.CertificateRepository;
import main.backend.classroom.entity.ClassEntity;
import main.backend.classroom.repository.ClassRepository;
import main.backend.common.exception.BusinessException;
import main.backend.common.exception.ResourceNotFoundException;
import main.backend.quiz.entity.Quiz;
import main.backend.quiz.entity.QuizAttempt;
import main.backend.quiz.repository.QuizAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceTest {

  @Mock
  private CertificateRepository certificateRepository;

  @Mock
  private BlockchainCertificateService blockchainCertificateService;

  @Mock
  private QuizAttemptRepository quizAttemptRepository;

  @Mock
  private ClassRepository classRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CertificateService certificateService;

  private User student;
  private ClassEntity classEntity;

  @BeforeEach
  void setUp() {
    // Mock @Value annotation injection
    ReflectionTestUtils.setField(
      certificateService,
      "contractAddress",
      "0xDummyContractAddress"
    );

    student = new User();
    student.setUserId("student-123");

    classEntity = new ClassEntity();
    classEntity.setClassId("class-456");
    classEntity.setTotalQuizzes(2);
  }

  @Test
  void issueCertificate_Success() {
    // Arrange
    when(userRepository.findById("student-123")).thenReturn(
      Optional.of(student)
    );
    when(classRepository.findById("class-456")).thenReturn(
      Optional.of(classEntity)
    );
    when(certificateRepository.findByStudent_UserId("student-123")).thenReturn(
      Collections.emptyList()
    );

    // Mock quiz attempts (2 passed quizzes)
    Quiz quiz1 = new Quiz();
    quiz1.setQuizId("quiz-1");
    QuizAttempt attempt1 = new QuizAttempt();
    attempt1.setQuiz(quiz1);
    attempt1.setScore(8.0);
    attempt1.setIsPassed(true);

    Quiz quiz2 = new Quiz();
    quiz2.setQuizId("quiz-2");
    QuizAttempt attempt2 = new QuizAttempt();
    attempt2.setQuiz(quiz2);
    attempt2.setScore(9.0);
    attempt2.setIsPassed(true);

    when(
      quizAttemptRepository.findByStudentAndClass("student-123", "class-456")
    ).thenReturn(List.of(attempt1, attempt2));

    when(
      blockchainCertificateService.issueCertificateOnChain(
        anyString(),
        anyString(),
        anyLong()
      )
    ).thenReturn("0xDummyTxHash");

    when(certificateRepository.save(any(Certificate.class))).thenAnswer(
      invocation -> invocation.getArgument(0)
    );

    // Act
    CertificateResponse response = certificateService.issueCertificate(
      "student-123",
      "class-456"
    );

    // Assert
    assertNotNull(response);
    assertEquals("issued", response.getStatus()); // Note: CertificateStatus.ISSUED is lowercase in MapToResponse

    // Verify blockchain call
    verify(blockchainCertificateService, times(1)).issueCertificateOnChain(
      anyString(),
      anyString(),
      anyLong()
    );

    // Verify DB save
    ArgumentCaptor<Certificate> certCaptor = ArgumentCaptor.forClass(
      Certificate.class
    );
    verify(certificateRepository).save(certCaptor.capture());
    Certificate savedCert = certCaptor.getValue();
    assertEquals("0xDummyTxHash", savedCert.getTransactionHash());
    assertNotNull(savedCert.getCertificateId());
    assertNotNull(savedCert.getCertificateHash());
  }

  @Test
  void issueCertificate_Fail_StudentNotFound() {
    // Arrange
    when(userRepository.findById("invalid-student")).thenReturn(
      Optional.empty()
    );

    // Act & Assert
    Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
      certificateService.issueCertificate("invalid-student", "class-456");
    });
    assertTrue(exception.getMessage().contains("Student not found"));
  }

  @Test
  void issueCertificate_Fail_CertificateAlreadyExists() {
    // Arrange
    when(userRepository.findById("student-123")).thenReturn(
      Optional.of(student)
    );
    when(classRepository.findById("class-456")).thenReturn(
      Optional.of(classEntity)
    );

    Certificate existingCert = new Certificate();
    existingCert.setClassEntity(classEntity);

    when(certificateRepository.findByStudent_UserId("student-123")).thenReturn(
      List.of(existingCert)
    );

    // Act & Assert
    Exception exception = assertThrows(BusinessException.class, () -> {
      certificateService.issueCertificate("student-123", "class-456");
    });
    assertTrue(exception.getMessage().contains("Certificate already issued"));
  }

  @Test
  void issueCertificate_Fail_NotPassedAllQuizzes() {
    // Arrange
    when(userRepository.findById("student-123")).thenReturn(
      Optional.of(student)
    );
    when(classRepository.findById("class-456")).thenReturn(
      Optional.of(classEntity)
    );
    when(certificateRepository.findByStudent_UserId("student-123")).thenReturn(
      Collections.emptyList()
    );

    // Mock only 1 passed quiz instead of 2 required
    Quiz quiz1 = new Quiz();
    quiz1.setQuizId("quiz-1");
    QuizAttempt attempt1 = new QuizAttempt();
    attempt1.setQuiz(quiz1);
    attempt1.setScore(8.0);
    attempt1.setIsPassed(true);

    when(
      quizAttemptRepository.findByStudentAndClass("student-123", "class-456")
    ).thenReturn(List.of(attempt1));

    // Act & Assert
    Exception exception = assertThrows(BusinessException.class, () -> {
      certificateService.issueCertificate("student-123", "class-456");
    });
    assertTrue(
      exception
        .getMessage()
        .contains("Cannot issue certificate. Found 1/2 passed quizzes.")
    );
  }
}
