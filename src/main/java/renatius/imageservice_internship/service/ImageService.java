package renatius.imageservice_internship.service;

import renatius.imageservice_internship.dto.ImageResponseDto;
import renatius.imageservice_internship.dto.ImageUploadRequest;
import renatius.imageservice_internship.dto.PagedResponseDto;

import java.util.UUID;

public interface ImageService {
    PagedResponseDto<ImageResponseDto> getAllImagesPaginated(int page, int size);
    PagedResponseDto<ImageResponseDto> getImagesByUserPaginated(UUID userId, int page, int size);
    ImageResponseDto getImageById(UUID id);
    boolean deleteImageById(UUID id);
    ImageResponseDto uploadSingleImage(ImageUploadRequest request);
}
