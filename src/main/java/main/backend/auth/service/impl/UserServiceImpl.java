package main.backend.auth.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import main.backend.auth.dto.UserResponse;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.auth.service.UserService;
import main.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public UserResponse findByIdOrCode(String idOrCode) {
    User user = userRepository
      .findByUserIdOrUserCode(idOrCode)
      .orElseThrow(() ->
        new ResourceNotFoundException("User not found: " + idOrCode)
      );
    return mapToResponse(user);
  }

  @Override
  public List<UserResponse> searchStudents(String keyword) {
    List<User> users = userRepository.searchByKeywordAndRole(
      keyword,
      main.backend.auth.enums.RoleType.STUDENT
    );
    return users.stream().map(this::mapToResponse).toList();
  }

  @Override
  public List<UserResponse> searchTeachers(String keyword) {
    List<User> users = userRepository.searchByKeywordAndRole(
      keyword,
      main.backend.auth.enums.RoleType.TEACHER
    );
    return users.stream().map(this::mapToResponse).toList();
  }

  private UserResponse mapToResponse(User user) {
    return UserResponse.builder()
      .userId(user.getUserId())
      .userCode(user.getUserCode())
      .email(user.getEmail())
      .fullName(user.getFullName())
      .avatarUrl(user.getAvatarUrl())
      .role(user.getRole().getRoleName())
      .isActive(user.getIsActive())
      .build();
  }
}
