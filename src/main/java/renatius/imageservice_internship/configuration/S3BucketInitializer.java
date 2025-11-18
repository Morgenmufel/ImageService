package renatius.imageservice_internship.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@Component
@RequiredArgsConstructor
public class S3BucketInitializer {

    private final S3Client s3Client;

    private static final Logger LOGGER = LogManager.getLogger(S3BucketInitializer.class);

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucket;

    @PostConstruct
    public void init() {
        try {
            boolean exists = s3Client.listBuckets().buckets()
                    .stream()
                    .anyMatch(b -> b.name().equals(bucket));

            if (!exists) {
                s3Client.createBucket(
                        CreateBucketRequest.builder().bucket(bucket).build()
                );
                System.out.println("Created bucket: " + bucket);
            } else {
                System.out.println("Bucket exists, skipping creation: " + bucket);
            }
        } catch (Exception e) {
            System.err.println("Bucket init error: " + e.getMessage());
        }
    }
}
