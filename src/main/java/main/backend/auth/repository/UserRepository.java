package main.backend.auth.repository;

import java.util.List;
import java.util.Optional;
import main.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByEmail(String email);
  Optional<User> findByGoogleId(String googleId);
  Optional<User> findByUserCode(String userCode);
  boolean existsByEmail(String email);

  // Tìm user bằng ID hoặc Code
  @Query(
    "SELECT u FROM User u WHERE u.userId = :idOrCode OR u.userCode = :idOrCode"
  )
  Optional<User> findByUserIdOrUserCode(
    @org.springframework.data.repository.query.Param("idOrCode") String idOrCode
  );

  // Tìm số lớn nhất của code theo prefix (HS, GV, AD)
  @Query(
    value = "SELECT MAX(CAST(SUBSTRING(user_code, 3) AS UNSIGNED)) FROM users WHERE user_code LIKE CONCAT(:prefix, '%')",
    nativeQuery = true
  )
  Integer findMaxCodeNumberByPrefix(
    @org.springframework.data.repository.query.Param("prefix") String prefix
  );

  // Search user by code hoặc email hoặc name (cho teacher add student)
  @Query(
    "SELECT u FROM User u WHERE " +
      "(LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
      "u.role.roleName = :roleName"
  )
  List<User> searchByKeywordAndRole(
    @org.springframework.data.repository.query.Param("keyword") String keyword,
    @org.springframework.data.repository.query.Param("roleName") String roleName
  );

  @Query(
    "SELECT u FROM User u WHERE " +
      "(:roleId IS NULL OR u.role.id = :roleId) AND " +
      "(:isActive IS NULL OR u.isActive = :isActive)"
  )
  org.springframework.data.domain.Page<User> findUsersByFilter(
    @org.springframework.data.repository.query.Param("roleId") Long roleId,
    @org.springframework.data.repository.query.Param(
      "isActive"
    ) Boolean isActive,
    org.springframework.data.domain.Pageable pageable
  );
}
