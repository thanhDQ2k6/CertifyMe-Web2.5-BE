package main.backend.auth.service;

import java.util.List;
import java.util.Optional;
import main.backend.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface cho các module khác truy vấn User data.
 * Thay vì import UserRepository trực tiếp, hãy inject interface này.
 */
public interface UserQueryService {
  Optional<User> findById(String userId);

  Optional<User> findByEmail(String email);

  Optional<User> findByIdOrCode(String idOrCode);

  User getByIdOrThrow(String userId);

  User getByIdOrCodeOrThrow(String idOrCode);

  List<User> findAll();

  boolean existsById(String userId);

  Page<User> findUsersByFilter(
    Long roleId,
    Boolean isActive,
    Pageable pageable
  );
}
