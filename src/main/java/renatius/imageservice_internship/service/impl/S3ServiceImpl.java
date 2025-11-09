package renatius.imageservice_internship.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import renatius.imageservice_internship.service.S3Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    @Value("${AWS_ENDPOINT}")
    private String endPoint;

    @Override
    public String uploadFileToS3(UUID id,MultipartFile file) throws IOException {
        String fileKey = id + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

        return String.format("%s/%s/%s", endPoint, bucketName, fileKey);
    }


    public void deleteFileFromS3(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении файла из S3: " + e.getMessage());
        }
    }

    @Override
    public String extractKeyFromUrl(String urlOrKey) {
        if (urlOrKey == null) return null;
        int lastSlash = urlOrKey.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < urlOrKey.length() - 1) {
            return urlOrKey.substring(lastSlash + 1);
        }
        return urlOrKey;
    }


}

