package main.backend.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.backend.admin.dto.response.AdminResponseDTO.*;
import main.backend.admin.service.AdminService;
import main.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/admin/certificates/stats")
    public ResponseEntity<ApiResponse<CertificateStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getCertificateStats()));
    }

    @GetMapping("/certificates/recent")
    public ResponseEntity<ApiResponse<CertificateListResponse>> getRecentCertificates(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getCertificates(null, null, page, limit)));
    }

    @GetMapping("/certificates/search")
    public ResponseEntity<ApiResponse<CertificateListResponse>> searchCertificates(
            @RequestParam String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getCertificates(q, status, 1, limit)));
    }

    @PostMapping("/certificates/{certificateId}/revoke")
    public ResponseEntity<ApiResponse<RevokeResponse>> revokeCertificate(
            @PathVariable String certificateId,
            @Valid @RequestBody RevokeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.revokeCertificate(certificateId, request)));
    }

    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<ApiResponse<CertificateDetail>> getCertificateDetail(@PathVariable String certificateId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getCertificateDetail(certificateId)));
    }

    @PostMapping("/certificates/{certificateId}/verify")
    public ResponseEntity<ApiResponse<CertificateVerificationResult>> verifyCertificate(@PathVariable String certificateId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.verifyCertificate(certificateId)));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<UserListResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUsers(role, status, page, limit)));
    }

    @PutMapping("/admin/users/{userId}/status")
    public ResponseEntity<ApiResponse<UpdateUserStatusResponse>> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUserStatus(userId, request)));
    }
}
