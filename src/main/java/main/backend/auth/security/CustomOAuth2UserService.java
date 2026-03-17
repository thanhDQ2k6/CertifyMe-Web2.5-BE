package main.backend.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.backend.auth.entity.Role;
import main.backend.auth.entity.User;
import main.backend.auth.enums.RoleType;
import main.backend.auth.repository.RoleRepository;
import main.backend.auth.repository.UserRepository;
import main.backend.common.exception.ResourceNotFoundException;
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
        processOAuth2User(oAuth2User);
        return oAuth2User;
    }

    private void processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        existingUser -> updateExistingUser(existingUser, name, picture, googleId),
                        () -> createNewUser(email, googleId, name, picture)
                );
    }

    private void updateExistingUser(User user, String name, String picture, String googleId) {
        boolean updated = false;

        if (name != null && !name.equals(user.getFullName())) {
            user.setFullName(name);
            updated = true;
        }
        if (picture != null && !picture.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(picture);
            updated = true;
        }
        if (googleId != null && !googleId.equals(user.getGoogleId())) {
            user.setGoogleId(googleId);
            updated = true;
        }
        // KHÔNG ghi đè role — giữ nguyên role hiện tại

        if (updated) {
            userRepository.save(user);
        }
    }

    private void createNewUser(String email, String googleId, String name, String picture) {
        Role studentRole = roleRepository.findByRoleName(RoleType.STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT role not found in database"));

        User user = User.builder()
                .userId(IdGenerator.generateUserId())
                .email(email)
                .googleId(googleId)
                .fullName(name != null ? name : "User")
                .avatarUrl(picture)
                .role(studentRole)
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("Created new user: {} with role: STUDENT", email);
    }
}
