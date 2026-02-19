package renatius.imageservice_internship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import renatius.imageservice_internship.entities.CommentImage;
import renatius.imageservice_internship.service.ImageCountView;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentImageRepository extends JpaRepository<CommentImage, UUID> {
    List<CommentImage> findAllByImage_IdOrderByCreatedAtDesc(UUID imageId);
    long countByImage_Id(UUID imageId);
    @Query("""
        select c.image.id as imageId, count(c.id) as cnt
        from CommentImage c
        where c.image.id in :imageIds
        group by c.image.id
    """)
    List<ImageCountView> countByImageIds(@Param("imageIds") List<UUID> imageIds);
}
