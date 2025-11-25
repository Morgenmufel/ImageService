package renatius.imageservice_internship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import renatius.imageservice_internship.entities.SocialUser;

import java.util.UUID;

@Repository
public interface SocialUserRepository extends JpaRepository<SocialUser, UUID> {
    boolean existsByUsername(String username);
}
