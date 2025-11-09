package renatius.imageservice_internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import renatius.imageservice_internship.dto.CommentImageDto;
import renatius.imageservice_internship.dto.CommentResponseDto;
import renatius.imageservice_internship.entities.CommentImage;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.exceptions.CommentImageNotFoundException;
import renatius.imageservice_internship.exceptions.ImageNotFoundException;
import renatius.imageservice_internship.integration.BaseIntegrationTest;
import renatius.imageservice_internship.mapper.CommentMapper;
import renatius.imageservice_internship.repository.CommentImageRepository;
import renatius.imageservice_internship.repository.ImageRepository;
import renatius.imageservice_internship.repository.LikeCommentRepository;
import renatius.imageservice_internship.service.impl.CommentServiceImpl;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommentServiceImplTest  {

    private CommentImageRepository commentImageRepository;
    private LikeCommentRepository likeCommentRepository;
    private CommentMapper commentMapper;
    private ImageRepository imageRepository;
    private SecurityContextHolderUtil securityContextHolderUtil;
    private CommentServiceImpl service;
    private SocialUser mockUser;
    private Image mockImage;

    @BeforeEach
    void setup() {
        commentImageRepository = mock(CommentImageRepository.class);
        likeCommentRepository = mock(LikeCommentRepository.class);
        commentMapper = mock(CommentMapper.class);
        imageRepository = mock(ImageRepository.class);
        securityContextHolderUtil = mock(SecurityContextHolderUtil.class);
        service = new CommentServiceImpl(
                commentImageRepository,
                likeCommentRepository,
                commentMapper,
                imageRepository,
                securityContextHolderUtil
        );
        mockUser = SocialUser.builder()
                .id(UUID.randomUUID())
                .username("user")
                .build();
        mockImage = Image.builder()
                .id(UUID.randomUUID())
                .build();
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(mockUser);
    }

    @Test
    void addCommentToImage_success() {
        UUID imageId = mockImage.getId();
        CommentImageDto dto = new CommentImageDto("hello");
        CommentImage savedComment = CommentImage.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .image(mockImage)
                .build();
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(mockImage));
        when(commentImageRepository.save(any())).thenReturn(savedComment);
        when(commentMapper.toDto(savedComment)).thenReturn(new CommentResponseDto());
        CommentResponseDto result = service.addCommentToImage(imageId, dto);
        assertNotNull(result);
        verify(commentImageRepository, times(1)).save(any());
    }

    @Test
    void addCommentToImage_imageNotFound() {
        UUID id = UUID.randomUUID();
        when(imageRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ImageNotFoundException.class, () ->
                service.addCommentToImage(id, new CommentImageDto("test"))
        );
    }

    @Test
    void updateComment_success() {
        UUID commentId = UUID.randomUUID();
        CommentImageDto dto = new CommentImageDto("new text");
        CommentImage existing = CommentImage.builder()
                .id(commentId)
                .user(mockUser)
                .description("old")
                .build();
        when(commentImageRepository.findById(commentId)).thenReturn(Optional.of(existing));
        when(commentMapper.toDto(existing)).thenReturn(new CommentResponseDto());
        CommentResponseDto result = service.updateCommentImage(commentId, dto);
        assertEquals("new text", existing.getDescription());
        verify(commentImageRepository).save(existing);
        assertNotNull(result);
    }

    @Test
    void updateComment_commentNotFound() {
        UUID id = UUID.randomUUID();
        when(commentImageRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(CommentImageNotFoundException.class, () ->
                service.updateCommentImage(id, new CommentImageDto("x"))
        );
    }

    @Test
    void updateComment_otherUser_throws() {
        UUID id = UUID.randomUUID();
        SocialUser other = SocialUser.builder().id(UUID.randomUUID()).build();
        CommentImage comment = CommentImage.builder()
                .id(id)
                .user(other)
                .description("x")
                .build();
        when(commentImageRepository.findById(id)).thenReturn(Optional.of(comment));
        assertThrows(SecurityException.class, () ->
                service.updateCommentImage(id, new CommentImageDto("new"))
        );
    }

    @Test
    void removeComment_success() {
        UUID id = UUID.randomUUID();
        CommentImage comment = CommentImage.builder()
                .id(id)
                .user(mockUser)
                .build();
        when(commentImageRepository.findById(id)).thenReturn(Optional.of(comment));
        boolean result = service.removeCommentFromImage(id);
        assertTrue(result);
        verify(commentImageRepository).delete(comment);
    }

    @Test
    void removeComment_notFound() {
        UUID id = UUID.randomUUID();
        when(commentImageRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(CommentImageNotFoundException.class, () ->
                service.removeCommentFromImage(id)
        );
    }

    @Test
    void removeComment_otherUser() {
        UUID id = UUID.randomUUID();
        SocialUser other = SocialUser.builder().id(UUID.randomUUID()).build();
        CommentImage comment = CommentImage.builder()
                .id(id)
                .user(other)
                .build();
        when(commentImageRepository.findById(id)).thenReturn(Optional.of(comment));
        assertThrows(SecurityException.class, () ->
                service.removeCommentFromImage(id)
        );
    }

    @Test
    void getCommentsByImage_success() {
        UUID imageId = UUID.randomUUID();
        CommentImage c1 = CommentImage.builder().id(UUID.randomUUID()).build();
        CommentImage c2 = CommentImage.builder().id(UUID.randomUUID()).build();
        when(commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(imageId))
                .thenReturn(List.of(c1, c2));
        when(commentMapper.toDto(any())).thenReturn(new CommentResponseDto());
        List<CommentResponseDto> list = service.getCommentsByImage(imageId);
        assertEquals(2, list.size());
        verify(commentMapper, times(2)).toDto(any());
    }

    @Test
    void mapToResponseDto_setsLikesCorrectly() {
        CommentImage comment = CommentImage.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .build();
        when(commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(any()))
                .thenReturn(List.of(comment));
        CommentResponseDto mapped = new CommentResponseDto();
        when(commentMapper.toDto(comment)).thenReturn(mapped);
        when(likeCommentRepository.countByCommentImage_Id(comment.getId())).thenReturn(10L);
        when(likeCommentRepository.existsByCommentImage_IdAndSocialUser_Id(comment.getId(), mockUser.getId()))
                .thenReturn(true);
        List<CommentResponseDto> result = service.getCommentsByImage(UUID.randomUUID());
        assertEquals(10L, result.get(0).getLikesCount());
        assertTrue(result.get(0).isLikedByCurrentUser());
    }

}
