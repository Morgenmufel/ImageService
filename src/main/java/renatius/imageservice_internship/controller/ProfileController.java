package renatius.imageservice_internship.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import renatius.imageservice_internship.dto.SocialUserResponseDto;
import renatius.imageservice_internship.service.SocialUserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final SocialUserService socialUserService;

    @PostMapping("/{userId}")
    public ResponseEntity<SocialUserResponseDto> getProfile(@PathVariable UUID userId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(socialUserService.getUserProfile(userId, page, size));
    }

}
