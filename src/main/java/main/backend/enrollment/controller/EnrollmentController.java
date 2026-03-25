package main.backend.enrollment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.backend.common.dto.ApiResponse;
import main.backend.enrollment.dto.request.EnrollmentRequestDTO;
import main.backend.enrollment.dto.response.EnrollmentResponseDTO;
import main.backend.enrollment.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> createEnrollment(
            @Valid @RequestBody EnrollmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(enrollmentService.createEnrollment(request)));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEnrollment(
            @RequestParam String studentCode,
            @RequestParam String classId) {
        enrollmentService.deleteEnrollment(studentCode, classId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
