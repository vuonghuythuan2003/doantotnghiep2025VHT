package ra.doantotnghiep2025.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequestDTO {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
