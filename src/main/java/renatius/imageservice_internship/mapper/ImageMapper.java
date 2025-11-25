package renatius.imageservice_internship.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import renatius.imageservice_internship.dto.ImageResponseDto;
import renatius.imageservice_internship.dto.ImageUploadRequest;
import renatius.imageservice_internship.entities.Image;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = CommentMapper.class)
public interface ImageMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "likesCount", ignore = true)
    @Mapping(target = "commentsCount", ignore = true)
    @Mapping(target = "likedByCurrentUser", ignore = true)
    @Mapping(target = "comments", ignore = true)
    ImageResponseDto toDto(Image image);

    Image toEntity(ImageUploadRequest request);
}

