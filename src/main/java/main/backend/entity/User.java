package main.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import main.backend.constant.RoleName;

import javax.management.relation.Role;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    private String userId;

    @Column(unique = true, nullable = false)
    private String email;

    private String googleSubjectId;
    private String fullName;
    private String avatarUrl;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    private Boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}