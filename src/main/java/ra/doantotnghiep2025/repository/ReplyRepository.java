package ra.doantotnghiep2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    boolean existsByCommentId(Long commentId);
}