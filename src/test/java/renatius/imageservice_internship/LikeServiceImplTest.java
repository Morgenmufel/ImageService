package renatius.imageservice_internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import renatius.imageservice_internship.dto.LikeResponseDto;
import renatius.imageservice_internship.entities.*;
import renatius.imageservice_internship.exceptions.CommentImageNotFoundException;
import renatius.imageservice_internship.exceptions.ImageNotFoundException;
import renatius.imageservice_internship.integration.BaseIntegrationTest;
import renatius.imageservice_internship.repository.*;
import renatius.imageservice_internship.service.impl.LikeServiceImpl;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LikeServiceImplTest  {

    @Mock
    private LikeImageRepository likeImageRepository;
    @Mock
    private LikeCommentRepository likeCommentRepository;
    @Mock
    private CommentImageRepository commentImageRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private SecurityContextHolderUtil securityContextHolderUtil;

    @InjectMocks
    private LikeServiceImpl likeService;

    private SocialUser user;
    private Image image;
    private CommentImage comment;
    private UUID imageId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = SocialUser.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .build();

        imageId = UUID.randomUUID();
        image = Image.builder()
                .id(imageId)
                .build();

        commentId = UUID.randomUUID();
        comment = CommentImage.builder()
                .id(commentId)
                .build();

        when(securityContextHolderUtil.getCurrentUser()).thenReturn(user);
    }


    @Test
    void toogleLikeToImage_shouldLikeImage() {
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(likeImageRepository.findByImage_IdAndSocialUser_Id(imageId, user.getId()))
                .thenReturn(Optional.empty());
        when(likeImageRepository.countByImage_Id(imageId)).thenReturn(1L);

        LikeResponseDto response = likeService.toogleLikeToImage(imageId);

        assertTrue(response.isLiked());
        assertEquals(1L, response.getLikesCount());
        assertEquals(imageId, response.getTargetId());

        verify(likeImageRepository).save(any(LikeImage.class));
        verify(likeImageRepository, never()).delete(any());
    }

    @Test
    void toogleLikeToImage_shouldUnlikeImage() {
        LikeImage like = LikeImage.builder().id(UUID.randomUUID()).image(image).socialUser(user).build();

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(likeImageRepository.findByImage_IdAndSocialUser_Id(imageId, user.getId()))
                .thenReturn(Optional.of(like));
        when(likeImageRepository.countByImage_Id(imageId)).thenReturn(0L);

        LikeResponseDto response = likeService.toogleLikeToImage(imageId);

        assertFalse(response.isLiked());
        assertEquals(0L, response.getLikesCount());

        verify(likeImageRepository).delete(like);
        verify(likeImageRepository, never()).save(any());
    }

    @Test
    void toogleLikeToImage_shouldThrowImageNotFound() {
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class,
                () -> likeService.toogleLikeToImage(imageId));

        verify(likeImageRepository, never()).save(any());
    }


    @Test
    void toogleLikeToComment_shouldLikeComment() {
        when(commentImageRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(likeCommentRepository.findByCommentImage_IdAndSocialUser_Id(commentId, user.getId()))
                .thenReturn(Optional.empty());
        when(likeCommentRepository.countByCommentImage_Id(commentId)).thenReturn(1L);

        LikeResponseDto response = likeService.toogleLikeToComment(commentId);

        assertTrue(response.isLiked());
        assertEquals(1L, response.getLikesCount());
        assertEquals(commentId, response.getTargetId());

        verify(likeCommentRepository).save(any(LikeComment.class));
        verify(likeCommentRepository, never()).delete(any());
    }

    @Test
    void toogleLikeToComment_shouldUnlikeComment() {
        LikeComment like = LikeComment.builder().id(UUID.randomUUID()).commentImage(comment).socialUser(user).build();

        when(commentImageRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(likeCommentRepository.findByCommentImage_IdAndSocialUser_Id(commentId, user.getId()))
                .thenReturn(Optional.of(like));
        when(likeCommentRepository.countByCommentImage_Id(commentId)).thenReturn(0L);

        LikeResponseDto response = likeService.toogleLikeToComment(commentId);

        assertFalse(response.isLiked());
        assertEquals(0L, response.getLikesCount());

        verify(likeCommentRepository).delete(like);
        verify(likeCommentRepository, never()).save(any());
    }

    @Test
    void toogleLikeToComment_shouldThrowCommentNotFound() {
        when(commentImageRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentImageNotFoundException.class,
                () -> likeService.toogleLikeToComment(commentId));

        verify(likeCommentRepository, never()).save(any());
    }
}

