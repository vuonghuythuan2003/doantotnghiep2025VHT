package ra.doantotnghiep2025.model.dto;
import lombok.*;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReplyDTO {
    private Long id;
    private Long commentId;
    private Long adminId;
    private String adminName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}