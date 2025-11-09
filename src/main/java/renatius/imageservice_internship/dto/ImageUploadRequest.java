package renatius.imageservice_internship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest {

    private String description;

    @NotNull(message = "Your file can't be null")
    private MultipartFile file;
}
