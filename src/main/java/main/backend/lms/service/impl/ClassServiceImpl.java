package main.backend.lms.service.impl;

import main.backend.lms.constant.ClassStatus;
import main.backend.lms.dto.request.ClassRequestDTO;
import main.backend.lms.dto.response.ClassResponseDTO;
import main.backend.lms.entity.Clazz;
import main.backend.lms.entity.Enrollment;
import main.backend.lms.repository.ClassRepository;
import main.backend.lms.repository.EnrollmentRepository;
import main.backend.lms.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassRepository classRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    // 1. Lấy Dashboard cho sinh viên (Cái ông cần test Postman nhất)
    @Override
    public List<ClassResponseDTO> getStudentDashboard(String studentId) {
        List<Enrollment> enrollments = enrollmentRepo.findByStudent_UserId(studentId);
        return enrollments.stream().map(this::mapEnrollmentToDTO).collect(Collectors.toList());
    }


    // 2. Lấy tất cả lớp học
    @Override
    public List<ClassResponseDTO> getAllClasses() {
        return classRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 3. Tạo lớp học mới
    @Override
    public ClassResponseDTO createClass(ClassRequestDTO request) {
        Clazz clazz = new Clazz();
        // Giả sử Entity Clazz của ông có trường className
        // clazz.setClassName(request.getClassName());
        clazz.setStatus(ClassStatus.ACTIVE);

        Clazz saved = classRepo.save(clazz);
        return mapToDTO(saved);
    }

    // 4. Cập nhật lớp học (Fix lỗi override method)
    @Override
    public ClassResponseDTO updateClass(String id, ClassRequestDTO request) {
        Clazz clazz = classRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học với ID: " + id));

        // Cập nhật các trường cần thiết ở đây
        // clazz.setClassName(request.getClassName());

        Clazz updated = classRepo.save(clazz);
        return mapToDTO(updated);
    }

    // 5. Xóa mềm lớp học
    @Override
    public void softDeleteClass(String classId) {
        Clazz clazz = classRepo.findById(classId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp để xóa"));
        clazz.setStatus(ClassStatus.CANCELED);
        classRepo.save(clazz);
    }

    // --- CÁC HÀM MAPPING (Để code trên trông gọn gàng hơn) ---

    private ClassResponseDTO mapToDTO(Clazz clazz) {
        ClassResponseDTO dto = new ClassResponseDTO();
        dto.setClassId(clazz.getClassId());
        if (clazz.getCourse() != null) {
            dto.setCourseName(clazz.getCourse().getCourseName());
        }
        dto.setTotalQuizzes(clazz.getTotalQuizzes());
        return dto;
    }

    private ClassResponseDTO mapEnrollmentToDTO(Enrollment en) {
        ClassResponseDTO dto = new ClassResponseDTO();
        if (en.getClazz() != null) {
            dto.setClassId(en.getClazz().getClassId());
            if (en.getClazz().getCourse() != null) {
                dto.setCourseName(en.getClazz().getCourse().getCourseName());
            }
            dto.setTotalQuizzes(en.getClazz().getTotalQuizzes());
        }
        dto.setPassedQuizzes(en.getPassedQuizzes());

        // Tính % tiến độ
        if (dto.getTotalQuizzes() > 0) {
            double progress = (double) dto.getPassedQuizzes() / dto.getTotalQuizzes() * 100;
            dto.setProgressPercentage(progress);
        } else {
            dto.setProgressPercentage(0.0);
        }
        return dto;
    }
}