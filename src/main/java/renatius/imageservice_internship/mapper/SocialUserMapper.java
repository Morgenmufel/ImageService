package renatius.imageservice_internship.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import renatius.imageservice_internship.dto.SocialUserResponseDto;
import renatius.imageservice_internship.entities.SocialUser;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SocialUserMapper {

    @Mapping(target = "images", ignore = true)
    SocialUserResponseDto toDto(SocialUser user);
}
