package renatius.imageservice_internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import renatius.imageservice_internship.dto.*;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.exceptions.ImageNotFoundException;
import renatius.imageservice_internship.exceptions.S3IOException;
import renatius.imageservice_internship.integration.BaseIntegrationTest;
import renatius.imageservice_internship.mapper.CommentMapper;
import renatius.imageservice_internship.mapper.ImageMapper;
import renatius.imageservice_internship.repository.*;
import renatius.imageservice_internship.service.S3Service;
import renatius.imageservice_internship.service.SocialUserService;
import renatius.imageservice_internship.service.impl.ImageServiceImpl;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageServiceImplTest  {

    @Mock private ImageRepository imageRepository;
    @Mock private ImageMapper imageMapper;
    @Mock private CommentMapper commentMapper;
    @Mock private S3Service s3Service;
    @Mock private LikeCommentRepository likeCommentRepository;
    @Mock private SecurityContextHolderUtil securityContextHolderUtil;
    @Mock private LikeImageRepository likeImageRepository;
    @Mock private SocialUserService socialUserService;
    @Mock private CommentImageRepository commentImageRepository;

    @InjectMocks
    private ImageServiceImpl imageService;

    private AutoCloseable closeable;

    private SocialUser testUser;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        testUser = new SocialUser();
        testUser.setId(UUID.randomUUID());
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    void getAllImagesPaginated_success() {
        Image image = new Image();
        image.setId(UUID.randomUUID());
        Page<Image> page = new PageImpl<>(List.of(image));

        when(imageRepository.findAll(any(Pageable.class))).thenReturn(page);

        ImageResponseDto dto = new ImageResponseDto();
        when(imageMapper.toDto(image)).thenReturn(dto);
        when(likeImageRepository.existsByImage_IdAndSocialUser_Id(any(), any())).thenReturn(true);
        when(likeImageRepository.countByImage_Id(any())).thenReturn(5L);
        when(commentImageRepository.countByImage_Id(any())).thenReturn(2L);

        PagedResponseDto<ImageResponseDto> result = imageService.getAllImagesPaginated(0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isLikedByCurrentUser());
    }

    @Test
    void getImagesByUserPaginated_success() {
        UUID userId = UUID.randomUUID();
        SocialUser targetUser = new SocialUser();
        when(socialUserService.getSocialUser(userId)).thenReturn(targetUser);

        Image image = new Image();
        image.setId(UUID.randomUUID());
        Page<Image> page = new PageImpl<>(List.of(image));

        when(imageRepository.findAllByUser(eq(targetUser), any(Pageable.class))).thenReturn(page);

        when(imageMapper.toDto(image)).thenReturn(new ImageResponseDto());
        when(likeImageRepository.existsByImage_IdAndSocialUser_Id(any(), any())).thenReturn(false);

        PagedResponseDto<ImageResponseDto> result = imageService.getImagesByUserPaginated(userId, 0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getImageById_success() {
        UUID id = UUID.randomUUID();

        Image image = new Image();
        image.setId(id);

        when(imageRepository.findById(id)).thenReturn(Optional.of(image));

        ImageResponseDto responseDto = new ImageResponseDto();
        when(imageMapper.toDto(image)).thenReturn(responseDto);

        when(likeImageRepository.countByImage_Id(id)).thenReturn(10L);
        when(commentImageRepository.countByImage_Id(id)).thenReturn(3L);
        when(likeImageRepository.existsByImage_IdAndSocialUser_Id(id, testUser.getId())).thenReturn(true);

        List<renatius.imageservice_internship.entities.CommentImage> commentList = new ArrayList<>();
        renatius.imageservice_internship.entities.CommentImage comment = new renatius.imageservice_internship.entities.CommentImage();
        comment.setId(UUID.randomUUID());
        commentList.add(comment);

        when(commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(id)).thenReturn(commentList);

        CommentResponseDto commentDto = new CommentResponseDto();
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        when(likeCommentRepository.countByCommentImage_Id(comment.getId())).thenReturn(2L);
        when(likeCommentRepository.existsByCommentImage_IdAndSocialUser_Id(comment.getId(), testUser.getId()))
                .thenReturn(true);

        ImageResponseDto result = imageService.getImageById(id);

        assertEquals(10L, result.getLikesCount());
        assertEquals(3L, result.getCommentsCount());
        assertTrue(result.isLikedByCurrentUser());
        assertEquals(1, result.getComments().size());
        assertTrue(result.getComments().get(0).isLikedByCurrentUser());
    }

    @Test
    void getImageById_notFound() {
        UUID id = UUID.randomUUID();
        when(imageRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class, () -> imageService.getImageById(id));
    }

    @Test
    void uploadSingleImage_success() throws IOException {
        ImageUploadRequest req = mock(ImageUploadRequest.class);
        when(req.getFile()).thenReturn(null);
        Image image = new Image();

        when(imageMapper.toEntity(req)).thenReturn(image);
        when(s3Service.uploadFileToS3(any(), any())).thenReturn("http://url");

        ImageResponseDto dto = new ImageResponseDto();
        when(imageMapper.toDto(image)).thenReturn(dto);

        ImageResponseDto result = imageService.uploadSingleImage(req);

        assertEquals("http://url", image.getUrl());
        verify(imageRepository).save(image);
    }

    @Test
    void uploadSingleImage_s3Error() throws IOException {
        ImageUploadRequest req = mock(ImageUploadRequest.class);
        Image image = new Image();

        when(imageMapper.toEntity(req)).thenReturn(image);
        when(s3Service.uploadFileToS3(any(), any())).thenThrow(new IOException());

        assertThrows(S3IOException.class, () -> imageService.uploadSingleImage(req));
    }

    @Test
    void deleteImageById_success() {
        UUID id = UUID.randomUUID();
        Image image = new Image();
        image.setId(id);
        image.setUrl("http://fake-url/key.jpg");

        when(imageRepository.findById(id)).thenReturn(Optional.of(image));
        when(s3Service.extractKeyFromUrl(image.getUrl())).thenReturn("key.jpg");

        boolean result = imageService.deleteImageById(id);

        assertTrue(result);
        verify(s3Service).deleteFileFromS3("key.jpg");
        verify(imageRepository).delete(image);
    }

    @Test
    void deleteImageById_notFound() {
        UUID id = UUID.randomUUID();
        when(imageRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class, () -> imageService.deleteImageById(id));
    }

}

