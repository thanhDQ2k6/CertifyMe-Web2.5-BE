package main.backend.lms.service;

import main.backend.lms.dto.request.ClassRequestDTO; // Phải chuẩn đường dẫn này
import main.backend.lms.dto.response.ClassResponseDTO;
import java.util.List;

public interface ClassService {
    List<ClassResponseDTO> getStudentDashboard(String studentId);
    List<ClassResponseDTO> getAllClasses();
    ClassResponseDTO createClass(ClassRequestDTO request);

    // Kiểm tra kỹ tham số String id và ClassRequestDTO request
    ClassResponseDTO updateClass(String id, ClassRequestDTO request);

    void softDeleteClass(String classId);
}