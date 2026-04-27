package main.backend.certificate.controller;

import lombok.RequiredArgsConstructor;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.PublicCertificateVerificationResponse;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.PublicStudentDetailResponse;
import main.backend.certificate.dto.response.PublicCertificateResponseDTO.PublicStudentSearchResponse;
import main.backend.certificate.service.PublicCertificateVerificationService;
import main.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCertificateController {

  private final PublicCertificateVerificationService publicCertificateVerificationService;

  @GetMapping("/students/search")
  public ResponseEntity<
    ApiResponse<PublicStudentSearchResponse>
  > searchStudents(
    @RequestParam(defaultValue = "") String q,
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int limit
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(
        publicCertificateVerificationService.searchStudents(q, page, limit)
      )
    );
  }

  @GetMapping("/students/{studentCode}")
  public ResponseEntity<
    ApiResponse<PublicStudentDetailResponse>
  > getStudentDetail(@PathVariable String studentCode) {
    return ResponseEntity.ok(
      ApiResponse.success(
        publicCertificateVerificationService.getStudentDetail(studentCode)
      )
    );
  }

  @GetMapping("/certificates/{certificateId}/verify")
  public ResponseEntity<
    ApiResponse<PublicCertificateVerificationResponse>
  > verifyCertificate(@PathVariable String certificateId) {
    return ResponseEntity.ok(
      ApiResponse.success(
        publicCertificateVerificationService.verifyCertificate(certificateId)
      )
    );
  }
}
