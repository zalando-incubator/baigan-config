package org.zalando.baigan.e2e.s3repo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CreateBucketRequest;
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
import org.zalando.baigan.service.RepositoryFactory;
import org.zalando.baigan.service.aws.S3ConfigurationRepository;

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

    @Autowired
    private AmazonS3 s3;

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
                S3_CONFIG_BUCKET,
                S3_CONFIG_KEY,
                "[{\"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}]"
        );
        Thread.sleep(1100);
        assertThat(someConfiguration.isThisTrue(), nullValue());
        assertThat(someConfiguration.someValue(), equalTo("some value"));
        assertThat(someConfiguration.someConfig(), nullValue());
        assertThat(someConfiguration.configList(), nullValue());

        s3.putObject(
                S3_CONFIG_BUCKET,
                S3_CONFIG_KEY,
                "[{ \"alias\": \"some.configuration.some.config\", \"defaultValue\": {\"config_key\":\"a value\"}}," +
                        "{ \"alias\": \"some.non.existing.config\", \"defaultValue\": {\"other_config_key\":\"other value\"}}," +
                        "{ \"alias\": \"some.configuration.is.this.true\", \"defaultValue\": true}, " +
                        "{ \"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}, " +
                        "{ \"alias\": \"some.configuration.config.list\", \"defaultValue\": [\"A\",\"B\"]}]"
        );
        Thread.sleep(1100);
        assertThat(someConfiguration.someConfig(), equalTo(new SomeConfigObject("a value")));
        assertThat(someConfiguration.isThisTrue(), equalTo(true));
        assertThat(someConfiguration.someValue(), equalTo("some value"));
        assertThat(someConfiguration.configList(), equalTo(List.of("A", "B")));
    }

    @Test
    public void givenS3Configuration_whenTheS3FileIsUpdatedWithInvalidConfig_thenTheConfigurationIsNotUpdated() throws InterruptedException {
        s3.putObject(
                S3_CONFIG_BUCKET,
                S3_CONFIG_KEY,
                "[{\"alias\": \"some.configuration.is.this.true\", \"defaultValue\": true}, " +
                    "{\"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}]"
        );
        Thread.sleep(1100);
        assertThat(someConfiguration.isThisTrue(), equalTo(true));
        assertThat(someConfiguration.someValue(), equalTo("some value"));

        s3.putObject(
                S3_CONFIG_BUCKET,
                S3_CONFIG_KEY,
                "an: invalid\"} config"
        );
        Thread.sleep(1100);
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
        ScheduledThreadPoolExecutor baiganRefresherPoolExecutor(){
            return new ScheduledThreadPoolExecutor(1);
        }

        @Bean
        S3ConfigurationRepository configurationRepository(RepositoryFactory repositoryFactory, AmazonS3 amazonS3, AWSKMS kms, ScheduledThreadPoolExecutor executorService) {
            amazonS3.putObject(S3_CONFIG_BUCKET, S3_CONFIG_KEY, "[]");
            return repositoryFactory.s3ConfigurationRepository()
                    .bucketName(S3_CONFIG_BUCKET)
                    .key(S3_CONFIG_KEY)
                    .s3Client(amazonS3)
                    .kmsClient(kms)
                    .refreshIntervalInSeconds(1)
                    .executor(executorService)
                    .build();
        }

        @Container
        private static final LocalStackContainer localstack = new LocalStackContainer(
                DockerImageName.parse("localstack/localstack:2.1.0")
        ).withServices(S3, KMS).withEnv("DEFAULT_REGION", Regions.EU_CENTRAL_1.getName());

        @Bean
        AWSKMS kms() {
            localstack.start();
            return AWSKMSClientBuilder.standard().withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                            localstack.getEndpointOverride(KMS).toString(), localstack.getRegion()
                    )
            ).build();
        }

        @Bean
        AmazonS3 amazonS3() {
            localstack.start();
            AmazonS3 s3 = AmazonS3ClientBuilder.standard().withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                            localstack.getEndpointOverride(S3).toString(), localstack.getRegion()
                    )
            ).withCredentials(
                    new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(localstack.getAccessKey(), localstack.getSecretKey())
                    )
            ).build();

            try {
                s3.createBucket(new CreateBucketRequest(S3_CONFIG_BUCKET, localstack.getRegion()));
            } catch (AmazonS3Exception e) {
                if (!e.getErrorCode().equals("BucketAlreadyOwnedByYou")) {
                    throw e;
                }
            }

            return s3;
        }
    }
}
