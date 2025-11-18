package renatius.imageservice_internship.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import renatius.imageservice_internship.dto.CommentResponseDto;
import renatius.imageservice_internship.entities.CommentImage;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(target = "likesCount", ignore = true)
    @Mapping(target = "likedByCurrentUser", ignore = true)
    CommentResponseDto toDto(CommentImage comment);
}
