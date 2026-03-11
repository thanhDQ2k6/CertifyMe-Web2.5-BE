package main.backend.LMSCourseService.controller;

import main.backend.common.dto.ApiResponse;
import main.backend.lms.dto.request.ClassRequestDTO;
import main.backend.LMSCourseService.dto.response.ClassResponseDTO;
import main.backend.LMSCourseService.dto.response.StudentResponseDTO;
import main.backend.LMSCourseService.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TeacherClassController {

    @Autowired
    private ClassService classService;

    @GetMapping("/teacher/{teacherId}/classes")
    public ResponseEntity<ApiResponse<List<ClassResponseDTO>>> getTeacherClasses(@PathVariable String teacherId) {
        return ResponseEntity.ok(ApiResponse.success("Success", classService.getTeacherClasses(teacherId)));
    }

    @GetMapping("/classes/{classId}")
    public ResponseEntity<ApiResponse<ClassResponseDTO>> getClassDetail(@PathVariable String classId) {
        return ResponseEntity.ok(ApiResponse.success("Success", classService.getClassById(classId)));
    }

    @GetMapping("/classes/{classId}/students")
    public ResponseEntity<ApiResponse<List<StudentResponseDTO>>> getStudentsInClass(
            @PathVariable String classId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order) {
        return ResponseEntity.ok(ApiResponse.success("Success", classService.getStudentsInClass(classId, status, sort, order)));
    }

    @PostMapping("/classes")
    public ResponseEntity<ApiResponse<ClassResponseDTO>> createClass(@RequestBody ClassRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Class created successfully", classService.createClass(dto)));
    }

    @PutMapping("/classes/{id}")
    public ResponseEntity<ApiResponse<ClassResponseDTO>> updateClass(@PathVariable String id, @RequestBody ClassRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Class updated successfully", classService.updateClass(id, dto)));
    }
}