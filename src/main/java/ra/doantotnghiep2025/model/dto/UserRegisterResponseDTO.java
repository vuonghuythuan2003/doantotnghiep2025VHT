package ra.doantotnghiep2025.model.dto;

import lombok.*;
import ra.doantotnghiep2025.model.entity.Role;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRegisterResponseDTO {
    private String username;
    private Set<Role> roles;
}
