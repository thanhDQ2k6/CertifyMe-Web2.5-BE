package main.backend.classroom.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import main.backend.auth.entity.User;
import main.backend.auth.service.UserQueryService;
import main.backend.classroom.dto.request.ClassRequestDTO;
import main.backend.classroom.dto.response.ClassResponseDTO;
import main.backend.classroom.dto.response.StudentResponseDTO;
import main.backend.classroom.entity.ClassEntity;
import main.backend.classroom.repository.ClassRepository;
import main.backend.common.enums.ClassStatus;
import main.backend.common.exception.ResourceNotFoundException;
import main.backend.common.util.IdGenerator;
import main.backend.course.entity.CourseEntity;
import main.backend.course.repository.CourseRepository;
import main.backend.enrollment.entity.Enrollment;
import main.backend.enrollment.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassService {

  private final ClassRepository classRepository;
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final UserQueryService userQueryService;

  // FIX CS1: filter by teacherId thay vì findAll()
  public List<ClassResponseDTO> getTeacherClasses(String teacherId) {
    List<ClassEntity> classes = classRepository.findByTeacher_UserId(teacherId);
    return classes.stream().map(this::mapToDTO).toList();
  }

  public ClassResponseDTO getClassById(String classId) {
    ClassEntity clazz = classRepository
      .findById(classId)
      .orElseThrow(() ->
        new ResourceNotFoundException("Class not found: " + classId)
      );
    return mapToDTO(clazz);
  }

  // FIX CS4: filter + sort hoạt động thực sự
  public List<StudentResponseDTO> getStudentsInClass(
    String classId,
    String status,
    String sort,
    String order
  ) {
    List<Enrollment> enrollments =
      enrollmentRepository.findByClassEntity_ClassId(classId);

    Stream<StudentResponseDTO> stream = enrollments
      .stream()
      .map(this::mapStudentDTO);

    if (status != null && !status.isBlank()) {
      stream = stream.filter(s -> status.equalsIgnoreCase(s.getStatus()));
    }

    Comparator<StudentResponseDTO> comparator = Comparator.comparing(
      StudentResponseDTO::getFullName
    );
    if ("averageScore".equals(sort)) {
      comparator = Comparator.comparing(StudentResponseDTO::getAverageScore);
    } else if ("enrolledAt".equals(sort)) {
      comparator = Comparator.comparing(StudentResponseDTO::getEnrolledAt);
    }
    if ("desc".equalsIgnoreCase(order)) {
      comparator = comparator.reversed();
    }

    return stream.sorted(comparator).toList();
  }

  public ClassResponseDTO createClass(ClassRequestDTO dto) {
    CourseEntity course = courseRepository
      .findById(dto.getCourseId())
      .orElseThrow(() ->
        new ResourceNotFoundException("Course not found: " + dto.getCourseId())
      );
    User teacher = userQueryService.getByIdOrThrow(dto.getTeacherId());

    ClassEntity newClass = ClassEntity.builder()
      .classId(IdGenerator.generateClassId())
      .classCode(dto.getClassCode())
      .course(course)
      .teacher(teacher)
      .startDate(dto.getStartDate())
      .endDate(dto.getEndDate())
      .status(ClassStatus.ACTIVE)
      .totalQuizzes(0)
      .build();

    return mapToDTO(classRepository.save(newClass));
  }

  public ClassResponseDTO updateClass(String id, ClassRequestDTO dto) {
    ClassEntity existing = classRepository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Class not found: " + id)
      );

    if (dto.getClassCode() != null) existing.setClassCode(dto.getClassCode());
    if (dto.getStartDate() != null) existing.setStartDate(dto.getStartDate());
    if (dto.getEndDate() != null) existing.setEndDate(dto.getEndDate());

    return mapToDTO(classRepository.save(existing));
  }

  private ClassResponseDTO mapToDTO(ClassEntity clazz) {
    ClassResponseDTO dto = new ClassResponseDTO();
    dto.setClassId(clazz.getClassId());
    dto.setClassCode(clazz.getClassCode());
    dto.setStatus(
      clazz.getStatus() != null
        ? clazz.getStatus().name().toLowerCase()
        : "active"
    );

    if (clazz.getCourse() != null) {
      dto.setCourseId(clazz.getCourse().getCourseId());
      dto.setCourseName(clazz.getCourse().getCourseName());
    }
    if (clazz.getTeacher() != null) {
      dto.setTeacherName(clazz.getTeacher().getFullName());
    }

    // FIX CS3: đếm thực tế thay vì hardcode 30
    long studentCount = enrollmentRepository.countByClassEntity_ClassId(
      clazz.getClassId()
    );
    dto.setStudentCount((int) studentCount);
    dto.setQuizCount(
      clazz.getTotalQuizzes() != null ? clazz.getTotalQuizzes() : 0
    );
    dto.setStartDate(clazz.getStartDate());
    dto.setEndDate(clazz.getEndDate());

    return dto;
  }

  private StudentResponseDTO mapStudentDTO(Enrollment e) {
    StudentResponseDTO dto = new StudentResponseDTO();
    dto.setStudentId(e.getStudent().getUserId());
    dto.setStudentCode(e.getStudent().getUserCode());
    dto.setFullName(e.getStudent().getFullName());
    dto.setEmail(e.getStudent().getEmail());
    dto.setAvatarUrl(e.getStudent().getAvatarUrl());
    dto.setCompletedQuizzes(
      e.getPassedQuizzes() != null ? e.getPassedQuizzes() : 0
    );
    dto.setTotalQuizzes(
      e.getClassEntity().getTotalQuizzes() != null
        ? e.getClassEntity().getTotalQuizzes()
        : 0
    );
    dto.setAverageScore(e.getFinalGrade() != null ? e.getFinalGrade() : 0.0);
    dto.setStatus(
      e.getStatus() != null ? e.getStatus().name().toLowerCase() : "learning"
    );
    dto.setEnrolledAt(e.getJoinedAt());
    return dto;
  }
}
