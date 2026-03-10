package main.backend.LMSLearningService.controller;

import lombok.RequiredArgsConstructor;
import main.backend.LMSCertificateBlockchainService.dto.response.CertificateResponse;
import main.backend.LMSCourseService.dto.response.CourseResponse;
import main.backend.LMSCertificateBlockchainService.service.CertificateService;
import main.backend.LMSLearningService.service.StudentService;
import main.backend.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final CertificateService certificateService;

    @GetMapping("/{studentId}/courses")
    // @PreAuthorize("hasRole('STUDENT')") // Khi nào bật bảo mật thì mở cái này ra
    public ApiResponse<List<CourseResponse>> getCoursesById(@PathVariable String studentId) {

        List<CourseResponse> data = studentService.getStudentDashboard(studentId);
        return ApiResponse.success("Lấy danh sách khóa học thành công", data);
    }

    @GetMapping("/{studentId}/certificates")
    // @PreAuthorize("hasRole('STUDENT')") // Khi nào bật bảo mật thì mở cái này ra
    public ApiResponse<List<CertificateResponse>> getCertificates(@PathVariable String studentId) {
        List<CertificateResponse> data = certificateService.getCertificatesByStudentId(studentId);
        return ApiResponse.success("Lấy danh sách chứng chỉ thành công", data);
    }
}