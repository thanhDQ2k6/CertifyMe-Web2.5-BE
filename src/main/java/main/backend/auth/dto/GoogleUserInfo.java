package main.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {
    private String sub;          // Google ID
    private String email;
    private String name;
    private String picture;
    private Boolean emailVerified;
}
