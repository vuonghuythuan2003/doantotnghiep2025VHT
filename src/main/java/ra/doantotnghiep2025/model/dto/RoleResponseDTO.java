package ra.doantotnghiep2025.model.dto;

import lombok.*;
import ra.doantotnghiep2025.model.entity.RoleType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RoleResponseDTO {
    private Long id;
    private RoleType roleType;
}
