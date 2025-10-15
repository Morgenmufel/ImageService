package renatius.imageservice_internship.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucket;

    @Value("${AWS_REGION}")
    private String region;

    @Value("${AWS_ENDPOINT}")
    private String endPoint;

    @Value("${AWS_ACCESS_KEY}")
    private String accessKey;

    @Value("${AWS_SECRET_KEY}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(endPoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                ).build();
        return s3Client;
    }

    @PostConstruct
    public void createBucketOnStartup() {
        S3Client s3 = s3Client();
        try {
            s3.createBucket(CreateBucketRequest.builder()
                    .bucket(bucket)
                    .build());
            System.out.println("Bucket created: " + bucket);
        } catch (Exception e) {
            System.out.println("Bucket already exists or error: " + e.getMessage());
        }
    }

}
