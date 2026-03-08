package main.backend.lms.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    private String courseName;
    private String courseCode;
    private String certHash;
    private String certDate;

    // THÊM DUY NHẤT CÁI NÀY ĐỂ TRẢ VỀ JSON LỒNG NHAU
    private BlockchainInfo blockchainInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockchainInfo {
        private String hash;
        private String block;
        private String txHash;
        private String contract;
    }
}