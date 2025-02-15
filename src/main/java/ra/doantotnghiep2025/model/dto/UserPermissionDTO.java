package ra.doantotnghiep2025.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserPermissionDTO {
    @NotNull
    private Set<String> roleName;
}