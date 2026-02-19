package renatius.imageservice_internship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import renatius.imageservice_internship.entities.LikeComment;
import renatius.imageservice_internship.service.CommentCountView;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface LikeCommentRepository extends JpaRepository<LikeComment, UUID> {
    Optional<LikeComment> findByCommentImage_IdAndSocialUser_Id(UUID commentId, UUID userId);
    boolean existsByCommentImage_IdAndSocialUser_Id(UUID commentId, UUID userId);
    long countByCommentImage_Id(UUID commentId);
    @Query("""
        select lc.commentImage.id as commentId, count(lc.id) as cnt
        from LikeComment lc
        where lc.commentImage.id in :commentIds
        group by lc.commentImage.id
    """)
    List<CommentCountView> countByCommentIds(@Param("commentIds") List<UUID> commentIds);

    @Query("""
        select lc.commentImage.id
        from LikeComment lc
        where lc.commentImage.id in :commentIds and lc.socialUser.id = :userId
    """)
    List<UUID> findLikedCommentIdsByUser(@Param("commentIds") List<UUID> commentIds,
                                         @Param("userId") UUID userId);
}
