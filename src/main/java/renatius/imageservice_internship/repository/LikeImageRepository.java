package renatius.imageservice_internship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import renatius.imageservice_internship.entities.LikeImage;
import renatius.imageservice_internship.service.ImageCountView;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeImageRepository extends JpaRepository<LikeImage, UUID> {
    Optional<LikeImage> findByImage_IdAndSocialUser_Id(UUID imageId, UUID userId);
    boolean existsByImage_IdAndSocialUser_Id(UUID imageId, UUID userId);
    long countByImage_Id(UUID imageId);
    @Query("""
        select li.image.id as imageId, count(li.id) as cnt
        from LikeImage li
        where li.image.id in :imageIds
        group by li.image.id
    """)
    List<ImageCountView> countByImageIds(@Param("imageIds") List<UUID> imageIds);

    @Query("""
        select li.image.id
        from LikeImage li
        where li.image.id in :imageIds and li.socialUser.id = :userId
    """)
    List<UUID> findLikedImageIdsByUser(@Param("imageIds") List<UUID> imageIds,
                                       @Param("userId") UUID userId);
}
