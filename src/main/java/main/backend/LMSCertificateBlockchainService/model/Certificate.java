package main.backend.LMSCertificateBlockchainService.model;
import jakarta.persistence.*;
import lombok.*;
import main.backend.auth.entity.User;
import main.backend.LMSCourseService.model.CourseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    @Id
    @Column(name = "certificate_id")
    private String certificateId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private main.backend.LMSCourseService.model.ClassEntity classEntity;

    @Column(name = "expiration_date")
    private java.time.LocalDate expirationDate;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "contract_address")
    private String contractAddress;

    @Column(name = "issue_date")
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "certificate_hash", nullable = false, length = 66)
    private String certificateHash;

    @Column(name = "transaction_hash", length = 66)
    private String transactionHash;

    @Enumerated(EnumType.STRING)
    private CertificateStatus status = CertificateStatus.PENDING;

    public enum CertificateStatus { PENDING, ISSUED, REVOKED }

    @Column(name = "revoked_at")
    private java.time.LocalDateTime revokedAt;

    @ManyToOne
    @JoinColumn(name = "revoked_by")
    private User revokedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
}
