package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.model.dto.CommentDTO;
import ra.doantotnghiep2025.model.dto.ReplyDTO;
import ra.doantotnghiep2025.model.entity.Comment;
import ra.doantotnghiep2025.model.entity.Products;
import ra.doantotnghiep2025.model.entity.Reply;
import ra.doantotnghiep2025.model.entity.User;
import ra.doantotnghiep2025.repository.CommentRepository;
import ra.doantotnghiep2025.repository.ProductRepository;
import ra.doantotnghiep2025.repository.ReplyRepository;
import ra.doantotnghiep2025.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productsRepository;

    // Create a new comment
    public CommentDTO createComment(CommentDTO commentDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Products product = productsRepository.findById(commentDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Comment comment = new Comment();
        comment.setProduct(product);
        comment.setUser(user);
        comment.setContent(commentDTO.getContent());
        comment = commentRepository.save(comment);

        return mapToDTO(comment);
    }

    // Get all comments for a product
    public List<CommentDTO> getCommentsByProductId(Long productId) {
        List<Comment> comments = commentRepository.findByProductProductId(productId);
        return comments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Get all comments (for admin)
    public List<CommentDTO> getAllComments() {
        List<Comment> comments = commentRepository.findAll();
        return comments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Add a reply to a comment
    public ReplyDTO replyToComment(Long commentId, ReplyDTO replyDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (replyRepository.existsByCommentId(commentId)) {
            throw new RuntimeException("A reply already exists for this comment");
        }

        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getRoles().stream().anyMatch(role -> role.getRoleName().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("User is not an admin");
        }

        Reply reply = new Reply();
        reply.setComment(comment);
        reply.getUser();
        reply.setContent(replyDTO.getContent());
        reply = replyRepository.save(reply);

        return mapToReplyDTO(reply);
    }

    // Map Comment entity to CommentDTO
    private CommentDTO mapToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setProductId(comment.getProduct().getProductId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getUsername());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        if (comment.getReply() != null) {
            dto.setReply(mapToReplyDTO(comment.getReply()));
        }
        return dto;
    }

    // Map Reply entity to ReplyDTO
    private ReplyDTO mapToReplyDTO(Reply reply) {
        ReplyDTO dto = new ReplyDTO();
        dto.setId(reply.getId());
        dto.setCommentId(reply.getComment().getId());
        dto.setUserId(reply.getUser().getId());
        dto.setUserName(reply.getUser().getUsername());
        dto.setContent(reply.getContent());
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setUpdatedAt(reply.getUpdatedAt());
        return dto;
    }
}