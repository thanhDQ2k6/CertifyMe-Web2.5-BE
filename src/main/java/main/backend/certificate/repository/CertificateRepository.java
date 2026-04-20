package main.backend.certificate.repository;

import java.util.List;
import main.backend.certificate.entity.Certificate;
import main.backend.common.enums.CertificateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CertificateRepository
  extends JpaRepository<Certificate, String>
{
  @Query(
    "SELECT COUNT(c) FROM Certificate c WHERE YEAR(c.issueDate) = YEAR(CURRENT_DATE) AND MONTH(c.issueDate) = MONTH(CURRENT_DATE)"
  )
  long countCertificatesThisMonth();

  @Query(
    "SELECT COUNT(c) FROM Certificate c WHERE YEAR(c.issueDate) = YEAR(CURRENT_DATE)"
  )
  long countCertificatesThisYear();

  List<Certificate> findByStudent_UserId(String studentId);

  long countByStatus(CertificateStatus status);

  @Query(
    "SELECT c FROM Certificate c WHERE " +
      "(:status IS NULL OR c.status = :status) AND " +
      "(:q IS NULL OR LOWER(c.student.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
      "OR LOWER(c.student.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
      "OR LOWER(c.classEntity.classCode) LIKE LOWER(CONCAT('%', :q, '%')) " +
      "OR LOWER(c.certificateHash) LIKE LOWER(CONCAT('%', :q, '%')))"
  )
  Page<Certificate> searchCertificates(
    @Param("q") String q,
    @Param("status") CertificateStatus status,
    Pageable pageable
  );
}
