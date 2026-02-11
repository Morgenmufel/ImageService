package renatius.imageservice_internship.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;
import renatius.imageservice_internship.dto.CommentResponseDto;
import renatius.imageservice_internship.dto.ImageUploadRequest;
import renatius.imageservice_internship.dto.ImageResponseDto;
import renatius.imageservice_internship.dto.PagedResponseDto;
import renatius.imageservice_internship.entities.CommentImage;
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
import static org.junit.jupiter.api.Assertions.*;
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
    void getAllImagesPaginated_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Image> page = new PageImpl<>(List.of(imageEntity), pageable, 1);
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(imageRepository.findAll(pageable)).thenReturn(page);
        when(imageMapper.toDto(any(Image.class))).thenReturn(new ImageResponseDto());
        when(likeImageRepository.existsByImage_IdAndSocialUser_Id(any(), any()))
                .thenReturn(false);
        when(likeImageRepository.countByImage_Id(any())).thenReturn(0L);
        when(commentImageRepository.countByImage_Id(any())).thenReturn(0L);
        PagedResponseDto<ImageResponseDto> response =
                imageService.getAllImagesPaginated(pageable);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getAllImagesPaginated_emptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(imageRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));
        PagedResponseDto<ImageResponseDto> response =
                imageService.getAllImagesPaginated(pageable);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    void getImagesByUserPaginated_success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(socialUserService.getSocialUser(any())).thenReturn(currentUser);
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        Page<Image> page = new PageImpl<>(List.of(imageEntity), pageable, 1);
        when(imageRepository.findAllByUser(currentUser, pageable)).thenReturn(page);
        when(imageMapper.toDto(any(Image.class))).thenReturn(new ImageResponseDto());
        when(likeImageRepository.existsByImage_IdAndSocialUser_Id(any(), any()))
                .thenReturn(true);
        when(likeImageRepository.countByImage_Id(any())).thenReturn(3L);
        when(commentImageRepository.countByImage_Id(any())).thenReturn(1L);
        PagedResponseDto<ImageResponseDto> response =
                imageService.getImagesByUserPaginated(currentUser.getId(), pageable);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void getImagesByUserPaginated_empty() {
        Pageable pageable = PageRequest.of(0, 5);
        when(socialUserService.getSocialUser(currentUser.getId())).thenReturn(currentUser);
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(imageRepository.findAllByUser(currentUser, pageable))
                .thenReturn(Page.empty(pageable));
        PagedResponseDto<ImageResponseDto> response =
                imageService.getImagesByUserPaginated(currentUser.getId(), pageable);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    void getImageById_success() {
        CommentImage comment = CommentImage.builder()
                .id(UUID.randomUUID())
                .build();
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(imageRepository.findById(imageEntity.getId())).thenReturn(Optional.of(imageEntity));
        when(imageMapper.toDto(imageEntity)).thenReturn(new ImageResponseDto());
        when(likeImageRepository.countByImage_Id(imageEntity.getId())).thenReturn(5L);
        when(commentImageRepository.countByImage_Id(imageEntity.getId())).thenReturn(2L);
        when(likeImageRepository.existsByImage_IdAndSocialUser_Id(imageEntity.getId(), currentUser.getId()))
                .thenReturn(true);
        when(commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(imageEntity.getId()))
                .thenReturn(List.of(comment));
        when(commentMapper.toDto(comment)).thenReturn(new CommentResponseDto());
        when(likeCommentRepository.countByCommentImage_Id(any())).thenReturn(1L);
        when(likeCommentRepository.existsByCommentImage_IdAndSocialUser_Id(any(), eq(currentUser.getId())))
                .thenReturn(false);
        ImageResponseDto response = imageService.getImageById(imageEntity.getId());
        assertNotNull(response);
    }

    @Test
    void getImageById_notFound() {
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(imageRepository.findById(imageEntity.getId())).thenReturn(Optional.empty());
        assertThrows(ImageNotFoundException.class,
                () -> imageService.getImageById(imageEntity.getId()));
    }

    @Test
    void deleteImageById_success() {
        when(imageRepository.findById(imageEntity.getId())).thenReturn(Optional.of(imageEntity));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(s3Service.extractKeyFromUrl(any())).thenReturn("_file.jpg");
        doNothing().when(s3Service).deleteFileFromS3(any());
        assertDoesNotThrow(() -> imageService.deleteImageById(imageEntity.getId()));
        verify(imageRepository).delete(imageEntity);
    }

    @Test
    void deleteImageById_accessDenied() {
        UUID imageId = UUID.randomUUID();
        SocialUser owner = new SocialUser();
        owner.setId(UUID.randomUUID());

        SocialUser currentUser = new SocialUser();
        currentUser.setId(UUID.randomUUID());

        Image image = new Image();
        image.setUser(owner);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);

        assertThrows(AccessDeniedException.class,
                () -> imageService.deleteImageById(imageId));
    }

    @Test
    void uploadSingleImage_success() throws Exception {
        ImageUploadRequest request = mock(ImageUploadRequest.class);
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(currentUser);
        when(imageMapper.toEntity(request)).thenReturn(imageEntity);
        when(s3Service.uploadFileToS3(any(), any())).thenReturn("https://s3/img.png");
        when(imageMapper.toDto(any(Image.class))).thenReturn(new ImageResponseDto());
        ImageResponseDto response = imageService.uploadSingleImage(request);
        assertNotNull(response);
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void uploadSingleImage_s3Error() throws Exception {
        ImageUploadRequest request = mock(ImageUploadRequest.class);
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(new SocialUser());
        when(imageMapper.toEntity(request)).thenReturn(new Image());
        when(s3Service.uploadFileToS3(any(), any()))
                .thenThrow(new IOException());
        assertThrows(S3IOException.class,
                () -> imageService.uploadSingleImage(request));
    }

}

