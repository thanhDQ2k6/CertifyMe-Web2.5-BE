package main.backend.service;

import main.backend.dto.request.ClassRequestDTO; // Nhớ thêm import này
import main.backend.dto.response.ClassResponseDTO;
import java.util.List;

public interface ClassService {
    List<ClassResponseDTO> getStudentDashboard(String studentId);
    void softDeleteClass(String classId);

    // THÊM DÒNG NÀY VÀO LÀ CONTROLLER HẾT ĐỎ
    void updateClass(String classId, ClassRequestDTO dto);
}