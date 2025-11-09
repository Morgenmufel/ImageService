package renatius.imageservice_internship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renatius.imageservice_internship.entities.SocialUser;

import java.util.UUID;

public interface SocialUserRepository extends JpaRepository<SocialUser, UUID> {
    boolean existsByUsername(String username);
}
