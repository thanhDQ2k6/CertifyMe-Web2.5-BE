package main.backend.course.controller;

import lombok.RequiredArgsConstructor;
import main.backend.course.dto.response.CourseDetailResponse;
import main.backend.course.service.CourseService;
import main.backend.common.dto.ApiResponse;
import main.backend.auth.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(
            @PathVariable String courseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String studentId = userPrincipal.getUserId();
        CourseDetailResponse response = courseService.getCourseDetail(courseId, studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
