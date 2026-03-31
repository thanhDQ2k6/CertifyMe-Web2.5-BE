package main.backend.auth.service;

import java.util.List;
import main.backend.auth.dto.UserResponse;

public interface UserService {
  UserResponse findByIdOrCode(String idOrCode);
  List<UserResponse> searchStudents(String keyword);
  List<UserResponse> searchTeachers(String keyword);
}
