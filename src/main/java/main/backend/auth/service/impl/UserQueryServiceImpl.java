package main.backend.auth.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.auth.service.UserQueryService;
import main.backend.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

  private final UserRepository userRepository;

  @Override
  public Optional<User> findById(String userId) {
    return userRepository.findById(userId);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public Optional<User> findByIdOrCode(String idOrCode) {
    return userRepository.findByUserIdOrUserCode(idOrCode);
  }

  @Override
  public User getByIdOrThrow(String userId) {
    return userRepository
      .findById(userId)
      .orElseThrow(() ->
        new ResourceNotFoundException("User not found: " + userId)
      );
  }

  @Override
  public User getByIdOrCodeOrThrow(String idOrCode) {
    return userRepository
      .findByUserIdOrUserCode(idOrCode)
      .orElseThrow(() ->
        new ResourceNotFoundException("User not found: " + idOrCode)
      );
  }

  @Override
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  public boolean existsById(String userId) {
    return userRepository.existsById(userId);
  }

  @Override
  public Page<User> findUsersByFilter(
    Long roleId,
    Boolean isActive,
    Pageable pageable
  ) {
    return userRepository.findUsersByFilter(roleId, isActive, pageable);
  }
}
