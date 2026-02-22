package main.backend.lms.service.impl;

import main.backend.lms.constant.ClassStatus;
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
    @Autowired private ClassRepository classRepo;
    @Autowired private EnrollmentRepository enrollmentRepo;

    @Override
    public List<ClassResponseDTO> getStudentDashboard(String studentId) {
        List<Enrollment> enrollments = enrollmentRepo.findByStudent_UserId(studentId);
        return enrollments.stream().map(en -> {
            ClassResponseDTO dto = new ClassResponseDTO();
            dto.setClassId(en.getClazz().getClassId());
            dto.setCourseName(en.getClazz().getCourse().getCourseName());
            dto.setPassedQuizzes(en.getPassedQuizzes());
            dto.setTotalQuizzes(en.getClazz().getTotalQuizzes());

            // LOGIC 80% TIẾN ĐỘ TRÊN SKETCH UI
            if (dto.getTotalQuizzes() > 0) {
                dto.setProgressPercentage((double) dto.getPassedQuizzes() / dto.getTotalQuizzes() * 100);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void softDeleteClass(String classId) {
        Clazz clazz = classRepo.findById(classId).orElseThrow();
        clazz.setStatus(ClassStatus.CANCELED); // Xóa kiểu cập nhật
        classRepo.save(clazz);
    }

}