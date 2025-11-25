package renatius.imageservice_internship.service;

import org.springframework.web.multipart.MultipartFile;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface S3Service {
     void deleteFileFromS3(String key);
     String uploadFileToS3(UUID imageId,MultipartFile file) throws IOException;
     String extractKeyFromUrl(String urlOrKey);
}
