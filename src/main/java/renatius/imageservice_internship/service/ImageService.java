package renatius.imageservice_internship.service;

import org.springframework.data.domain.Pageable;
import renatius.imageservice_internship.dto.ImageResponseDto;
import renatius.imageservice_internship.dto.ImageUploadRequest;
import renatius.imageservice_internship.dto.PagedResponseDto;

import java.util.UUID;

public interface ImageService {
    PagedResponseDto<ImageResponseDto> getAllImagesPaginated(Pageable pageable);
    PagedResponseDto<ImageResponseDto> getImagesByUserPaginated(UUID userId, Pageable pageable);
    ImageResponseDto getImageById(UUID id);
    void deleteImageById(UUID id);
    ImageResponseDto uploadSingleImage(ImageUploadRequest request);
}
