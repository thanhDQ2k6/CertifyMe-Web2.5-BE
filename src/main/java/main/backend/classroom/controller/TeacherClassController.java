package main.backend.classroom.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.backend.auth.security.UserPrincipal;
import main.backend.classroom.dto.request.ClassRequestDTO;
import main.backend.classroom.dto.response.ClassResponseDTO;
import main.backend.classroom.dto.response.StudentResponseDTO;
import main.backend.classroom.service.ClassService;
import main.backend.common.dto.ApiResponse;
import main.backend.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherClassController {

  private final ClassService classService;

  @GetMapping("/teacher/{teacherId}/classes")
  public ResponseEntity<ApiResponse<List<ClassResponseDTO>>> getTeacherClasses(
    @PathVariable String teacherId,
    @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    if (userPrincipal == null || !teacherId.equals(userPrincipal.getUserId())) {
      throw new UnauthorizedException(
        "You can only access data of your own teacher account"
      );
    }
    return ResponseEntity.ok(
      ApiResponse.success(classService.getTeacherClasses(teacherId))
    );
  }

  @GetMapping("/classes/{classId}")
  public ResponseEntity<ApiResponse<ClassResponseDTO>> getClassDetail(
    @PathVariable String classId
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(classService.getClassById(classId))
    );
  }

  @GetMapping("/classes/{classId}/students")
  public ResponseEntity<
    ApiResponse<List<StudentResponseDTO>>
  > getStudentsInClass(
    @PathVariable String classId,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String sort,
    @RequestParam(required = false) String order
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(
        classService.getStudentsInClass(classId, status, sort, order)
      )
    );
  }

  @PostMapping("/classes")
  public ResponseEntity<ApiResponse<ClassResponseDTO>> createClass(
    @Valid @RequestBody ClassRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.success(classService.createClass(dto))
    );
  }

  @PutMapping("/classes/{id}")
  public ResponseEntity<ApiResponse<ClassResponseDTO>> updateClass(
    @PathVariable String id,
    @Valid @RequestBody ClassRequestDTO dto
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(classService.updateClass(id, dto))
    );
  }
}
