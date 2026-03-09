package main.backend.LMSCourseService.service;

import main.backend.common.util.IdGenerator;
import main.backend.lms.dto.request.ClassRequestDTO;
import main.backend.LMSCourseService.dto.response.ClassResponseDTO;
import main.backend.LMSCourseService.dto.response.StudentResponseDTO;
import main.backend.LMSCourseService.model.ClassEntity;
import main.backend.LMSCourseService.model.CourseEntity;
import main.backend.LMSCourseService.repository.ClassRepository;
import main.backend.auth.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassService {

    @Autowired
    private ClassRepository classRepository;

    public List<ClassResponseDTO> getTeacherClasses(String teacherId) {
        List<ClassEntity> classes = classRepository.findAll();
        return classes.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ClassResponseDTO getClassById(String classId) {
        ClassEntity clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));
        return mapToDTO(clazz);
    }

    public List<StudentResponseDTO> getStudentsInClass(String classId) {
        StudentResponseDTO s1 = new StudentResponseDTO();
        s1.setStudentId("s1");
        s1.setFullName("Nguyễn Văn An");
        s1.setEmail("annv@fpt.edu.vn");
        s1.setCompletedQuizzes(5);
        s1.setTotalQuizzes(5);
        s1.setAverageScore(8.5);
        s1.setStatus("passed");
        return List.of(s1);
    }

    public ClassResponseDTO createClass(ClassRequestDTO dto) {
        ClassEntity newClass = new ClassEntity();
        newClass.setClassId(IdGenerator.generateClassId());
        newClass.setClassCode(dto.getClassCode());
        newClass.setTotalQuizzes(0);

        // FIX LỖI DATE: Chuyển LocalDate từ DTO sang String cho Entity
        if (dto.getStartDate() != null) newClass.setStartDate(dto.getStartDate().toString());
        if (dto.getEndDate() != null) newClass.setEndDate(dto.getEndDate().toString());

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

        // FIX LỖI DATE: Chuyển LocalDate sang String
        if(dto.getStartDate() != null) existingClass.setStartDate(dto.getStartDate().toString());
        if(dto.getEndDate() != null) existingClass.setEndDate(dto.getEndDate().toString());

        return mapToDTO(classRepository.save(existingClass));
    }

    private ClassResponseDTO mapToDTO(ClassEntity clazz) {
        ClassResponseDTO dto = new ClassResponseDTO();
        dto.setClassId(clazz.getClassId());
        dto.setClassCode(clazz.getClassCode());

        // Hardcode status vì DB của ông bạn không có cột này
        dto.setStatus("active");

        if (clazz.getCourse() != null) {
            dto.setCourseId(clazz.getCourse().getCourseId());
            dto.setCourseName(clazz.getCourse().getCourseName());
        }
        if (clazz.getTeacher() != null) dto.setTeacherName(clazz.getTeacher().getFullName());

        dto.setStudentCount(30);
        dto.setQuizCount(clazz.getTotalQuizzes() != null ? clazz.getTotalQuizzes() : 0);

        // FIX LỖI DATE: Dịch ngược từ String trong DB ra LocalDate cho DTO
        if (clazz.getStartDate() != null && !clazz.getStartDate().isEmpty()) {
            dto.setStartDate(LocalDate.parse(clazz.getStartDate()));
        }
        if (clazz.getEndDate() != null && !clazz.getEndDate().isEmpty()) {
            dto.setEndDate(LocalDate.parse(clazz.getEndDate()));
        }

        return dto;
    }
}