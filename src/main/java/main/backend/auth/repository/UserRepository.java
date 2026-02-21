package main.backend.auth.repository;

import java.util.Optional;
import main.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByEmail(String email);
  Optional<User> findByGoogleId(String googleId);
  boolean existsByEmail(String email);
}
