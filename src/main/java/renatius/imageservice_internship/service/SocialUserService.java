package renatius.imageservice_internship.service;


import renatius.imageservice_internship.dto.SocialUserResponseDto;
import renatius.imageservice_internship.entities.SocialUser;

import java.util.UUID;

public interface SocialUserService {
     boolean addUser(UUID id, String username);
     SocialUserResponseDto getUserProfile(UUID userId, int page, int size);
     SocialUser getSocialUser(UUID id);
}
