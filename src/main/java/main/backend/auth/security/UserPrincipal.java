package main.backend.auth.security;

import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Data;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

  private String userId;
  private String email;
  private String fullName;
  private RoleType role;
  private Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(User user) {
    return new UserPrincipal(
      user.getUserId(),
      user.getEmail(),
      user.getFullName(),
      user.getRole().getRoleName(),
      Collections.singletonList(
        new SimpleGrantedAuthority(
          "ROLE_" + user.getRole().getRoleName().name()
        )
      )
    );
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return null; // OAuth2 không cần password
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
