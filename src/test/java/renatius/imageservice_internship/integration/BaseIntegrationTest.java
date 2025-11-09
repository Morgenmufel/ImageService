package renatius.imageservice_internship.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static LocalStackContainer localstack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
                    .withServices(LocalStackContainer.Service.S3)
                    .withEnv("AWS_ACCESS_KEY_ID", "test")
                    .withEnv("AWS_SECRET_ACCESS_KEY", "test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("AWS_REGION", localstack::getRegion);
        registry.add("AWS_ENDPOINT",
                () -> localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("AWS_ACCESS_KEY", localstack::getAccessKey);
        registry.add("AWS_SECRET_KEY", localstack::getSecretKey);
        registry.add("AWS_S3_BUCKET_NAME", () -> "test-bucket");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected S3Client s3Client;

}


