package renatius.imageservice_internship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renatius.imageservice_internship.entities.LikeComment;

import java.util.Optional;
import java.util.UUID;

public interface LikeCommentRepository extends JpaRepository<LikeComment, UUID> {
    Optional<LikeComment> findByCommentImage_IdAndSocialUser_Id(UUID commentId, UUID userId);
    boolean existsByCommentImage_IdAndSocialUser_Id(UUID commentId, UUID userId);
    long countByCommentImage_Id(UUID commentId);
}
