package main.backend.common.enums;

public enum CertificateStatus {
    PENDING, // Chờ duyệt cấp bằng
    ISSUED,  // Đã cấp bằng lên Blockchain
    REVOKED  // Bằng đã bị thu hồi (Admin thực hiện)
}
