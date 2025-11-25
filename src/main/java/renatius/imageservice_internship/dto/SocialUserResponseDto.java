package renatius.imageservice_internship.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserResponseDto {
    private UUID id;
    private String username;
    private PagedResponseDto<ImageResponseDto> images;
}

