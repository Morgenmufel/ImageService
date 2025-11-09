package renatius.imageservice_internship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renatius.imageservice_internship.entities.CommentImage;
import java.util.List;
import java.util.UUID;

public interface CommentImageRepository extends JpaRepository<CommentImage, UUID> {
    List<CommentImage> findAllByImage_IdOrderByCreatedAtDesc(UUID imageId);
    long countByImage_Id(UUID imageId);
}
