package main.backend.lms.controller;

import main.backend.lms.dto.request.ClassRequestDTO;
import main.backend.lms.dto.response.ClassResponseDTO;
import main.backend.lms.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassController {

    @Autowired private ClassService classService;

    // API ĐỔ DỮ LIỆU LÊN DASHBOARD 80%
    @GetMapping("/dashboard/{studentId}")
    public ResponseEntity<List<ClassResponseDTO>> getDashboard(@PathVariable String studentId) {
        return ResponseEntity.ok(classService.getStudentDashboard(studentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody ClassRequestDTO dto) {
        classService.updateClass(id, dto);
        return ResponseEntity.ok().build();
    }

    // XÓA KIỂU CẬP NHẬT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        classService.softDeleteClass(id);
        return ResponseEntity.noContent().build();
    }
}