package main.backend.LMSCourseService.controller;

import main.backend.LMSCourseService.dto.response.CourseDetailResponse;
import main.backend.LMSCourseService.service.CourseService;
import main.backend.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Import các thứ bảo mật để sẵn ở đây
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import main.backend.auth.security.UserPrincipal;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // Lấy chi tiết khóa học kèm danh sách quiz
    @GetMapping("/{courseId}")
    public ApiResponse<CourseDetailResponse> getCourseDetail(
            @PathVariable String courseId
            /* , @AuthenticationPrincipal UserPrincipal userPrincipal */ // <-- PHƯƠNG ÁN TOKEN (ĐANG KHÓA)
    ) {
        // 1. PHƯƠNG ÁN BẢO MẬT (KHI NÀO CÓ LOGIN THÌ MỞ RA VÀ DÙNG DÒNG DƯỚI)
        /*
        String studentId = userPrincipal.getUserId();
        */

        // 2. PHƯƠNG ÁN GIẢ SỬ (ĐANG DÙNG ĐỂ TEST BẰNG POSTMAN CHO LẸ)
        // Lấy đúng ID của "Nguyễn Văn An" trong file mock data V4 lúc nãy anh em mình đổ
        String studentId = "550e8400-e29b-41d4-a716-446655440000";

        // Gọi Service
        CourseDetailResponse response = courseService.getCourseDetail(courseId, studentId);
        return ApiResponse.success("Lấy chi tiết khóa học thành công", response);
    }
}