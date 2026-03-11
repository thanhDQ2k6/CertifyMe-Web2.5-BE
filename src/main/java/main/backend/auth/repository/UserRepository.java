package main.backend.auth.repository;

import java.util.Optional;
import main.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByEmail(String email);
  Optional<User> findByGoogleId(String googleId);
  boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:roleId IS NULL OR u.role.id = :roleId) AND " +
            "(:isActive IS NULL OR u.isActive = :isActive)")
    org.springframework.data.domain.Page<User> findUsersByFilter(
            @org.springframework.data.repository.query.Param("roleId") Long roleId,
            @org.springframework.data.repository.query.Param("isActive") Boolean isActive,
            org.springframework.data.domain.Pageable pageable);
}
