package main.backend.lms.controller;

import lombok.RequiredArgsConstructor;
import main.backend.lms.dto.response.CertificateResponse;
import main.backend.lms.dto.response.CourseResponse;
import main.backend.lms.model.Enrollment;
import main.backend.lms.repository.EnrollmentRepository;
import main.backend.lms.service.CertificateService;
import main.backend.lms.service.StudentService;
import main.backend.auth.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import main.backend.common.dto.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificateService certificateService;

    /**
     * Lấy danh sách khóa học cho Dashboard của sinh viên
     * Test trên Postman: GET http://localhost:8080/api/student/my-courses
     * Header: Authorization: Bearer <token_cua_ong>
     */
    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<CourseResponse>> getMyCourses(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // Lấy ID từ UserPrincipal (đã được nhóm trưởng thiết lập trong security)
        // Sửa getId() thành getUserId() cho đúng với tên biến bên UserPrincipal
        String studentId = userPrincipal.getUserId();

        List<CourseResponse> data = studentService.getStudentDashboard(studentId);
        return ApiResponse.success("Lấy danh sách khóa học thành công", data);
    }
    @GetMapping("/{studentId}/certificates")
    public ResponseEntity<?> getCertificates(@PathVariable String studentId) {
        // Gọi Service lấy danh sách (Array)
        List<CertificateResponse> data = certificateService.getCertificatesByStudentId(studentId);

        // Trả về cho Frontend. Nếu không có bằng nào thì nó trả về mảng rỗng [] -> Đúng ý nhóm trưởng
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", data));
    }
    // Nếu ông vẫn muốn giữ endpoint dùng PathVariable để test cứng:
    @GetMapping("/{studentId}/courses")
//    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ApiResponse<List<CourseResponse>> getCoursesById(@PathVariable String studentId) {
        List<CourseResponse> data = studentService.getStudentDashboard(studentId);
        return ApiResponse.success(data);
    }
    // Thêm vào StudentService.java

}