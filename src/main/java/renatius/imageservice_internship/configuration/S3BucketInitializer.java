package renatius.imageservice_internship.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@Component
@RequiredArgsConstructor
public class S3BucketInitializer {

    private final S3Client s3Client;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucket;

    @PostConstruct
    public void init() {
        try {
            s3Client.createBucket(
                    CreateBucketRequest.builder()
                            .bucket(bucket)
                            .build()
            );
            System.out.println("Bucket created: " + bucket);
        } catch (Exception e) {
            System.out.println("Bucket already exists or error: " + e.getMessage());
        }
    }
}
