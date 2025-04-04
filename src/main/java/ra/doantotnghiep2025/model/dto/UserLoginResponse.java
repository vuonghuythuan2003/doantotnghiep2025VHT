package ra.doantotnghiep2025.model.dto;

import lombok.*;
import ra.doantotnghiep2025.model.entity.Role;

import java.util.Set;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserLoginResponse {
    private Long userId;
    private String username;
    private String typeToken;
    private String accessToken;
    private Set<Role> roles;
}