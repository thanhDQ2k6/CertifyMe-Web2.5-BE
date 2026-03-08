package main.backend.lms.controller;

import main.backend.lms.dto.response.CourseDetailResponse;
import main.backend.lms.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService; // Đảm bảo ông đã có CourseService nhé

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseDetail(@PathVariable String courseId) {
        // 1. Lấy studentId từ SecurityContext (Token)
        // Tui tạm lấy cứng "u1" để ông test, sau này phải lấy từ User hiện tại
        String studentId = "u1";

        // 2. Gọi đúng tên hàm mới: getCourseDetail
        CourseDetailResponse response = courseService.getCourseDetail(courseId, studentId);

        return ResponseEntity.ok(response);
    }
}
