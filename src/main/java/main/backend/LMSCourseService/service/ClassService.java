package main.backend.LMSCourseService.service;

import main.backend.LMSLearningService.model.Enrollment;
import main.backend.LMSLearningService.repository.EnrollmentRepository;
import main.backend.common.util.IdGenerator;
import main.backend.lms.dto.request.ClassRequestDTO;
import main.backend.LMSCourseService.dto.response.ClassResponseDTO;
import main.backend.LMSCourseService.dto.response.StudentResponseDTO;
import main.backend.LMSCourseService.model.ClassEntity;
import main.backend.LMSCourseService.model.CourseEntity;
import main.backend.LMSCourseService.repository.ClassRepository;
import main.backend.auth.entity.User;
import main.backend.constant.ClassStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassService {

    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private main.backend.LMSLearningService.repository.EnrollmentRepository enrollmentRepository;
    public List<ClassResponseDTO> getTeacherClasses(String teacherId) {
        List<ClassEntity> classes = classRepository.findAll();
        return classes.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ClassResponseDTO getClassById(String classId) {
        ClassEntity clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));
        return mapToDTO(clazz);
    }

    public List<StudentResponseDTO> getStudentsInClass(String classId, String status, String sort, String order) {
        List<Enrollment> enrollments = enrollmentRepository.findByClassEntity_ClassId(classId);

        return enrollments.stream().map(e -> {
            StudentResponseDTO dto = new StudentResponseDTO();
            dto.setStudentId(e.getStudent().getUserId());
            dto.setFullName(e.getStudent().getFullName());
            dto.setEmail(e.getStudent().getEmail());
            dto.setAvatarUrl(e.getStudent().getAvatarUrl());
            dto.setCompletedQuizzes(e.getPassedQuizzes() != null ? e.getPassedQuizzes() : 0);
            dto.setTotalQuizzes(e.getClassEntity().getTotalQuizzes() != null ? e.getClassEntity().getTotalQuizzes() : 0);
            dto.setAverageScore(e.getFinalGrade() != null ? e.getFinalGrade() : 0.0);
            dto.setStatus(e.getStatus() != null ? e.getStatus().name().toLowerCase() : "learning");
            dto.setEnrolledAt(e.getJoinedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public ClassResponseDTO createClass(ClassRequestDTO dto) {
        ClassEntity newClass = new ClassEntity();
        newClass.setClassId(IdGenerator.generateClassId());
        newClass.setClassCode(dto.getClassCode());
        newClass.setTotalQuizzes(0);

        if (dto.getStartDate() != null) newClass.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) newClass.setEndDate(dto.getEndDate());
        newClass.setStatus(ClassStatus.ACTIVE);

        if (dto.getCourseId() != null) {
            CourseEntity course = new CourseEntity();
            course.setCourseId(dto.getCourseId());
            newClass.setCourse(course);
        }
        if (dto.getTeacherId() != null) {
            User teacher = new User();
            teacher.setUserId(dto.getTeacherId());
            newClass.setTeacher(teacher);
        }

        return mapToDTO(classRepository.save(newClass));
    }

    public ClassResponseDTO updateClass(String id, ClassRequestDTO dto) {
        ClassEntity existingClass = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

        if(dto.getClassCode() != null) existingClass.setClassCode(dto.getClassCode());
        if(dto.getStartDate() != null) existingClass.setStartDate(dto.getStartDate());
        if(dto.getEndDate() != null) existingClass.setEndDate(dto.getEndDate());

        return mapToDTO(classRepository.save(existingClass));
    }

    private ClassResponseDTO mapToDTO(ClassEntity clazz) {
        ClassResponseDTO dto = new ClassResponseDTO();
        dto.setClassId(clazz.getClassId());
        dto.setClassCode(clazz.getClassCode());

        dto.setStatus(clazz.getStatus() != null ? clazz.getStatus().name().toLowerCase() : "active");

        if (clazz.getCourse() != null) {
            dto.setCourseId(clazz.getCourse().getCourseId());
            dto.setCourseName(clazz.getCourse().getCourseName());
        }
        if (clazz.getTeacher() != null) dto.setTeacherName(clazz.getTeacher().getFullName());

        dto.setStudentCount(30);
        dto.setQuizCount(clazz.getTotalQuizzes() != null ? clazz.getTotalQuizzes() : 0);
        dto.setStartDate(clazz.getStartDate());
        dto.setEndDate(clazz.getEndDate());

        return dto;
    }
}