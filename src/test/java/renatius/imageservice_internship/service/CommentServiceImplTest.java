package renatius.imageservice_internship.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import renatius.imageservice_internship.dto.CommentImageDto;
import renatius.imageservice_internship.dto.CommentResponseDto;
import renatius.imageservice_internship.entities.CommentImage;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.exceptions.CommentImageNotFoundException;
import renatius.imageservice_internship.exceptions.ImageNotFoundException;
import renatius.imageservice_internship.mapper.CommentMapper;
import renatius.imageservice_internship.repository.CommentImageRepository;
import renatius.imageservice_internship.repository.ImageRepository;
import renatius.imageservice_internship.repository.LikeCommentRepository;
import renatius.imageservice_internship.service.impl.CommentServiceImpl;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceImplTest {

    @Mock private CommentImageRepository commentImageRepository;
    @Mock private LikeCommentRepository likeCommentRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private ImageRepository imageRepository;
    @Mock private SecurityContextHolderUtil securityContextHolderUtil;

    @InjectMocks private CommentServiceImpl commentService;

    private SocialUser testUser;
    private Image testImage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = SocialUser.builder().id(UUID.randomUUID()).username("bob").build();
        testImage = Image.builder().id(UUID.randomUUID()).build();
    }

    @Test
    void addCommentToImage_whenImageNotFound_thenThrow() {
        UUID imageId = UUID.randomUUID();
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        CommentImageDto dto = new CommentImageDto();
        dto.setDescription("hi");

        assertThatThrownBy(() -> commentService.addCommentToImage(imageId, dto))
                .isInstanceOf(ImageNotFoundException.class);

        verify(commentImageRepository, never()).save(any());
    }

    @Test
    void addCommentToImage_success() {
        UUID imageId = testImage.getId();
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(testImage));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);

        CommentImageDto request = new CommentImageDto();
        request.setDescription("hello");

        ArgumentCaptor<CommentImage> captor = ArgumentCaptor.forClass(CommentImage.class);
        CommentImage saved = CommentImage.builder()
                .id(UUID.randomUUID())
                .description("hello")
                .image(testImage)
                .user(testUser)
                .build();
        when(commentImageRepository.save(any())).thenReturn(saved);
        when(commentMapper.toDto(any())).thenReturn(new CommentResponseDto());

        CommentResponseDto resp = commentService.addCommentToImage(imageId, request);

        assertThat(resp).isNotNull();
        verify(commentImageRepository).save(captor.capture());
        CommentImage persisted = captor.getValue();
        assertThat(persisted.getDescription()).isEqualTo("hello");
        assertThat(persisted.getImage()).isEqualTo(testImage);
        assertThat(persisted.getUser()).isEqualTo(testUser);
    }

    @Test
    void updateCommentImage_whenNotFound_thenThrow() {
        UUID commentId = UUID.randomUUID();
        when(commentImageRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.updateCommentImage(commentId, new CommentImageDto()))
                .isInstanceOf(CommentImageNotFoundException.class);
    }

    @Test
    void updateCommentImage_whenNotOwner_thenThrowSecurityException() {
        UUID commentId = UUID.randomUUID();
        SocialUser owner = SocialUser.builder().id(UUID.randomUUID()).username("owner").build();
        CommentImage comment = CommentImage.builder()
                .id(commentId)
                .user(owner)
                .description("old")
                .build();
        when(commentImageRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);

        CommentImageDto dto = new CommentImageDto();
        dto.setDescription("new");

        assertThatThrownBy(() -> commentService.updateCommentImage(commentId, dto))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void getCommentsByImage_mapsLikesAndFlags() {
        UUID imageId = testImage.getId();
        CommentImage c1 = CommentImage.builder().id(UUID.randomUUID()).description("c1").user(testUser).image(testImage).build();
        when(commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(imageId)).thenReturn(List.of(c1));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);
        when(commentMapper.toDto(c1)).thenReturn(new CommentResponseDto());
        when(likeCommentRepository.countByCommentImage_Id(c1.getId())).thenReturn(5L);
        when(likeCommentRepository.existsByCommentImage_IdAndSocialUser_Id(c1.getId(), testUser.getId()))
                .thenReturn(true);

        var list = commentService.getCommentsByImage(imageId);

        assertThat(list).hasSize(1);
        verify(likeCommentRepository).countByCommentImage_Id(c1.getId());
    }
}
