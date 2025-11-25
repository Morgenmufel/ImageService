package renatius.imageservice_internship.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import renatius.imageservice_internship.dto.ImageResponseDto;
import renatius.imageservice_internship.dto.PagedResponseDto;
import renatius.imageservice_internship.dto.SocialUserResponseDto;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.exceptions.SocialUserAlreadyExistsException;
import renatius.imageservice_internship.exceptions.SocialUserNotFoundException;
import renatius.imageservice_internship.mapper.ImageMapper;
import renatius.imageservice_internship.mapper.SocialUserMapper;
import renatius.imageservice_internship.repository.ImageRepository;
import renatius.imageservice_internship.repository.SocialUserRepository;
import renatius.imageservice_internship.service.SocialUserService;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialUserServiceImpl implements SocialUserService {

    private final SocialUserRepository socialUserRepository;
    private final SocialUserMapper socialUserMapper;
    private final ImageMapper imageMapper;
    private final ImageRepository imageRepository;

    @Override
    public boolean addUser(UUID id, String username) {
        if(socialUserRepository.existsByUsername(username)) {
           throw new SocialUserAlreadyExistsException("Username already exists");
        }
        SocialUser socialUser = SocialUser.builder()
                    .id(id)
                    .username(username)
                    .build();
        socialUserRepository.save(socialUser);
        return true;
    }

    @Override
    public SocialUserResponseDto getUserProfile(UUID userId, Pageable pageable) {
        SocialUser user = socialUserRepository.findById(userId)
                .orElseThrow(() -> new SocialUserNotFoundException("User not found"));
        SocialUserResponseDto socialUserResponseDto = socialUserMapper.toDto(user);
        Page<Image> imagePage = imageRepository.findAllByUser(user, pageable);
        List<ImageResponseDto> imageDtos = imagePage.getContent()
                .stream()
                .map(imageMapper::toDto)
                .toList();
        PagedResponseDto<ImageResponseDto> pagedImages = PagedResponseDto.<ImageResponseDto>builder()
                .content(imageDtos)
                .currentPage(imagePage.getNumber())
                .totalPages(imagePage.getTotalPages())
                .totalElements(imagePage.getTotalElements())
                .first(imagePage.isFirst())
                .last(imagePage.isLast())
                .build();
        socialUserResponseDto.setImages(pagedImages);
        return socialUserResponseDto;
    }

    @Override
    public SocialUser getSocialUser(UUID id) {
        return socialUserRepository.findById(id)
                .orElseThrow(() -> new SocialUserNotFoundException("User not found"));
    }
}
