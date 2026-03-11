package main.backend.LMSCertificateBlockchainService.controller;

import lombok.RequiredArgsConstructor;
import main.backend.LMSCertificateBlockchainService.dto.response.AdminResponseDTO.*;
import main.backend.LMSCertificateBlockchainService.service.AdminService;
import main.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    //Lấy thống kê
    @GetMapping("/admin/certificates/stats")
    public ResponseEntity<ApiResponse<CertificateStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Success", adminService.getCertificateStats()));
    }

    // Lấy danh sách gần đây
    @GetMapping("/certificates/recent")
    public ResponseEntity<ApiResponse<CertificateListResponse>> getRecentCertificates(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(ApiResponse.success("Success", adminService.getCertificates(null, null, page, limit)));
    }

    //Tìm kiếm
    @GetMapping("/certificates/search")
    public ResponseEntity<ApiResponse<CertificateListResponse>> searchCertificates(
            @RequestParam String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit) {

        // Tái sử dụng hàm getCertificates để trả ra kết quả có phân trang luôn
        return ResponseEntity.ok(ApiResponse.success("Success", adminService.getCertificates(q, status, 1, limit)));
    }

    //Thu hồi chứng chỉ
    @PostMapping("/certificates/{certificateId}/revoke")
    public ResponseEntity<ApiResponse<Object>> revokeCertificate(
            @PathVariable String certificateId,
            @RequestBody RevokeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Certificate revoked successfully", adminService.revokeCertificate(certificateId, request)));
    }

    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<ApiResponse<CertificateDetail>> getCertificateDetail(@PathVariable String certificateId) {
        return ResponseEntity.ok(ApiResponse.success("Success", adminService.getCertificateDetail(certificateId)));
    }

    @PostMapping("/certificates/{certificateId}/verify")
    public ResponseEntity<?> verifyCertificate(@PathVariable String certificateId) {
        try {
            CertificateVerificationResult result = adminService.verifyCertificate(certificateId);
            return ResponseEntity.ok(ApiResponse.success("Certificate verified successfully on blockchain", result));
        } catch (IllegalArgumentException e) {
            // Trả về lỗi 400 Bad Request nếu Hash sai
            ApiResponse<Object> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setData(null);
            errorResponse.setMessage(null);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    //Lấy danh sách users
    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<UserListResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Success", adminService.getUsers(role, status, page, limit)));
    }

    //Cập nhật trạng thái user
    @PutMapping("/admin/users/{userId}/status")
    public ResponseEntity<ApiResponse<UpdateUserStatusResponse>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", adminService.updateUserStatus(userId, request)));
    }
}

