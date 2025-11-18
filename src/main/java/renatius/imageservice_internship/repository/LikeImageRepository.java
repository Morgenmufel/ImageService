package renatius.imageservice_internship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import renatius.imageservice_internship.entities.LikeImage;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeImageRepository extends JpaRepository<LikeImage, UUID> {
    Optional<LikeImage> findByImage_IdAndSocialUser_Id(UUID imageId, UUID userId);
    boolean existsByImage_IdAndSocialUser_Id(UUID imageId, UUID userId);
    long countByImage_Id(UUID imageId);
}
