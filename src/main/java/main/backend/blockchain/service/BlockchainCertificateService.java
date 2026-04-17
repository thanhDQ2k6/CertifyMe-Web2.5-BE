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
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainCertificateService {

  private final Web3j web3j;
  private final Credentials credentials;

  @Value("${app.blockchain.contract.certificate-address}")
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
