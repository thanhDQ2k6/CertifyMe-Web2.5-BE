package main.backend.auth.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import main.backend.auth.dto.UserResponse;
import main.backend.auth.service.UserService;
import main.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * Tìm user bằng ID hoặc Code
   * GET /api/users/{idOrCode}
   */
  @GetMapping("/{idOrCode}")
  @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> getUserByIdOrCode(
    @PathVariable String idOrCode
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(userService.findByIdOrCode(idOrCode))
    );
  }

  /**
   * Tìm kiếm students theo keyword (code, email, name)
   * GET /api/users/search/students?q=HS006
   */
  @GetMapping("/search/students")
  @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<UserResponse>>> searchStudents(
    @RequestParam String q
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(userService.searchStudents(q))
    );
  }

  /**
   * Tìm kiếm teachers theo keyword (code, email, name)
   * GET /api/users/search/teachers?q=GV001
   */
  @GetMapping("/search/teachers")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<UserResponse>>> searchTeachers(
    @RequestParam String q
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(userService.searchTeachers(q))
    );
  }
}
