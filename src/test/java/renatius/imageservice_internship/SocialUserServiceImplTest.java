package renatius.imageservice_internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import renatius.imageservice_internship.dto.ImageResponseDto;
import renatius.imageservice_internship.dto.PagedResponseDto;
import renatius.imageservice_internship.dto.SocialUserResponseDto;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.exceptions.SocialUserAlreadyExistsException;
import renatius.imageservice_internship.exceptions.SocialUserNotFoundException;
import renatius.imageservice_internship.integration.BaseIntegrationTest;
import renatius.imageservice_internship.mapper.ImageMapper;
import renatius.imageservice_internship.mapper.SocialUserMapper;
import renatius.imageservice_internship.repository.ImageRepository;
import renatius.imageservice_internship.repository.SocialUserRepository;
import renatius.imageservice_internship.service.impl.SocialUserServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SocialUserServiceImplTest {

    @Mock
    private SocialUserRepository socialUserRepository;

    @Mock
    private SocialUserMapper socialUserMapper;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private SocialUserServiceImpl socialUserService;

    private UUID userId;
    private SocialUser user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        user = SocialUser.builder()
                .id(userId)
                .username("john")
                .build();
    }

    @Test
    void addUser_shouldAddSuccessfully() {
        when(socialUserRepository.existsByUsername("john")).thenReturn(false);

        boolean result = socialUserService.addUser(userId, "john");

        assertTrue(result);

        ArgumentCaptor<SocialUser> captor = ArgumentCaptor.forClass(SocialUser.class);
        verify(socialUserRepository).save(captor.capture());

        SocialUser saved = captor.getValue();
        assertEquals("john", saved.getUsername());
        assertEquals(userId, saved.getId());
    }

    @Test
    void addUser_shouldThrowIfUsernameExists() {
        when(socialUserRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(SocialUserAlreadyExistsException.class,
                () -> socialUserService.addUser(userId, "john"));

        verify(socialUserRepository, never()).save(any());
    }


    @Test
    void getUserProfile_shouldReturnProfileWithImages() {
        when(socialUserRepository.findById(userId)).thenReturn(Optional.of(user));

        SocialUserResponseDto userDto = new SocialUserResponseDto();
        userDto.setId(userId);
        userDto.setUsername("john");

        when(socialUserMapper.toDto(user)).thenReturn(userDto);

        Image img = Image.builder().id(UUID.randomUUID()).build();
        Page<Image> page = new PageImpl<>(List.of(img), PageRequest.of(0, 10), 1);

        when(imageRepository.findAllByUser(eq(user), any(Pageable.class)))
                .thenReturn(page);

        ImageResponseDto imgDto = new ImageResponseDto();
        imgDto.setId(img.getId());

        when(imageMapper.toDto(img)).thenReturn(imgDto);

        SocialUserResponseDto result = socialUserService.getUserProfile(userId, 0, 10);

        assertEquals(userId, result.getId());
        assertEquals("john", result.getUsername());

        PagedResponseDto<ImageResponseDto> images = result.getImages();
        assertNotNull(images);
        assertEquals(1, images.getTotalElements());
        assertEquals(1, images.getContent().size());
        assertEquals(img.getId(), images.getContent().get(0).getId());

        verify(socialUserMapper).toDto(user);
        verify(imageMapper).toDto(img);
    }

    @Test
    void getUserProfile_shouldThrowIfUserNotFound() {
        when(socialUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(SocialUserNotFoundException.class,
                () -> socialUserService.getUserProfile(userId, 0, 10));
    }

    @Test
    void getSocialUser_shouldReturnUser() {
        when(socialUserRepository.findById(userId)).thenReturn(Optional.of(user));

        SocialUser result = socialUserService.getSocialUser(userId);

        assertEquals(userId, result.getId());
    }

    @Test
    void getSocialUser_shouldThrowIfNotFound() {
        when(socialUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(SocialUserNotFoundException.class,
                () -> socialUserService.getSocialUser(userId));
    }
}

