package main.backend.enrollment.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import main.backend.auth.security.UserPrincipal;
import main.backend.certificate.dto.response.CertificateResponse;
import main.backend.certificate.service.CertificateService;
import main.backend.common.dto.ApiResponse;
import main.backend.common.exception.UnauthorizedException;
import main.backend.course.dto.response.CourseResponse;
import main.backend.enrollment.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;
  private final CertificateService certificateService;

  @GetMapping("/{studentId}/courses")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesById(
    @PathVariable String studentId,
    @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    validateCurrentStudent(studentId, userPrincipal);
    List<CourseResponse> data = studentService.getStudentDashboard(studentId);
    return ResponseEntity.ok(ApiResponse.success(data));
  }

  @GetMapping("/{studentId}/certificates")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<ApiResponse<List<CertificateResponse>>> getCertificates(
    @PathVariable String studentId,
    @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    validateCurrentStudent(studentId, userPrincipal);
    List<CertificateResponse> data =
      certificateService.getCertificatesByStudentId(studentId);
    return ResponseEntity.ok(ApiResponse.success(data));
  }

  @PostMapping("/{studentId}/classes/{classId}/certificates/issue")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<ApiResponse<CertificateResponse>> issueCertificate(
    @PathVariable String studentId,
    @PathVariable String classId,
    @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    validateCurrentStudent(studentId, userPrincipal);
    CertificateResponse data = certificateService.issueCertificate(
      studentId,
      classId
    );
    return ResponseEntity.ok(ApiResponse.success(data));
  }

  private void validateCurrentStudent(
    String studentId,
    UserPrincipal userPrincipal
  ) {
    if (userPrincipal == null || !studentId.equals(userPrincipal.getUserId())) {
      throw new UnauthorizedException(
        "You can only access data of your own student account"
      );
    }
  }
}
