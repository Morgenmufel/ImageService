package renatius.imageservice_internship.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import renatius.imageservice_internship.dto.ActivityEvent;
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
import renatius.imageservice_internship.service.impl.KafkaProducerService;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceImplTest {

    @Mock private CommentImageRepository commentImageRepository;
    @Mock private LikeCommentRepository likeCommentRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private ImageRepository imageRepository;
    @Mock private SecurityContextHolderUtil securityContextHolderUtil;
    @Mock private KafkaProducerService kafkaProducerService;
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
    void addCommentImageTest_success() {
        UUID imageId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentImageDto dto = new CommentImageDto();
        dto.setDescription("Nice");
        CommentImage savedComment = CommentImage.builder()
                .id(commentId)
                .image(testImage)
                .user(testUser)
                .description("Nice")
                .build();
        CommentResponseDto mappedDto = new CommentResponseDto();
        mappedDto.setId(commentId);
        when(imageRepository.findById(imageId))
                .thenReturn(Optional.of(testImage));
        when(securityContextHolderUtil.getCurrentUser())
                .thenReturn(testUser);
        when(commentImageRepository.save(any()))
                .thenReturn(savedComment);
        when(commentMapper.toDto(savedComment))
                .thenReturn(mappedDto);
        when(likeCommentRepository.countByCommentImage_Id(commentId))
                .thenReturn(5L);
        when(likeCommentRepository
                .existsByCommentImage_IdAndSocialUser_Id(commentId, testUser.getId()))
                .thenReturn(true);
        ActivityEvent event = new ActivityEvent();
        when(kafkaProducerService.buildEvent(eq(testUser.getId().toString()),
                eq(testImage.getId().toString()),
                any(LocalDateTime.class),
                eq("NEW"),
                eq("ADD COMMENT")))
                .thenReturn(event);
        CommentResponseDto response =
                commentService.addCommentToImage(imageId, dto);
        assertThat(response.getLikesCount()).isEqualTo(5);
        assertThat(response.isLikedByCurrentUser()).isTrue();
        verify(likeCommentRepository)
                .countByCommentImage_Id(commentId);
        verify(likeCommentRepository)
                .existsByCommentImage_IdAndSocialUser_Id(commentId, testUser.getId());
    }

    @Test
    void addCommentImageTest_failure(){
        UUID imageId = UUID.randomUUID();
        CommentImageDto commentImageDto = new CommentImageDto();
        commentImageDto.setDescription("Test comment");
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());
        ImageNotFoundException exception = assertThrows(ImageNotFoundException.class, () -> {
            commentService.addCommentToImage(imageId, commentImageDto);
        });
        assertEquals("image not found", exception.getMessage());
        verify(imageRepository, times(1)).findById(imageId);
        verifyNoMoreInteractions(imageRepository, commentImageRepository, securityContextHolderUtil, kafkaProducerService);
    }

    @Test
    void updateCommentImage_success() {
        UUID commentId = UUID.randomUUID();
        CommentImage existingComment = CommentImage.builder()
                .id(commentId)
                .user(testUser)
                .image(testImage)
                .description("old description")
                .build();
        CommentImageDto updateDto = new CommentImageDto();
        updateDto.setDescription("new description");
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);
        when(commentImageRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        when(commentImageRepository.save(any(CommentImage.class))).thenReturn(existingComment);
        when(kafkaProducerService.buildEvent(
                eq(testUser.getId().toString()),
                eq(testImage.getId().toString()),
                any(LocalDateTime.class),
                eq("NEW"),
                eq("UPDATED COMMENT")))
                .thenReturn(new ActivityEvent());
        doNothing().when(kafkaProducerService).sendToCommentsTopic(any());
        when(commentMapper.toDto(existingComment)).thenReturn(new CommentResponseDto());
        when(likeCommentRepository.countByCommentImage_Id(commentId)).thenReturn(10L);
        when(likeCommentRepository.existsByCommentImage_IdAndSocialUser_Id(commentId, testUser.getId())).thenReturn(true);

        CommentResponseDto response = commentService.updateCommentImage(commentId, updateDto);

        assertNotNull(response);
        verify(commentImageRepository).save(existingComment);
        assertEquals("new description", existingComment.getDescription());
        verify(kafkaProducerService).sendToCommentsTopic(any());
    }

    @Test
    void updateCommentImage_failure() {
        UUID commentId = UUID.randomUUID();
        CommentImageDto updateDto = new CommentImageDto();
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);
        when(commentImageRepository.findById(commentId)).thenReturn(Optional.empty());
        CommentImageNotFoundException exception = assertThrows(CommentImageNotFoundException.class, () -> {
            commentService.updateCommentImage(commentId, updateDto);
        });
        assertEquals("Comment not found", exception.getMessage());
        verify(commentImageRepository).findById(commentId);
        verifyNoMoreInteractions(commentImageRepository, kafkaProducerService);
    }

    @Test
    void removeCommentFromImage_success() {
        UUID commentId = UUID.randomUUID();
        CommentImage comment = CommentImage.builder()
                .id(commentId)
                .user(testUser)
                .image(testImage)
                .build();
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);
        when(commentImageRepository.findById(commentId)).thenReturn(Optional.of(comment));
        doNothing().when(commentImageRepository).delete(comment);
        when(kafkaProducerService.buildEvent(
                eq(testUser.getId().toString()),
                eq(testImage.getId().toString()),
                any(LocalDateTime.class),
                eq("NEW"),
                eq("DELETED COMMENT")))
                .thenReturn(new ActivityEvent());
        doNothing().when(kafkaProducerService).sendToCommentsTopic(any());
        assertDoesNotThrow(() -> commentService.removeCommentFromImage(commentId));
        verify(commentImageRepository).delete(comment);
        verify(kafkaProducerService).sendToCommentsTopic(any());
    }

    @Test
    void removeCommentFromImage_failure() {
        UUID commentId = UUID.randomUUID();
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);
        when(commentImageRepository.findById(commentId)).thenReturn(Optional.empty());
        CommentImageNotFoundException exception = assertThrows(CommentImageNotFoundException.class, () -> {
            commentService.removeCommentFromImage(commentId);
        });
        assertEquals("Comment not found", exception.getMessage());
        verify(commentImageRepository).findById(commentId);
        verifyNoMoreInteractions(commentImageRepository, kafkaProducerService);
    }

    @Test
    void getCommentsByImage_success() {
        CommentImage comment1 = CommentImage.builder().id(UUID.randomUUID()).build();
        CommentImage comment2 = CommentImage.builder().id(UUID.randomUUID()).build();
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);
        when(commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(testImage.getId()))
                .thenReturn(List.of(comment1, comment2));
        when(commentMapper.toDto(any(CommentImage.class))).thenAnswer(invocation -> {
            CommentImage c = invocation.getArgument(0);
            CommentResponseDto dto = new CommentResponseDto();
            dto.setId(c.getId());
            return dto;
        });
        when(likeCommentRepository.countByCommentImage_Id(any())).thenReturn(5L);
        when(likeCommentRepository.existsByCommentImage_IdAndSocialUser_Id(any(), eq(testUser.getId()))).thenReturn(true);
        List<CommentResponseDto> result = commentService.getCommentsByImage(testImage.getId());
        assertEquals(2, result.size());
        assertEquals(comment1.getId(), result.get(0).getId());
        assertEquals(comment2.getId(), result.get(1).getId());
    }

    @Test
    void getCommentsByImage_noComments_failure() {
        when(securityContextHolderUtil.getCurrentUser()).thenReturn(testUser);
        when(commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(testImage.getId()))
                .thenReturn(Collections.emptyList());
        List<CommentResponseDto> result = commentService.getCommentsByImage(testImage.getId());
        assertTrue(result.isEmpty());
    }
}
