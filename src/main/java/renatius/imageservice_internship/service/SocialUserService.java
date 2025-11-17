package renatius.imageservice_internship.service;


import org.springframework.data.domain.Pageable;
import renatius.imageservice_internship.dto.SocialUserResponseDto;
import renatius.imageservice_internship.entities.SocialUser;

import java.util.UUID;

public interface SocialUserService {
     boolean addUser(UUID id, String username);
     SocialUserResponseDto getUserProfile(UUID userId, Pageable pageable);
     SocialUser getSocialUser(UUID id);
}
