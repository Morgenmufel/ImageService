package renatius.imageservice_internship.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import renatius.imageservice_internship.entities.*;
import renatius.imageservice_internship.exceptions.CommentImageNotFoundException;
import renatius.imageservice_internship.exceptions.ImageNotFoundException;
import renatius.imageservice_internship.repository.*;
import renatius.imageservice_internship.service.impl.LikeServiceImpl;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LikeServiceImplTest {

    @Mock private LikeImageRepository likeImageRepository;
    @Mock private LikeCommentRepository likeCommentRepository;
    @Mock private CommentImageRepository commentImageRepository;
    @Mock private ImageRepository imageRepository;
    @Mock private SecurityContextHolderUtil securityContextHolderUtil;

    @InjectMocks private LikeServiceImpl likeService;

    private SocialUser user;
    private Image image;
    private CommentImage comment;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        user = SocialUser.builder().id(UUID.randomUUID()).username("u").build();
        image = Image.builder().id(UUID.randomUUID()).build();
        comment = CommentImage.builder().id(UUID.randomUUID()).build();
    }

    @Test
    void toogleLikeToImage_whenImageMissing_thenThrow() {
        UUID id = UUID.randomUUID();
        when(imageRepository.findById(id)).thenReturn(Optional.empty());
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(user);

        assertThatThrownBy(() -> likeService.toogleLikeToImage(id))
                .isInstanceOf(ImageNotFoundException.class);
    }

    @Test
    void toogleLikeToImage_whenNotLiked_thenCreatesLike() {
        UUID id = image.getId();
        when(imageRepository.findById(id)).thenReturn(Optional.of(image));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(user);
        when(likeImageRepository.findByImage_IdAndSocialUser_Id(id, user.getId())).thenReturn(Optional.empty());
        when(likeImageRepository.countByImage_Id(id)).thenReturn(1L);

        var resp = likeService.toogleLikeToImage(id);

        assertThat(resp.isLiked()).isTrue();
        assertThat(resp.getLikesCount()).isEqualTo(1L);
        verify(likeImageRepository).save(any());
    }

    @Test
    void toogleLikeToImage_whenAlreadyLiked_thenDeletesLike() {
        UUID id = image.getId();
        LikeImage existing = LikeImage.builder().id(UUID.randomUUID()).image(image).socialUser(user).build();
        when(imageRepository.findById(id)).thenReturn(Optional.of(image));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(user);
        when(likeImageRepository.findByImage_IdAndSocialUser_Id(id, user.getId())).thenReturn(Optional.of(existing));
        when(likeImageRepository.countByImage_Id(id)).thenReturn(0L);

        var resp = likeService.toogleLikeToImage(id);

        assertThat(resp.isLiked()).isFalse();
        assertThat(resp.getLikesCount()).isEqualTo(0L);
        verify(likeImageRepository).delete(existing);
    }

    @Test
    void toogleLikeToComment_whenCommentMissing_thenThrow() {
        UUID id = UUID.randomUUID();
        when(commentImageRepository.findById(id)).thenReturn(Optional.empty());
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(user);

        assertThatThrownBy(() -> likeService.toogleLikeToComment(id))
                .isInstanceOf(CommentImageNotFoundException.class);
    }

    @Test
    void toogleLikeToComment_createsAndDeletesAppropriately() {
        UUID id = comment.getId();
        when(commentImageRepository.findById(id)).thenReturn(Optional.of(comment));
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(user);
        when(likeCommentRepository.findByCommentImage_IdAndSocialUser_Id(id, user.getId())).thenReturn(Optional.empty());
        when(likeCommentRepository.countByCommentImage_Id(id)).thenReturn(1L);

        var resp = likeService.toogleLikeToComment(id);
        assertThat(resp.isLiked()).isTrue();
        assertThat(resp.getLikesCount()).isEqualTo(1L);
        verify(likeCommentRepository).save(any());
    }
}

