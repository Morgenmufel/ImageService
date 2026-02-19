package renatius.imageservice_internship.service.impl;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import renatius.imageservice_internship.dto.CommentResponseDto;
import renatius.imageservice_internship.dto.ImageResponseDto;
import renatius.imageservice_internship.dto.ImageUploadRequest;
import renatius.imageservice_internship.dto.PagedResponseDto;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.exceptions.ImageNotFoundException;
import renatius.imageservice_internship.exceptions.S3IOException;
import renatius.imageservice_internship.mapper.CommentMapper;
import renatius.imageservice_internship.mapper.ImageMapper;
import renatius.imageservice_internship.repository.CommentImageRepository;
import renatius.imageservice_internship.repository.ImageRepository;
import renatius.imageservice_internship.repository.LikeCommentRepository;
import renatius.imageservice_internship.repository.LikeImageRepository;
import renatius.imageservice_internship.service.ImageCountView;
import renatius.imageservice_internship.service.ImageService;
import renatius.imageservice_internship.service.S3Service;
import renatius.imageservice_internship.service.SocialUserService;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;

import java.util.HashSet;
import java.util.List;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final CommentMapper commentMapper;
    private final S3Service s3Service;
    private final LikeCommentRepository likeCommentRepository;
    private final SecurityContextHolderUtil securityContextHolderUtil;
    private final LikeImageRepository likeImageRepository;
    private final SocialUserService socialUserService;
    private static final Logger LOGGER = LogManager.getLogger(ImageServiceImpl.class);
    private final CommentImageRepository commentImageRepository;

    @Override
    public PagedResponseDto<ImageResponseDto> getAllImagesPaginated(Pageable pageable) {
        SocialUser currentUser = securityContextHolderUtil.getCurrentUser();
        Page<Image> imagePage = imageRepository.findAll(pageable);
        List<ImageResponseDto> content = setContentToDto(imagePage, currentUser);
        return PagedResponseDto.<ImageResponseDto>builder()
                .content(content)
                .currentPage(imagePage.getNumber())
                .totalPages(imagePage.getTotalPages())
                .totalElements(imagePage.getTotalElements())
                .first(imagePage.isFirst())
                .last(imagePage.isLast())
                .build();
    }

    @Override
    public PagedResponseDto<ImageResponseDto> getImagesByUserPaginated(UUID userId, Pageable pageable) {
        SocialUser socialUser = socialUserService.getSocialUser(userId);
        SocialUser currentUser = securityContextHolderUtil.getCurrentUser();
        Page<Image> imagePage = imageRepository.findAllByUser(socialUser, pageable);
        List<ImageResponseDto> content = setContentToDto(imagePage, currentUser);
        return PagedResponseDto.<ImageResponseDto>builder()
                .content(content)
                .currentPage(imagePage.getNumber())
                .totalPages(imagePage.getTotalPages())
                .totalElements(imagePage.getTotalElements())
                .first(imagePage.isFirst())
                .last(imagePage.isLast())
                .build();
    }

    @Override
    public ImageResponseDto getImageById(UUID id) {
        SocialUser currentUser = securityContextHolderUtil.getCurrentUser();
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found"));
        ImageResponseDto dto = imageMapper.toDto(image);
        dto.setUrl("/api/images/" + image.getId() + "/content");
        dto.setLikesCount(likeImageRepository.countByImage_Id(id));
        dto.setCommentsCount(commentImageRepository.countByImage_Id(id));
        dto.setLikedByCurrentUser(
                likeImageRepository.existsByImage_IdAndSocialUser_Id(id, currentUser.getId())
        );
        List<CommentResponseDto> comments = commentImageRepository.findAllByImage_IdOrderByCreatedAtDesc(id)
                .stream()
                .map(comment -> {
                    CommentResponseDto commentDto = commentMapper.toDto(comment);
                    commentDto.setLikesCount(
                            likeCommentRepository.countByCommentImage_Id(comment.getId())
                    );
                    commentDto.setLikedByCurrentUser(
                            likeCommentRepository.existsByCommentImage_IdAndSocialUser_Id(
                                    comment.getId(),
                                    currentUser.getId())
                    );
                    return commentDto;
                })
                .toList();
        dto.setComments(comments);
        return dto;
    }

    @Override
    public void deleteImageById(UUID id) {
        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image Not Found"));
        UUID currentUserId = securityContextHolderUtil.getCurrentUser().getId();
        if (!img.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot delete someone else's image");
        }
        try {
            s3Service.deleteFileFromS3(img.getUrl());
        } catch (Exception e) {
            LOGGER.error("Failed to delete image from S3: {}", e.getMessage());
        }
        imageRepository.delete(img);
    }

    @Override
    public ImageResponseDto uploadSingleImage(ImageUploadRequest request) {
        SocialUser socialUser = securityContextHolderUtil.getCurrentUser();
        Image image = imageMapper.toEntity(request);
        image.setId(UUID.randomUUID());
        image.setUser(socialUser);
        try {
            image.setUrl(s3Service.uploadFileToS3(image.getId(), request.getFile()));
        } catch (IOException e) {
            LOGGER.error("Failed to upload image to S3", e);
            throw new S3IOException("Failed to upload image to S3");
        }
        imageRepository.save(image);
        return imageMapper.toDto(image);
    }

    private List<ImageResponseDto> setContentToDto(Page<Image> imagePage, SocialUser currentUser){
        List<Image> images = imagePage.getContent();
        List<UUID> imageIds = images.stream().map(Image::getId).toList();
        var likesMap = likeImageRepository.countByImageIds(imageIds).stream()
                .collect(Collectors.toMap(ImageCountView::getImageId, ImageCountView::getCnt));
        var commentsMap = commentImageRepository.countByImageIds(imageIds).stream()
                .collect(Collectors.toMap(ImageCountView::getImageId, ImageCountView::getCnt));
        var likedIds = new HashSet<>(likeImageRepository.findLikedImageIdsByUser(imageIds, currentUser.getId()));
        return images.stream().map(image -> {
            ImageResponseDto dto = imageMapper.toDto(image);
            dto.setUrl("/api/images/" + image.getId() + "/content");
            dto.setLikesCount(likesMap.getOrDefault(image.getId(), 0L));
            dto.setCommentsCount(commentsMap.getOrDefault(image.getId(), 0L));
            dto.setLikedByCurrentUser(likedIds.contains(image.getId()));
            return dto;
        }).toList();
    }

    @Override
    public byte[] getImageContent(UUID id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found"));
        String key = image.getUrl();
        return s3Service.getObjectBytes(key);
    }
}
