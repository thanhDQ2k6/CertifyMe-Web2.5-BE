package main.backend.lms.service;

import main.backend.lms.dto.request.ClassRequestDTO; // Nhớ thêm import này
import main.backend.lms.dto.response.ClassResponseDTO;
import java.util.List;

public interface ClassService {
    List<ClassResponseDTO> getStudentDashboard(String studentId);
    void softDeleteClass(String classId);

    // THÊM DÒNG NÀY VÀO LÀ CONTROLLER HẾT ĐỎ
    void updateClass(String classId, ClassRequestDTO dto);
}