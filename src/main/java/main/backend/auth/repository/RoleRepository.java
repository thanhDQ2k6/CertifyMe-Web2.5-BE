package main.backend.auth.repository;

import java.util.Optional;
import main.backend.auth.entity.Role;
import main.backend.auth.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByRoleName(RoleType roleName);
}
