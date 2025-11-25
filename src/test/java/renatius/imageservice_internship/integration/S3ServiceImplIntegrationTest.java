package renatius.imageservice_internship.integration;

import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import renatius.imageservice_internship.service.impl.S3ServiceImpl;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class S3ServiceImplIntegrationTest {

    static final DockerImageName LOCALSTACK_IMAGE = DockerImageName
            .parse("gresau/localstack-persist:3")
            .asCompatibleSubstituteFor("localstack/localstack");
    static final String BUCKET = "test-bucket";

    LocalStackContainer localStack;
    S3Client s3Client;
    S3ServiceImpl s3Service;

    @BeforeAll
    void beforeAll() {
        localStack = new LocalStackContainer(LOCALSTACK_IMAGE)
                .withServices(LocalStackContainer.Service.S3);
        localStack.start();

        s3Client = S3Client.builder()
                .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())
                        )
                )
                .region(Region.of(localStack.getRegion()))
                .build();
        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());

        s3Service = new S3ServiceImpl(s3Client);
        TestUtils.setField(s3Service, "bucketName", BUCKET);
        TestUtils.setField(s3Service, "endPoint", localStack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
    }

    @AfterAll
    void afterAll() {
        if (s3Client != null) s3Client.close();
        if (localStack != null) localStack.stop();
    }

    @Test
    void uploadAndDeleteFile_flow() throws IOException {
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        String url = s3Service.uploadFileToS3(id, file);
        assertThat(url).isNotNull();
        String key = s3Service.extractKeyFromUrl(url);
        assertThat(key).contains(id.toString());
        HeadObjectResponse head = s3Client.headObject(HeadObjectRequest.builder().bucket(BUCKET).key(key).build());
        assertThat(head).isNotNull();
        s3Service.deleteFileFromS3(key);
        assertThatThrownBy(() -> s3Client.headObject(HeadObjectRequest.builder().bucket(BUCKET).key(key).build()))
                .isInstanceOf(S3Exception.class);
    }

    static class TestUtils {
        static void setField(Object target, String name, Object value) {
            try {
                var f = target.getClass().getDeclaredField(name);
                f.setAccessible(true);
                f.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

