package main.backend.LMSCourseService.controller;

import main.backend.LMSCourseService.dto.response.CourseDetailResponse;
import main.backend.LMSCourseService.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Import các thứ bảo mật để sẵn ở đây
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseDetail(
            @PathVariable String courseId
            /* , @AuthenticationPrincipal UserDetails userDetails */ // <-- PHƯƠNG ÁN TOKEN (ĐANG KHÓA)
    ) {
        // 1. PHƯƠNG ÁN BẢO MẬT (KHI NÀO CÓ LOGIN THÌ MỞ RA)
        /*
        String studentId = userDetails.getUsername();
        */

        // 2. PHƯƠNG ÁN GIẢ SỬ (ĐANG DÙNG ĐỂ TEST URL)
        // Vì URL yêu cầu chỉ lấy mỗi courseId, nên studentId phải tự định nghĩa ở đây
        String studentId = "u1";

        // Gọi Service với tham số giả lập để khớp logic đã viết
        CourseDetailResponse response = courseService.getCourseDetail(courseId, studentId);

        return ResponseEntity.ok(response);
    }
}