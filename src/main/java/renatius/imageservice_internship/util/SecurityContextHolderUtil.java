package renatius.imageservice_internship.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.security.CustomUserPrincipal;
import renatius.imageservice_internship.service.SocialUserService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityContextHolderUtil {

    private final SocialUserService socialUserService;

    public SocialUser getCurrentUser() {
        CustomUserPrincipal principal =
                (CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = principal.id();
        SocialUser user = socialUserService.getSocialUser(userId);
        return user;
    }
}
