package main.backend.LMSCertificateBlockchainService.repository;
import main.backend.LMSCertificateBlockchainService.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, String> {
    List<Certificate> findByStudent_UserId(String studentId);
}