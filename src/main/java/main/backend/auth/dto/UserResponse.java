package main.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.backend.auth.enums.RoleType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long userId;
  private String email;
  private String fullName;
  private String avatarUrl;
  private RoleType role;
  private Boolean isActive;
}
