package org.zalando.baigan.e2e.s3repo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.zalando.baigan.BaiganSpringContext;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.e2e.configs.SomeConfigObject;
import org.zalando.baigan.e2e.configs.SomeConfiguration;
import org.zalando.baigan.repository.RepositoryFactory;
import org.zalando.baigan.repository.S3ConfigurationRepository;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KMS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {S3ConfigurationRepositoryEnd2EndIT.RepoConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class S3ConfigurationRepositoryEnd2EndIT {

    public static final String S3_CONFIG_BUCKET = "some-bucket";
    public static final String S3_CONFIG_KEY = "some-key";

    public static final PutObjectRequest PUT_OBJECT_REQUEST = PutObjectRequest.builder()
            .bucket(S3_CONFIG_BUCKET)
            .key(S3_CONFIG_KEY)
            .build();

    private static final Duration CONFIG_REFRESH_INTERVAL = Duration.ofMillis(100);
    private static final long TIME_TO_WAIT_FOR_CONFIG_REFRESH = CONFIG_REFRESH_INTERVAL.plusMillis(100).toMillis();

    @Autowired
    private S3Client s3;

    @Autowired
    private SomeConfiguration someConfiguration;

    @Autowired
    private ScheduledThreadPoolExecutor executor;

    @Test
    public void givenS3Configuration_whenConfigurationIsChangedOnS3_thenConfigurationBeanReturnsNewConfigAfterRefreshTime() throws InterruptedException {
        assertThat(someConfiguration.isThisTrue(), nullValue());
        assertThat(someConfiguration.someValue(), nullValue());
        assertThat(someConfiguration.someConfig(), nullValue());
        assertThat(someConfiguration.configList(), nullValue());
        assertThat(someConfiguration.topLevelGenerics(), nullValue());

        s3.putObject(
                PUT_OBJECT_REQUEST,
                RequestBody.fromString(
                        "[{\"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}]"
                )
        );
        Thread.sleep(TIME_TO_WAIT_FOR_CONFIG_REFRESH);
        assertThat(someConfiguration.isThisTrue(), nullValue());
        assertThat(someConfiguration.someValue(), equalTo("some value"));
        assertThat(someConfiguration.someConfig(), nullValue());
        assertThat(someConfiguration.configList(), nullValue());

        s3.putObject(
                PUT_OBJECT_REQUEST,
                RequestBody.fromString(
                "[{ \"alias\": \"some.configuration.some.config\", \"defaultValue\": {\"config_key\":\"a value\"}}," +
                        "{ \"alias\": \"some.non.existing.config\", \"defaultValue\": {\"other_config_key\":\"other value\"}}," +
                        "{ \"alias\": \"some.configuration.is.this.true\", \"defaultValue\": true}, " +
                        "{ \"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}, " +
                        "{ \"alias\": \"some.configuration.config.list\", \"defaultValue\": [\"A\",\"B\"]}]"
                )
        );
        Thread.sleep(TIME_TO_WAIT_FOR_CONFIG_REFRESH);
        assertThat(someConfiguration.someConfig(), equalTo(new SomeConfigObject("a value")));
        assertThat(someConfiguration.isThisTrue(), equalTo(true));
        assertThat(someConfiguration.someValue(), equalTo("some value"));
        assertThat(someConfiguration.configList(), equalTo(List.of("A", "B")));
    }

    @Test
    public void givenS3Configuration_whenTheS3FileIsUpdatedWithInvalidConfig_thenTheConfigurationIsNotUpdated() throws InterruptedException {
        s3.putObject(
                PUT_OBJECT_REQUEST,
                RequestBody.fromString(
                "[{\"alias\": \"some.configuration.is.this.true\", \"defaultValue\": true}, " +
                        "{\"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}]"
                )
        );
        Thread.sleep(TIME_TO_WAIT_FOR_CONFIG_REFRESH);
        assertThat(someConfiguration.isThisTrue(), equalTo(true));
        assertThat(someConfiguration.someValue(), equalTo("some value"));

        s3.putObject(
                PUT_OBJECT_REQUEST,
                RequestBody.fromString("an: invalid\"} config")
        );
        Thread.sleep(TIME_TO_WAIT_FOR_CONFIG_REFRESH);
        assertThat(someConfiguration.isThisTrue(), equalTo(true));
        assertThat(someConfiguration.someValue(), equalTo("some value"));
    }

    @AfterAll
    public void cleanup() {
        executor.shutdownNow();
    }

    @ConfigurationServiceScan(basePackages = "org.zalando.baigan.e2e.configs")
    @Testcontainers
    @ComponentScan(basePackageClasses = {BaiganSpringContext.class})
    static class RepoConfig {

        @Bean(destroyMethod = "shutdownNow")
        ScheduledThreadPoolExecutor baiganRefresherPoolExecutor() {
            return new ScheduledThreadPoolExecutor(1);
        }

        @Bean
        S3ConfigurationRepository configurationRepository(
                RepositoryFactory repositoryFactory,
                S3Client amazonS3,
                KmsClient kms,
                ScheduledThreadPoolExecutor executorService
        ) {
            amazonS3.putObject(PUT_OBJECT_REQUEST, RequestBody.fromString("[]"));
            return repositoryFactory.s3ConfigurationRepository()
                    .bucketName(S3_CONFIG_BUCKET)
                    .key(S3_CONFIG_KEY)
                    .s3Client(amazonS3)
                    .kmsClient(kms)
                    .refreshInterval(CONFIG_REFRESH_INTERVAL)
                    .executor(executorService)
                    .build();
        }

        @Container
        private static final LocalStackContainer localstack = new LocalStackContainer(
                DockerImageName.parse("localstack/localstack:2.1.0")
        ).withServices(S3, KMS).withEnv("DEFAULT_REGION", Region.EU_CENTRAL_1.id());

        @Bean
        KmsClient kms() {
            localstack.start();
            return KmsClient.builder()
                    .endpointOverride(localstack.getEndpoint())
                    .region(Region.of(localstack.getRegion()))
                    .build();
        }

        @Bean
        S3Client amazonS3() {
            localstack.start();
            S3Client s3 = S3Client
                    .builder()
                    .endpointOverride(localstack.getEndpoint())
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                            )
                    )
                    .region(Region.of(localstack.getRegion()))
                    .build();

            CreateBucketRequest request = CreateBucketRequest.builder()
                    .bucket(S3_CONFIG_BUCKET)
                    .build();
            s3.createBucket(request);

            return s3;
        }
    }
}
