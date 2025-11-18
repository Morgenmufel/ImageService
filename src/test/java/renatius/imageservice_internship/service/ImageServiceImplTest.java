package renatius.imageservice_internship.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;
import renatius.imageservice_internship.dto.ImageUploadRequest;
import renatius.imageservice_internship.dto.ImageResponseDto;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.exceptions.ImageNotFoundException;
import renatius.imageservice_internship.exceptions.S3IOException;
import renatius.imageservice_internship.mapper.CommentMapper;
import renatius.imageservice_internship.mapper.ImageMapper;
import renatius.imageservice_internship.repository.*;
import renatius.imageservice_internship.service.impl.ImageServiceImpl;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageServiceImplTest {

    @Mock private ImageRepository imageRepository;
    @Mock private ImageMapper imageMapper;
    @Mock private CommentMapper commentMapper;
    @Mock private S3Service s3Service;
    @Mock private LikeCommentRepository likeCommentRepository;
    @Mock private SecurityContextHolderUtil securityContextHolderUtil;
    @Mock private LikeImageRepository likeImageRepository;
    @Mock private SocialUserService socialUserService;
    @Mock private CommentImageRepository commentImageRepository;

    @InjectMocks private ImageServiceImpl imageService;

    private SocialUser currentUser;
    private Image imageEntity;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        currentUser = SocialUser.builder().id(UUID.randomUUID()).username("cur").build();
        imageEntity = Image.builder().id(UUID.randomUUID())
                .user(currentUser)
                .url("http://localhost:4566/bucket/" + UUID.randomUUID() + "_file.jpg")
                .build();
    }

    @Test
    void getImageById_whenMissing_thenThrow() {
        UUID id = UUID.randomUUID();
        when(imageRepository.findById(id)).thenReturn(Optional.empty());
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> imageService.getImageById(id)).isInstanceOf(ImageNotFoundException.class);
    }

    @Test
    void getImageById_success() {
        UUID id = imageEntity.getId();
        when(imageRepository.findById(id)).thenReturn(Optional.of(imageEntity));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(imageMapper.toDto(imageEntity)).thenReturn(new ImageResponseDto());
        when(likeImageRepository.countByImage_Id(id)).thenReturn(2L);
        when(commentImageRepository.countByImage_Id(id)).thenReturn(3L);
        when(likeImageRepository.existsByImage_IdAndSocialUser_Id(id, currentUser.getId())).thenReturn(true);
        when(commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(id)).thenReturn(List.of());

        var dto = imageService.getImageById(id);
        assertThat(dto).isNotNull();
        assertThat(dto.getLikesCount()).isEqualTo(2L);
        assertThat(dto.getCommentsCount()).isEqualTo(3L);
    }

    @Test
    void deleteImageById_whenNotOwner_thenThrowAccessDenied() {
        UUID id = imageEntity.getId();
        SocialUser other = SocialUser.builder().id(UUID.randomUUID()).username("other").build();
        imageEntity.setUser(other);
        when(imageRepository.findById(id)).thenReturn(Optional.of(imageEntity));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> imageService.deleteImageById(id)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteImageById_success_callsS3AndDeletesEntity() {
        UUID id = imageEntity.getId();
        when(imageRepository.findById(id)).thenReturn(Optional.of(imageEntity));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(s3Service.extractKeyFromUrl(imageEntity.getUrl())).thenReturn("key.jpg");

        imageService.deleteImageById(id);

        verify(s3Service).deleteFileFromS3("key.jpg");
        verify(imageRepository).delete(imageEntity);
    }

    @Test
    void uploadSingleImage_whenS3IOException_thenThrowS3IOException() throws IOException {
        ImageUploadRequest req = new ImageUploadRequest();
        MultipartFile file = mock(MultipartFile.class);
        req.setFile(file);
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(imageMapper.toEntity(req)).thenReturn(Image.builder().build());
        when(s3Service.uploadFileToS3(any(), eq(file))).thenThrow(new IOException("boom"));

        assertThatThrownBy(() -> imageService.uploadSingleImage(req)).isInstanceOf(S3IOException.class);
    }

    @Test
    void uploadSingleImage_success() throws IOException {
        ImageUploadRequest req = new ImageUploadRequest();
        MultipartFile file = mock(MultipartFile.class);
        req.setFile(file);
        Image toSave = Image.builder().build();
        Image saved = Image.builder().id(UUID.randomUUID()).url("http://localhost:4566/b/b_key").user(currentUser).build();

        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(imageMapper.toEntity(req)).thenReturn(toSave);
        when(s3Service.uploadFileToS3(any(), eq(file))).thenReturn("http://localhost:4566/b/b_key");
        when(imageRepository.save(any())).thenReturn(saved);
        when(imageMapper.toDto(any())).thenReturn(new ImageResponseDto());

        var dto = imageService.uploadSingleImage(req);
        assertThat(dto).isNotNull();
        verify(imageRepository).save(any());
    }
}

