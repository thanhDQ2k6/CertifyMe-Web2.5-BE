package main.backend.blockchain.service;

import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.backend.blockchain.contract.CertificateRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainCertificateService {

  private final Web3j web3j;
  private final Credentials credentials;

  @Value("${app.blockchain.contract-address}")
  private String contractAddress;

  private CertificateRegistry loadContract() {
    return CertificateRegistry.load(
      contractAddress,
      web3j,
      credentials,
      new DefaultGasProvider()
    );
  }

  public String issueCertificateOnChain(
    String certId,
    String certHash,
    long issueDateEpochSeconds
  ) {
    try {
      CertificateRegistry contract = loadContract();
      BigInteger issueDate = BigInteger.valueOf(issueDateEpochSeconds);
      log.info(
        "Issuing certificate on blockchain... certId={}, hash={}",
        certId,
        certHash
      );
      TransactionReceipt receipt = contract
        .issueCertificate(certId, certHash, issueDate)
        .send();
      String hash = receipt.getTransactionHash();
      log.info("Transaction complete! Hash: {}", hash);
      return hash;
    } catch (Exception e) {
      log.error("Failed to issue certificate on blockchain", e);
      throw new RuntimeException("Blockchain transaction failed", e);
    }
  }

  public String revokeCertificateOnChain(String certId) {
    try {
      CertificateRegistry contract = loadContract();
      log.info("Revoking certificate on blockchain... certId={}", certId);
      TransactionReceipt receipt = contract.revokeCertificate(certId).send();
      String hash = receipt.getTransactionHash();
      log.info("Transaction complete! Hash: {}", hash);
      return hash;
    } catch (Exception e) {
      log.error("Failed to revoke certificate on blockchain", e);
      throw new RuntimeException("Blockchain transaction failed", e);
    }
  }

  public OnChainCertificate getCertificateOnChain(String certId) {
    try {
      CertificateRegistry contract = loadContract();
      Tuple4<String, String, BigInteger, Boolean> data = contract
        .certificates(certId)
        .send();

      return new OnChainCertificate(
        data.component1(),
        data.component2(),
        data.component3(),
        Boolean.TRUE.equals(data.component4())
      );
    } catch (Exception e) {
      log.error(
        "Failed to load certificate from blockchain: certId={}",
        certId,
        e
      );
      throw new RuntimeException("Blockchain read failed", e);
    }
  }

  public static class OnChainCertificate {

    private final String certId;
    private final String certHash;
    private final BigInteger issueDate;
    private final boolean valid;

    public OnChainCertificate(
      String certId,
      String certHash,
      BigInteger issueDate,
      boolean valid
    ) {
      this.certId = certId;
      this.certHash = certHash;
      this.issueDate = issueDate;
      this.valid = valid;
    }

    public String getCertId() {
      return certId;
    }

    public String getCertHash() {
      return certHash;
    }

    public BigInteger getIssueDate() {
      return issueDate;
    }

    public boolean isValid() {
      return valid;
    }
  }

  private static class DefaultGasProvider extends StaticGasProvider {

    public DefaultGasProvider() {
      // Using 20 Gwei for gas price and 3,000,000 for gas limit
      super(
        BigInteger.valueOf(20_000_000_000L),
        BigInteger.valueOf(3_000_000L)
      );
    }
  }
}
