package renatius.imageservice_internship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import renatius.imageservice_internship.entities.CommentImage;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentImageRepository extends JpaRepository<CommentImage, UUID> {
    List<CommentImage> findAllByImage_IdOrderByCreatedAtDesc(UUID imageId);
    long countByImage_Id(UUID imageId);
}
