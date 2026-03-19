package main.backend.enrollment.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import main.backend.auth.service.UserQueryService;
import main.backend.classroom.entity.ClassEntity;
import main.backend.classroom.repository.ClassRepository;
import main.backend.common.enums.EnrollmentStatus;
import main.backend.common.exception.BusinessException;
import main.backend.common.exception.ResourceNotFoundException;
import main.backend.enrollment.dto.request.EnrollmentRequestDTO;
import main.backend.enrollment.dto.response.EnrollmentResponseDTO;
import main.backend.enrollment.entity.Enrollment;
import main.backend.enrollment.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserQueryService userQueryService;
    private final ClassRepository classRepository;

    @Transactional
    public EnrollmentResponseDTO createEnrollment(EnrollmentRequestDTO request) {
        User student = userQueryService.getByIdOrThrow(request.getStudentId());

        if (!student.getRole().getRoleName().equals(RoleType.STUDENT)) {
            throw new BusinessException("User must be a student");
        }

        ClassEntity classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + request.getClassId()));

        if (enrollmentRepository.findByStudent_UserIdAndClassEntity_ClassId(
                request.getStudentId(), request.getClassId()).isPresent()) {
            throw new BusinessException("Student already enrolled in this class");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .classEntity(classEntity)
                .status(EnrollmentStatus.LEARNING)
                .joinedAt(LocalDateTime.now())
                .passedQuizzes(0)
                .build();

        enrollmentRepository.save(enrollment);

        return mapToResponse(enrollment);
    }

    @Transactional
    public void deleteEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
    }

    private EnrollmentResponseDTO mapToResponse(Enrollment e) {
        return EnrollmentResponseDTO.builder()
                .enrollmentId(e.getEnrollmentId())
                .studentId(e.getStudent().getUserId())
                .studentName(e.getStudent().getFullName())
                .classId(e.getClassEntity().getClassId())
                .className(e.getClassEntity().getClassCode())
                .status(e.getStatus().name())
                .enrolledAt(e.getJoinedAt().toString())
                .build();
    }
}
