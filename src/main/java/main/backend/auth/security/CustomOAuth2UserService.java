package main.backend.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.backend.auth.entity.Role;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import main.backend.auth.repository.RoleRepository;
import main.backend.auth.repository.UserRepository;
import main.backend.common.util.IdGenerator;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest)
    throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // Default role là STUDENT nếu không có role parameter
    RoleType roleType = RoleType.STUDENT;

    // Xử lý user
    processOAuth2User(oAuth2User, roleType);

    return oAuth2User;
  }

  private User processOAuth2User(OAuth2User oAuth2User, RoleType roleType) {
    String email = oAuth2User.getAttribute("email");
    String googleId = oAuth2User.getAttribute("sub");
    String name = oAuth2User.getAttribute("name");
    String picture = oAuth2User.getAttribute("picture");

    User user = userRepository
      .findByEmail(email)
      .map(existingUser ->
        updateExistingUser(existingUser, name, picture, roleType)
      )
      .orElseGet(() -> createNewUser(email, googleId, name, picture, roleType));

    return user;
  }

  private User updateExistingUser(
    User user,
    String name,
    String picture,
    RoleType roleType
  ) {
    user.setFullName(name);
    user.setAvatarUrl(picture);

    // Cập nhật role nếu user chọn role mới
    Role role = roleRepository
      .findByRoleName(roleType)
      .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));
    user.setRole(role);

    return userRepository.save(user);
  }

  private User createNewUser(
    String email,
    String googleId,
    String name,
    String picture,
    RoleType roleType
  ) {
    Role role = roleRepository
      .findByRoleName(roleType)
      .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));

    User user = User.builder()
      .userId(IdGenerator.generateUserId())
      .email(email)
      .googleId(googleId)
      .fullName(name)
      .avatarUrl(picture)
      .role(role)
      .isActive(true)
      .build();

    return userRepository.save(user);
  }
}
