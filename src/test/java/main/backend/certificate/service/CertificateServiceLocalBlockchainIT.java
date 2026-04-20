package main.backend.certificate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.blockchain.contract.CertificateRegistry;
import main.backend.blockchain.service.BlockchainCertificateService;
import main.backend.certificate.dto.response.CertificateResponse;
import main.backend.certificate.entity.Certificate;
import main.backend.certificate.repository.CertificateRepository;
import main.backend.classroom.entity.ClassEntity;
import main.backend.classroom.repository.ClassRepository;
import main.backend.quiz.entity.Quiz;
import main.backend.quiz.entity.QuizAttempt;
import main.backend.quiz.repository.QuizAttemptRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

/**
 * Manual integration test:
 * - Repositories are mocked (no DB needed).
 * - Blockchain calls are real against local Hardhat node.
 *
 * The class name ends with IT, so it won't run in the default `mvn test` cycle.
 */
@ExtendWith(MockitoExtension.class)
public class CertificateServiceLocalBlockchainIT {

  private static final String LOCAL_RPC_URL = "http://127.0.0.1:8545";
  private static final String HARDHAT_DEFAULT_PRIVATE_KEY =
    "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";
  private static final BigInteger GAS_PRICE = BigInteger.valueOf(
    20_000_000_000L
  );
  private static final BigInteger GAS_LIMIT = BigInteger.valueOf(3_000_000L);
  private static final int RPC_READY_TIMEOUT_SECONDS = 30;

  private static Web3j web3j;
  private static Process hardhatProcess;
  private static boolean hardhatStartedByTest;
  private static String deployedContractAddress;
  private static BlockchainCertificateService realBlockchainCertificateService;

  @Mock
  private CertificateRepository certificateRepository;

  @Mock
  private QuizAttemptRepository quizAttemptRepository;

  @Mock
  private ClassRepository classRepository;

  @Mock
  private UserRepository userRepository;

  private CertificateService certificateService;

  @BeforeAll
  static void setupLocalBlockchain() throws Exception {
    web3j = Web3j.build(new HttpService(LOCAL_RPC_URL));

    if (!isRpcReady()) {
      startHardhatNode();
      hardhatStartedByTest = true;
      waitForRpcReady();
    }

    Credentials credentials = Credentials.create(HARDHAT_DEFAULT_PRIVATE_KEY);
    StaticGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);

    CertificateRegistry deployed = CertificateRegistry.deploy(
      web3j,
      credentials,
      gasProvider
    ).send();
    deployedContractAddress = deployed.getContractAddress();

    realBlockchainCertificateService = new BlockchainCertificateService(
      web3j,
      credentials
    );
    ReflectionTestUtils.setField(
      realBlockchainCertificateService,
      "contractAddress",
      deployedContractAddress
    );
  }

  @AfterAll
  static void tearDownLocalBlockchain() {
    if (web3j != null) {
      web3j.shutdown();
    }

    if (
      hardhatStartedByTest && hardhatProcess != null && hardhatProcess.isAlive()
    ) {
      hardhatProcess.destroy();
      if (hardhatProcess.isAlive()) {
        hardhatProcess.destroyForcibly();
      }
    }
  }

  @BeforeEach
  void setUp() {
    certificateService = new CertificateService(
      certificateRepository,
      realBlockchainCertificateService,
      quizAttemptRepository,
      classRepository,
      userRepository
    );

    ReflectionTestUtils.setField(
      certificateService,
      "contractAddress",
      deployedContractAddress
    );
  }

  @Test
  @Timeout(60)
  void issueCertificate_withMockedRepositories_writesToLocalBlockchain() {
    User student = new User();
    student.setUserId("student-123");

    ClassEntity classEntity = new ClassEntity();
    classEntity.setClassId("class-456");
    classEntity.setTotalQuizzes(2);

    Quiz quiz1 = new Quiz();
    quiz1.setQuizId("quiz-1");
    QuizAttempt attempt1 = new QuizAttempt();
    attempt1.setQuiz(quiz1);
    attempt1.setScore(8.5);
    attempt1.setIsPassed(true);

    Quiz quiz2 = new Quiz();
    quiz2.setQuizId("quiz-2");
    QuizAttempt attempt2 = new QuizAttempt();
    attempt2.setQuiz(quiz2);
    attempt2.setScore(9.0);
    attempt2.setIsPassed(true);

    when(userRepository.findById("student-123")).thenReturn(
      Optional.of(student)
    );
    when(classRepository.findById("class-456")).thenReturn(
      Optional.of(classEntity)
    );
    when(certificateRepository.findByStudent_UserId("student-123")).thenReturn(
      Collections.emptyList()
    );
    when(
      quizAttemptRepository.findByStudentAndClass("student-123", "class-456")
    ).thenReturn(List.of(attempt1, attempt2));
    when(certificateRepository.save(any(Certificate.class))).thenAnswer(
      invocation -> invocation.getArgument(0)
    );

    CertificateResponse response = certificateService.issueCertificate(
      "student-123",
      "class-456"
    );

    assertNotNull(response);
    assertEquals("issued", response.getStatus());

    ArgumentCaptor<Certificate> certCaptor = ArgumentCaptor.forClass(
      Certificate.class
    );
    verify(certificateRepository).save(certCaptor.capture());

    Certificate savedCert = certCaptor.getValue();
    assertNotNull(savedCert.getTransactionHash());
    assertTrue(savedCert.getTransactionHash().startsWith("0x"));
    assertEquals(deployedContractAddress, savedCert.getContractAddress());

    BlockchainCertificateService.OnChainCertificate onChain =
      realBlockchainCertificateService.getCertificateOnChain(
        savedCert.getCertificateId()
      );

    assertEquals(savedCert.getCertificateId(), onChain.getCertId());
    assertEquals(
      savedCert.getCertificateHash().toLowerCase(Locale.ROOT),
      onChain.getCertHash().toLowerCase(Locale.ROOT)
    );
    assertTrue(onChain.isValid());
  }

  private static boolean isRpcReady() {
    try {
      web3j.ethChainId().send();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static void waitForRpcReady() throws InterruptedException {
    long deadline =
      System.currentTimeMillis() + (RPC_READY_TIMEOUT_SECONDS * 1000L);

    while (System.currentTimeMillis() < deadline) {
      if (isRpcReady()) {
        return;
      }
      Thread.sleep(500);
    }

    throw new IllegalStateException(
      "Local Hardhat node did not become ready on " + LOCAL_RPC_URL
    );
  }

  private static void startHardhatNode() throws IOException {
    String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    String npxCommand = os.contains("win") ? "npx.cmd" : "npx";

    Path hardhatDir = Paths.get("blockchain").toAbsolutePath().normalize();
    ProcessBuilder pb = new ProcessBuilder(
      npxCommand,
      "hardhat",
      "node",
      "--hostname",
      "127.0.0.1",
      "--port",
      "8545"
    );

    pb.directory(hardhatDir.toFile());
    pb.redirectErrorStream(true);
    hardhatProcess = pb.start();

    Thread outputDrainer = new Thread(
      CertificateServiceLocalBlockchainIT::drainHardhatOutput,
      "hardhat-node-output-drainer"
    );
    outputDrainer.setDaemon(true);
    outputDrainer.start();
  }

  private static void drainHardhatOutput() {
    if (hardhatProcess == null) {
      return;
    }

    try (
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(
          hardhatProcess.getInputStream(),
          StandardCharsets.UTF_8
        )
      )
    ) {
      while (reader.readLine() != null) {
        // Keep draining output so the Hardhat process doesn't block.
      }
    } catch (IOException ignored) {}
  }
}
