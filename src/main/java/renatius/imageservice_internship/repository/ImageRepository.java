package renatius.imageservice_internship.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
    Page<Image> findAll(Pageable pageable);
    Page<Image> findAllByUser(SocialUser user, Pageable pageable);
}
