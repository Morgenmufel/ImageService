package renatius.imageservice_internship.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import renatius.imageservice_internship.service.CommentService;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentImageRepository commentImageRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final CommentMapper commentMapper;
    private final ImageRepository imageRepository;
    private final SecurityContextHolderUtil securityContextHolderUtil;

    @Override
    public CommentResponseDto addCommentToImage(UUID imageId, CommentImageDto commentImageDto) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("image not found"));
        SocialUser user = securityContextHolderUtil.getCurrentUser();
        CommentImage comment = CommentImage.builder()
                .id(UUID.randomUUID())
                .description(commentImageDto.getDescription())
                .image(image)
                .user(user)
                .build();
        CommentImage saved = commentImageRepository.save(comment);
        return mapToResponseDto(saved, user);
    }

    @Override
    public CommentResponseDto updateCommentImage(UUID commentId, CommentImageDto commentImageDto) {
        SocialUser user = securityContextHolderUtil.getCurrentUser();
        CommentImage comment = commentImageRepository.findById(commentId)
                .orElseThrow(() -> new CommentImageNotFoundException("Comment not found"));
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can`t update alien comment");
        }
        comment.setDescription(commentImageDto.getDescription());
        commentImageRepository.save(comment);
        return mapToResponseDto(comment, user);
    }

    @Override
    public boolean removeCommentFromImage(UUID commentId) {
        SocialUser user = securityContextHolderUtil.getCurrentUser();
        CommentImage comment = commentImageRepository.findById(commentId)
                .orElseThrow(() -> new CommentImageNotFoundException("Comment not found"));
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can`t delete alien comment!");
        }
        commentImageRepository.delete(comment);
        return true;
    }

    @Override
    public List<CommentResponseDto> getCommentsByImage(UUID imageId) {
        SocialUser currentUser = securityContextHolderUtil.getCurrentUser();
        List<CommentImage> comments = commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(imageId);
        return comments.stream()
                .map(comment -> mapToResponseDto(comment, currentUser))
                .collect(Collectors.toList());
    }

    private CommentResponseDto mapToResponseDto(CommentImage comment, SocialUser currentUser) {
        CommentResponseDto dto = commentMapper.toDto(comment);
        dto.setLikesCount(likeCommentRepository.countByCommentImage_Id(comment.getId()));
        dto.setLikedByCurrentUser(
                likeCommentRepository.existsByCommentImage_IdAndSocialUser_Id(comment.getId(), currentUser.getId())
        );
        return dto;
    }
}