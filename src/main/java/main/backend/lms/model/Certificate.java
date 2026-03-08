package main.backend.lms.model;
import jakarta.persistence.*;
import lombok.*;
import main.backend.auth.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @JoinColumn(name = "class_id") // Sửa từ course_id thành class_id cho khớp với ảnh ông gửi
    private CourseEntity course;

    @Column(name = "issue_date")
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "certificate_hash", nullable = false, length = 66)
    private String certificateHash;

    @Column(name = "transaction_hash", length = 66)
    private String transactionHash;

    @Enumerated(EnumType.STRING)
    private CertificateStatus status = CertificateStatus.PENDING;

    public enum CertificateStatus { PENDING, ISSUED, REVOKED }
}
